package com.dzg.shop.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dzg.common.core.exception.ServiceException;
import com.dzg.shop.config.ShopAiProperties;
import com.dzg.shop.domain.ShopCreditRecord;
import com.dzg.shop.domain.ShopOrder;
import com.dzg.shop.domain.ShopOrderItem;
import com.dzg.shop.domain.ShopProduct;
import com.dzg.shop.domain.ShopStock;
import com.dzg.shop.domain.bo.ShopAiChatBo;
import com.dzg.shop.domain.vo.ShopAiAnalysisVo;
import com.dzg.shop.domain.vo.ShopAiBusinessAnalysisVo;
import com.dzg.shop.domain.vo.ShopAiBusinessQuestionVo;
import com.dzg.shop.mapper.ShopCreditRecordMapper;
import com.dzg.shop.mapper.ShopOrderItemMapper;
import com.dzg.shop.mapper.ShopOrderMapper;
import com.dzg.shop.mapper.ShopProductMapper;
import com.dzg.shop.mapper.ShopStockMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ShopAiAnalysisService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ShopOrderMapper orderMapper;
    private final ShopOrderItemMapper orderItemMapper;
    private final ShopCreditRecordMapper creditRecordMapper;
    private final ShopStockMapper stockMapper;
    private final ShopProductMapper productMapper;
    private final ShopAiProperties aiProperties;
    private final DeepSeekAnalysisClient deepSeekAnalysisClient;
    private final ObjectMapper objectMapper;

    private ShopAiAnalysisVo cachedAnalysis;
    private LocalDateTime cachedAt;
    private ShopAiBusinessAnalysisVo cachedBusinessAnalysis;
    private LocalDateTime cachedBusinessAt;

    public synchronized ShopAiAnalysisVo analysis() {
        return analysis(false);
    }

    public synchronized ShopAiAnalysisVo analysis(boolean force) {
        if (!force && cachedAnalysis != null && cachedAt != null && cachedAt.plusSeconds(cacheSeconds()).isAfter(LocalDateTime.now())) {
            return cachedAnalysis;
        }

        AnalysisContext context = loadContext();
        ShopAiAnalysisVo fallback = localAnalysis(context, null);
        ShopAiAnalysisVo result = fallback;
        if (!Boolean.TRUE.equals(aiProperties.getEnabled())) {
            result = localAnalysis(context, "AI 分析未启用");
        } else if (!StringUtils.hasText(aiProperties.getApiKey())) {
            result = localAnalysis(context, "DeepSeek API Key 未配置");
        } else {
            try {
                result = parseDeepSeekAnalysis(deepSeekAnalysisClient.completeJson(buildPrompt(context)));
            } catch (Exception e) {
                result = localAnalysis(context, "DeepSeek 调用失败，已使用本地分析：" + e.getMessage());
            }
        }
        cachedAnalysis = result;
        cachedAt = LocalDateTime.now();
        return result;
    }

    public synchronized ShopAiBusinessAnalysisVo businessAnalysis() {
        return businessAnalysis(false);
    }

    public synchronized ShopAiBusinessAnalysisVo businessAnalysis(boolean force) {
        if (!force && cachedBusinessAnalysis != null && cachedBusinessAt != null && cachedBusinessAt.plusSeconds(cacheSeconds()).isAfter(LocalDateTime.now())) {
            return cachedBusinessAnalysis;
        }

        AnalysisContext context = loadContext();
        ShopAiBusinessAnalysisVo result;
        if (!Boolean.TRUE.equals(aiProperties.getEnabled())) {
            result = localBusinessAnalysis(context, "AI 分析未启用");
        } else if (!StringUtils.hasText(aiProperties.getApiKey())) {
            result = localBusinessAnalysis(context, "DeepSeek API Key 未配置");
        } else {
            try {
                result = parseBusinessAnalysis(deepSeekAnalysisClient.completeJson(buildBusinessPrompt(context)));
            } catch (Exception e) {
                result = localBusinessAnalysis(context, "DeepSeek 调用失败，已使用本地详细分析：" + e.getMessage());
            }
        }
        cachedBusinessAnalysis = result;
        cachedBusinessAt = LocalDateTime.now();
        return result;
    }

    public ShopAiBusinessQuestionVo businessQuestion(ShopAiChatBo bo) {
        if (bo == null || !StringUtils.hasText(bo.getContent())) {
            throw new ServiceException("请输入要询问 AI 的经营分析问题");
        }
        AnalysisContext context = loadContext();
        if (!Boolean.TRUE.equals(aiProperties.getEnabled())) {
            return localBusinessQuestion(context, bo.getContent(), "AI 分析未启用");
        }
        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            return localBusinessQuestion(context, bo.getContent(), "DeepSeek API Key 未配置");
        }
        try {
            return parseBusinessQuestion(deepSeekAnalysisClient.completeJson(buildBusinessQuestionPrompt(context, bo.getContent())));
        } catch (Exception e) {
            return localBusinessQuestion(context, bo.getContent(), "DeepSeek 调用失败，已使用本地经营分析回复：" + e.getMessage());
        }
    }

    private AnalysisContext loadContext() {
        LocalDate today = LocalDate.now();
        ZoneId zoneId = ZoneId.systemDefault();
        Date todayBegin = Date.from(today.atStartOfDay(zoneId).toInstant());
        Date tomorrowBegin = Date.from(today.plusDays(1).atStartOfDay(zoneId).toInstant());
        List<ShopOrder> todayOrders = orderMapper.selectList(Wrappers.<ShopOrder>lambdaQuery()
            .ge(ShopOrder::getOrderTime, todayBegin)
            .lt(ShopOrder::getOrderTime, tomorrowBegin));
        List<ShopOrder> orders = orderMapper.selectList(Wrappers.lambdaQuery());
        List<ShopOrderItem> orderItems = orderItemMapper.selectList(Wrappers.lambdaQuery());
        List<ShopCreditRecord> unsettledCredits = creditRecordMapper.selectList(
            Wrappers.<ShopCreditRecord>lambdaQuery().ne(ShopCreditRecord::getStatus, ShopConstants.SETTLED)
        );
        List<ShopStock> stocks = stockMapper.selectList(Wrappers.lambdaQuery());
        List<ShopProduct> products = productMapper.selectList(Wrappers.lambdaQuery());
        Map<Long, ShopProduct> productMap = products.stream()
            .collect(Collectors.toMap(ShopProduct::getProductId, Function.identity(), (left, right) -> left));

        BigDecimal todaySales = todayOrders.stream()
            .map(ShopOrder::getPaidAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal todayCredit = todayOrders.stream()
            .filter(order -> ShopConstants.CREDIT.equals(order.getPayType()))
            .map(ShopOrder::getTotalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal unpaidTotal = unsettledCredits.stream()
            .map(ShopCreditRecord::getUnpaidAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal salesTotal = orders.stream()
            .map(ShopOrder::getPaidAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grossProfit = grossProfit(orderItems, productMap);
        List<ShopStock> lowStocks = stocks.stream()
            .filter(stock -> stock.getQuantity() != null && stock.getWarningQty() != null && stock.getQuantity() <= stock.getWarningQty())
            .sorted(Comparator.comparing(stock -> stock.getQuantity() == null ? Integer.MAX_VALUE : stock.getQuantity()))
            .toList();
        List<ProductSale> topSales = topSales(orderItems);

        return new AnalysisContext(todayOrders, orders, unsettledCredits, lowStocks, productMap, topSales,
            todaySales, todayCredit, unpaidTotal, salesTotal, grossProfit);
    }

    private ShopAiAnalysisVo localAnalysis(AnalysisContext context, String fallbackReason) {
        ShopAiAnalysisVo vo = new ShopAiAnalysisVo();
        vo.setProvider("local");
        vo.setModel(null);
        vo.setGeneratedByAi(false);
        vo.setFallbackReason(fallbackReason);
        vo.setAnalysisTime(LocalDateTime.now());
        vo.setRiskLevel(riskLevel(context.lowStocks().size(), context.unpaidTotal()));
        vo.setSummary("今日完成 " + context.todayOrders().size() + " 笔订单，实收 " + money(context.todaySales())
            + " 元，赊账 " + money(context.todayCredit()) + " 元，粗略毛利 " + money(context.grossProfit())
            + " 元；当前有 " + context.lowStocks().size() + " 个库存预警，未还赊账 "
            + money(context.unpaidTotal()) + " 元。");
        vo.getInsights().add(priorityInsight(context));
        vo.getInsights().add(replenishmentInsight(context));
        vo.getInsights().add(salesInsight(context));
        vo.getInsights().add(creditInsight(context));
        vo.getInsights().add(purchaseInsight(context));
        vo.getInsights().add(reportInsight(context));
        return vo;
    }

    private ShopAiBusinessAnalysisVo localBusinessAnalysis(AnalysisContext context, String fallbackReason) {
        ShopAiBusinessAnalysisVo vo = new ShopAiBusinessAnalysisVo();
        vo.setProvider("local");
        vo.setModel(null);
        vo.setGeneratedByAi(false);
        vo.setFallbackReason(fallbackReason);
        vo.setAnalysisTime(LocalDateTime.now());
        vo.setRiskLevel(riskLevel(context.lowStocks().size(), context.unpaidTotal()));
        vo.setSummary("店铺当前实收 " + money(context.todaySales()) + " 元，未还赊账 " + money(context.unpaidTotal())
            + " 元，低库存商品 " + context.lowStocks().size() + " 个。短期先稳住现金和库存，中长期要把热销品、赊账额度和采购节奏固定下来。");
        vo.getMetrics().add(metric("今日订单", String.valueOf(context.todayOrders().size()), "看客流是否稳定"));
        vo.getMetrics().add(metric("今日实收", "￥" + money(context.todaySales()), "现金流核心指标"));
        vo.getMetrics().add(metric("粗略毛利", "￥" + money(context.grossProfit()), "用来判断货品利润"));
        vo.getMetrics().add(metric("未还赊账", "￥" + money(context.unpaidTotal()), context.unsettledCredits().size() + " 笔未结清"));
        vo.getMetrics().add(metric("库存预警", String.valueOf(context.lowStocks().size()), lowStockText(context)));
        vo.getMetrics().add(metric("热销商品", topSaleText(context), "优先保障陈列和库存"));
        vo.getSections().add(section("经营总览", "今天", "info",
            "今天先看三件事：收了多少钱、欠款有没有扩大、常卖货有没有断货。当前实收 " + money(context.todaySales())
                + " 元，今日赊账 " + money(context.todayCredit()) + " 元，说明现金流和赊账都需要一起盯。",
            List.of("收摊前核对现金、赊账和销售记录", "把热销商品补到顺手位置", "库存预警商品先核实实际货架数量")));
        vo.getSections().add(section("风险诊断", "1-7天", normalizeInsightLevelByRisk(vo.getRiskLevel()),
            "主要风险来自库存预警和赊账回收。低库存会直接影响明天销售，赊账过高会占用采购现金。",
            List.of("低库存商品按销量和利润排优先级", "未还赊账先联系金额较大的客户", "采购前预留必要现金，不要一次压太多货")));
        vo.getSections().add(section("短期动作", "1周内", "warning",
            "一周内目标是稳定货架和现金流，不追求复杂管理。把补货、催收、复盘固定成每天闭店前 10 分钟的动作。",
            List.of("每天闭店前查看经营报表", "库存预警超过 3 个时立即整理采购清单", "赊账新增时同步写清还款时间")));
        vo.getSections().add(section("中期优化", "1-3个月", "info",
            "中期目标是形成固定采购节奏和热销商品清单。把常卖货分成必备、观察、减少三类，降低缺货和积压。",
            List.of("每周统计热销前 5 个商品", "给熟客设置赊账上限", "用采购记录反推安全库存")));
        vo.getSections().add(section("长期规划", "3-12个月", "success",
            "长期目标是让店铺从凭经验进货，变成按销量和回款能力进货。优先做稳周转快的基础货，再逐步增加利润更好的商品。",
            List.of("建立固定供应商和备用供应商", "按季节提前准备高频商品", "每月复盘毛利、赊账和库存周转")));
        vo.getSuggestions().addAll(defaultBusinessSuggestions());
        return vo;
    }

    private ShopAiBusinessAnalysisVo parseBusinessAnalysis(String content) throws JsonProcessingException {
        Map<String, Object> data = objectMapper.readValue(extractJson(content), MAP_TYPE);
        String summary = stringValue(data.get("summary"));
        if (!StringUtils.hasText(summary)) {
            throw new IllegalStateException("DeepSeek 详细分析缺少 summary");
        }
        ShopAiBusinessAnalysisVo vo = new ShopAiBusinessAnalysisVo();
        vo.setProvider("deepseek");
        vo.setModel(aiProperties.getModel());
        vo.setGeneratedByAi(true);
        vo.setFallbackReason(null);
        vo.setAnalysisTime(LocalDateTime.now());
        vo.setSummary(summary);
        vo.setRiskLevel(normalizeRiskLevel(stringValue(data.get("riskLevel"))));
        Object metricsValue = data.get("metrics");
        if (metricsValue instanceof List<?> metrics) {
            metrics.stream()
                .filter(Map.class::isInstance)
                .map(item -> toMetric((Map<?, ?>) item))
                .filter(Objects::nonNull)
                .limit(8)
                .forEach(vo.getMetrics()::add);
        }
        Object sectionsValue = data.get("sections");
        if (sectionsValue instanceof List<?> sections) {
            sections.stream()
                .filter(Map.class::isInstance)
                .map(item -> toSection((Map<?, ?>) item))
                .filter(Objects::nonNull)
                .limit(8)
                .forEach(vo.getSections()::add);
        }
        vo.getSuggestions().addAll(stringListValue(data.get("suggestions")));
        if (vo.getSections().isEmpty()) {
            throw new IllegalStateException("DeepSeek 详细分析缺少 sections");
        }
        if (vo.getSuggestions().isEmpty()) {
            vo.getSuggestions().addAll(defaultBusinessSuggestions());
        }
        return vo;
    }

    private ShopAiBusinessQuestionVo parseBusinessQuestion(String content) throws JsonProcessingException {
        Map<String, Object> data = objectMapper.readValue(extractJson(content), MAP_TYPE);
        String answer = stringValue(data.get("answer"));
        if (!StringUtils.hasText(answer)) {
            throw new IllegalStateException("DeepSeek 经营分析问答缺少 answer");
        }
        ShopAiBusinessQuestionVo vo = new ShopAiBusinessQuestionVo();
        vo.setAnswer(answer);
        vo.setGeneratedByAi(true);
        vo.getSuggestions().addAll(stringListValue(data.get("suggestions")));
        if (vo.getSuggestions().isEmpty()) {
            vo.getSuggestions().addAll(defaultBusinessSuggestions());
        }
        return vo;
    }

    private ShopAiBusinessQuestionVo localBusinessQuestion(AnalysisContext context, String question, String fallbackReason) {
        ShopAiBusinessQuestionVo vo = new ShopAiBusinessQuestionVo();
        vo.setGeneratedByAi(false);
        vo.setFallbackReason(fallbackReason);
        vo.setAnswer("我只回答店铺经营分析相关问题。结合当前数据，建议先看现金流、库存和赊账：今日实收 "
            + money(context.todaySales()) + " 元，库存预警 " + context.lowStocks().size() + " 个，未还赊账 "
            + money(context.unpaidTotal()) + " 元。你问的是“" + question + "”，如果要落到经营动作上，先判断它会影响销售、补货、赊账回款还是长期采购计划。");
        vo.getSuggestions().addAll(defaultBusinessSuggestions());
        return vo;
    }

    private String buildBusinessPrompt(AnalysisContext context) {
        return """
            你是“店掌柜”的专精经营分析顾问，只分析乡镇小店经营，不回答闲聊、技术、娱乐、医疗、法律等无关问题。
            请基于系统给出的真实数据，生成一份比首页和经营报表更详细的经营分析报告。
            只返回 JSON 对象，不要 Markdown。JSON 字段：
            - summary：3 到 5 句话，说明现状、主要风险和长期方向。
            - riskLevel：low、medium、high。
            - metrics：4 到 8 个关键指标，每项包含 label、value、hint。
            - sections：5 到 8 个分析段落，每项包含 title、horizon、level、content、actions。
            - suggestions：4 到 6 个后续可追问的问题。
            sections 必须覆盖：经营总览、风险诊断、短期动作、采购与库存、赊账与现金流、中长期规划。
            长期规划要给 3-12 个月方向，不只写“马上要干什么”。content 要具体，actions 每段 2 到 4 条。

            经营数据：
            今日订单数：%d
            今日实收：%s 元
            今日赊账：%s 元
            累计销售额：%s 元
            粗略毛利：%s 元
            未还赊账笔数：%d
            未还赊账金额：%s 元
            库存预警数量：%d
            库存预警商品：%s
            热销商品：%s
            """.formatted(
            context.todayOrders().size(),
            money(context.todaySales()),
            money(context.todayCredit()),
            money(context.salesTotal()),
            money(context.grossProfit()),
            context.unsettledCredits().size(),
            money(context.unpaidTotal()),
            context.lowStocks().size(),
            lowStockText(context),
            topSaleText(context)
        );
    }

    private String buildBusinessQuestionPrompt(AnalysisContext context, String question) {
        return """
            你是“店掌柜”的专精经营分析顾问。只允许回答店铺经营分析、销售、库存、采购、赊账、现金流、长期规划相关问题。
            如果问题偏离经营分析，请简短拒绝并把话题拉回店铺经营。
            只返回 JSON 对象，不要 Markdown。JSON 字段：
            - answer：回答老板的问题，要结合当前经营数据，不能编造系统没有的数据。
            - suggestions：3 到 5 个后续可问的经营分析问题。

            当前经营数据：
            今日订单数：%d
            今日实收：%s 元
            今日赊账：%s 元
            累计销售额：%s 元
            粗略毛利：%s 元
            未还赊账笔数：%d
            未还赊账金额：%s 元
            库存预警数量：%d
            库存预警商品：%s
            热销商品：%s

            老板问题：
            %s
            """.formatted(
            context.todayOrders().size(),
            money(context.todaySales()),
            money(context.todayCredit()),
            money(context.salesTotal()),
            money(context.grossProfit()),
            context.unsettledCredits().size(),
            money(context.unpaidTotal()),
            context.lowStocks().size(),
            lowStockText(context),
            topSaleText(context),
            question
        );
    }

    private ShopAiAnalysisVo parseDeepSeekAnalysis(String content) throws JsonProcessingException {
        Map<String, Object> data = objectMapper.readValue(extractJson(content), MAP_TYPE);
        String summary = stringValue(data.get("summary"));
        String riskLevel = normalizeRiskLevel(stringValue(data.get("riskLevel")));
        if (!StringUtils.hasText(summary)) {
            throw new IllegalStateException("DeepSeek 结果缺少 summary");
        }

        ShopAiAnalysisVo vo = new ShopAiAnalysisVo();
        vo.setProvider("deepseek");
        vo.setModel(aiProperties.getModel());
        vo.setGeneratedByAi(true);
        vo.setFallbackReason(null);
        vo.setAnalysisTime(LocalDateTime.now());
        vo.setSummary(summary);
        vo.setRiskLevel(riskLevel);
        Object insightsValue = data.get("insights");
        if (insightsValue instanceof List<?> insights) {
            insights.stream()
                .filter(Map.class::isInstance)
                .map(item -> toInsight((Map<?, ?>) item))
                .filter(Objects::nonNull)
                .limit(6)
                .forEach(vo.getInsights()::add);
        }
        if (vo.getInsights().isEmpty()) {
            throw new IllegalStateException("DeepSeek 结果缺少 insights");
        }
        return vo;
    }

    private String buildPrompt(AnalysisContext context) {
        return """
            请根据下面的店铺经营数据，生成给小店老板看的经营分析。
            要求：
            1. 只返回 JSON 对象，不要 Markdown。
            2. JSON 字段必须是 summary、riskLevel、insights。
            3. riskLevel 只能是 low、medium、high。
            4. insights 最多 6 条，每条包含 type、title、content、level、actionText、actionPath。
            5. insight.level 只能是 success、info、warning、danger。
            6. actionPath 只能从 /shop/stock、/shop/purchase、/shop/cashier、/shop/credit、/shop/report 中选择。
            7. 文案适合中老年小店经营者，直接说明“先做什么、为什么、看哪个数字”。
            8. summary 写 2 句话以内，要包含订单、实收、库存、赊账的综合判断。
            9. insights 尽量覆盖：今日优先事项、库存补货、热销商品、赊账跟进、采购建议、报表观察。

            经营数据：
            今日订单数：%d
            今日实收：%s 元
            今日赊账：%s 元
            累计销售额：%s 元
            粗略毛利：%s 元
            未还赊账笔数：%d
            未还赊账金额：%s 元
            库存预警数量：%d
            库存预警商品：%s
            热销商品：%s
            """.formatted(
            context.todayOrders().size(),
            money(context.todaySales()),
            money(context.todayCredit()),
            money(context.salesTotal()),
            money(context.grossProfit()),
            context.unsettledCredits().size(),
            money(context.unpaidTotal()),
            context.lowStocks().size(),
            lowStockText(context),
            topSaleText(context)
        );
    }

    private ShopAiAnalysisVo.Insight priorityInsight(AnalysisContext context) {
        if (!context.lowStocks().isEmpty()) {
            return insight("priority", "今天先处理库存预警", "当前有 " + context.lowStocks().size()
                + " 个商品低于预警值，先核对 " + lowStockText(context) + "，避免顾客要买时缺货。", "danger", "去补货", "/shop/purchase");
        }
        if (!context.unsettledCredits().isEmpty()) {
            return insight("priority", "今天先跟进赊账", "未还赊账合计 " + money(context.unpaidTotal())
                + " 元，建议先联系金额较大的客户，能收一笔先登记一笔。", "warning", "处理赊账", "/shop/credit");
        }
        return insight("priority", "今天经营风险较低", "库存和赊账暂时没有明显压力，可以重点维护热销商品陈列并继续正常收银。", "success", "去收银", "/shop/cashier");
    }

    private ShopAiAnalysisVo.Insight replenishmentInsight(AnalysisContext context) {
        if (context.lowStocks().isEmpty()) {
            return insight("stock", "库存状态稳定", "当前没有低于预警值的商品，先保持正常巡检。", "success", "查看库存", "/shop/stock");
        }
        return insight("stock", "优先补货 " + context.lowStocks().size() + " 个商品",
            lowStockText(context) + "。先补日常高频商品，再补利润较高商品，进货后记得做采购入库。", "danger", "去补货", "/shop/purchase");
    }

    private ShopAiAnalysisVo.Insight salesInsight(AnalysisContext context) {
        String topProduct = context.topSales().stream()
            .findFirst()
            .map(item -> item.productName() + "累计售出" + item.quantity() + "件")
            .orElse("暂无销售明细，先完成几笔收银订单再分析热销商品");
        return insight("sales", "热销商品观察", topProduct + "。可把高频商品放到收银台常用区，晚高峰前确认货架和库存都够。", "info", "去收银", "/shop/cashier");
    }

    private ShopAiAnalysisVo.Insight creditInsight(AnalysisContext context) {
        if (context.unsettledCredits().isEmpty()) {
            return insight("credit", "赊账风险较低", "当前没有未结清赊账，可以继续按现有额度管理。", "success", "查看赊账", "/shop/credit");
        }
        return insight("credit", "跟进 " + context.unsettledCredits().size() + " 笔未还赊账", "未还金额合计 " + money(context.unpaidTotal())
            + " 元，今天先联系金额较大或拖得较久的客户，并把已还金额及时登记。", "warning", "处理赊账", "/shop/credit");
    }

    private ShopAiAnalysisVo.Insight purchaseInsight(AnalysisContext context) {
        if (context.lowStocks().isEmpty()) {
            return insight("purchase", "采购节奏正常", "没有紧急补货项，下一次采购可按日常销量安排。", "success", "看报表", "/shop/report");
        }
        String names = context.lowStocks().stream()
            .limit(5)
            .map(stock -> productName(stock.getProductId(), context.productMap()))
            .collect(Collectors.joining("、"));
        return insight("purchase", "生成采购清单建议", "建议采购清单先覆盖：" + names + "，避免热销基础货断档。", "warning", "采购入库", "/shop/purchase");
    }

    private ShopAiAnalysisVo.Insight reportInsight(AnalysisContext context) {
        return insight("report", "复盘今日经营数字", "今日实收 " + money(context.todaySales()) + " 元，今日赊账 "
            + money(context.todayCredit()) + " 元，粗略毛利 " + money(context.grossProfit())
            + " 元。收摊前再看一次报表，确认现金、赊账和库存变化是否对得上。", "info", "看报表", "/shop/report");
    }

    private ShopAiBusinessAnalysisVo.Metric metric(String label, String value, String hint) {
        ShopAiBusinessAnalysisVo.Metric metric = new ShopAiBusinessAnalysisVo.Metric();
        metric.setLabel(label);
        metric.setValue(value);
        metric.setHint(hint);
        return metric;
    }

    private ShopAiBusinessAnalysisVo.Section section(String title, String horizon, String level, String content, List<String> actions) {
        ShopAiBusinessAnalysisVo.Section section = new ShopAiBusinessAnalysisVo.Section();
        section.setTitle(title);
        section.setHorizon(horizon);
        section.setLevel(level);
        section.setContent(content);
        section.getActions().addAll(actions);
        return section;
    }

    private ShopAiBusinessAnalysisVo.Metric toMetric(Map<?, ?> data) {
        String label = stringValue(data.get("label"));
        String value = stringValue(data.get("value"));
        if (!StringUtils.hasText(label) || !StringUtils.hasText(value)) {
            return null;
        }
        return metric(label, value, defaultValue(stringValue(data.get("hint")), ""));
    }

    private ShopAiBusinessAnalysisVo.Section toSection(Map<?, ?> data) {
        String title = stringValue(data.get("title"));
        String content = stringValue(data.get("content"));
        if (!StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            return null;
        }
        return section(
            title,
            defaultValue(stringValue(data.get("horizon")), "经营规划"),
            normalizeInsightLevel(stringValue(data.get("level"))),
            content,
            stringListValue(data.get("actions"))
        );
    }

    private ShopAiAnalysisVo.Insight toInsight(Map<?, ?> data) {
        String title = stringValue(data.get("title"));
        String content = stringValue(data.get("content"));
        if (!StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            return null;
        }
        return insight(
            defaultValue(stringValue(data.get("type")), "suggestion"),
            title,
            content,
            normalizeInsightLevel(stringValue(data.get("level"))),
            defaultValue(stringValue(data.get("actionText")), "查看"),
            normalizeActionPath(stringValue(data.get("actionPath")))
        );
    }

    private ShopAiAnalysisVo.Insight insight(String type, String title, String content, String level, String actionText, String actionPath) {
        ShopAiAnalysisVo.Insight item = new ShopAiAnalysisVo.Insight();
        item.setType(type);
        item.setTitle(title);
        item.setContent(content);
        item.setLevel(level);
        item.setActionText(actionText);
        item.setActionPath(actionPath);
        return item;
    }

    private List<ProductSale> topSales(List<ShopOrderItem> orderItems) {
        Map<String, Integer> quantityByName = orderItems.stream()
            .filter(item -> StringUtils.hasText(item.getProductName()))
            .collect(Collectors.groupingBy(ShopOrderItem::getProductName, Collectors.summingInt(item -> item.getQuantity() == null ? 0 : item.getQuantity())));
        return quantityByName.entrySet().stream()
            .map(entry -> new ProductSale(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparingInt(ProductSale::quantity).reversed())
            .limit(5)
            .toList();
    }

    private BigDecimal grossProfit(List<ShopOrderItem> orderItems, Map<Long, ShopProduct> productMap) {
        BigDecimal profit = BigDecimal.ZERO;
        for (ShopOrderItem item : orderItems) {
            ShopProduct product = productMap.get(item.getProductId());
            BigDecimal purchasePrice = product == null || product.getPurchasePrice() == null ? BigDecimal.ZERO : product.getPurchasePrice();
            BigDecimal salePrice = item.getSalePrice() == null ? BigDecimal.ZERO : item.getSalePrice();
            int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
            profit = profit.add(salePrice.subtract(purchasePrice).multiply(BigDecimal.valueOf(quantity)));
        }
        return profit;
    }

    private String lowStockText(AnalysisContext context) {
        if (context.lowStocks().isEmpty()) {
            return "无";
        }
        return context.lowStocks().stream()
            .limit(5)
            .map(stock -> productName(stock.getProductId(), context.productMap()) + "剩余" + stock.getQuantity())
            .collect(Collectors.joining("、"));
    }

    private String topSaleText(AnalysisContext context) {
        if (context.topSales().isEmpty()) {
            return "暂无销售明细";
        }
        return context.topSales().stream()
            .map(item -> item.productName() + "售出" + item.quantity() + "件")
            .collect(Collectors.joining("、"));
    }

    private String productName(Long productId, Map<Long, ShopProduct> productMap) {
        ShopProduct product = productMap.get(productId);
        return product == null ? "商品" + productId : product.getProductName();
    }

    private String riskLevel(int lowStockCount, BigDecimal unpaidTotal) {
        if (lowStockCount >= 3 || unpaidTotal.compareTo(new BigDecimal("500")) >= 0) {
            return "high";
        }
        if (lowStockCount > 0 || unpaidTotal.compareTo(BigDecimal.ZERO) > 0) {
            return "medium";
        }
        return "low";
    }

    private String normalizeRiskLevel(String riskLevel) {
        if ("high".equals(riskLevel) || "medium".equals(riskLevel) || "low".equals(riskLevel)) {
            return riskLevel;
        }
        return "medium";
    }

    private String normalizeInsightLevel(String level) {
        if ("danger".equals(level) || "warning".equals(level) || "success".equals(level) || "info".equals(level)) {
            return level;
        }
        return "info";
    }

    private String normalizeActionPath(String actionPath) {
        if (List.of("/shop/stock", "/shop/purchase", "/shop/cashier", "/shop/credit", "/shop/report").contains(actionPath)) {
            return actionPath;
        }
        return "/shop/report";
    }

    private String extractJson(String content) {
        int begin = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (begin < 0 || end <= begin) {
            throw new IllegalStateException("DeepSeek 未返回 JSON 对象");
        }
        return content.substring(begin, end + 1);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private List<String> stringListValue(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
            .map(this::stringValue)
            .filter(StringUtils::hasText)
            .limit(6)
            .toList();
    }

    private String defaultValue(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private List<String> defaultBusinessSuggestions() {
        return List.of(
            "未来三个月我应该重点优化什么？",
            "哪些商品适合作为长期主推商品？",
            "赊账额度应该怎么控制更稳？",
            "采购计划怎样安排才不压货？",
            "如何判断店里现金流有没有风险？"
        );
    }

    private String normalizeInsightLevelByRisk(String riskLevel) {
        if ("high".equals(riskLevel)) {
            return "danger";
        }
        if ("medium".equals(riskLevel)) {
            return "warning";
        }
        return "success";
    }

    private int cacheSeconds() {
        return aiProperties.getCacheSeconds() == null ? 300 : Math.max(0, aiProperties.getCacheSeconds());
    }

    private String money(BigDecimal value) {
        return value == null ? "0.00" : value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private record AnalysisContext(
        List<ShopOrder> todayOrders,
        List<ShopOrder> orders,
        List<ShopCreditRecord> unsettledCredits,
        List<ShopStock> lowStocks,
        Map<Long, ShopProduct> productMap,
        List<ProductSale> topSales,
        BigDecimal todaySales,
        BigDecimal todayCredit,
        BigDecimal unpaidTotal,
        BigDecimal salesTotal,
        BigDecimal grossProfit
    ) {
    }

    private record ProductSale(String productName, int quantity) {
    }
}
