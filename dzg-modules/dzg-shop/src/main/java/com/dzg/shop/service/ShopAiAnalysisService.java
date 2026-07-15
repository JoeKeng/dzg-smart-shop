package com.dzg.shop.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dzg.shop.domain.ShopCreditRecord;
import com.dzg.shop.domain.ShopOrder;
import com.dzg.shop.domain.ShopOrderItem;
import com.dzg.shop.domain.ShopProduct;
import com.dzg.shop.domain.ShopStock;
import com.dzg.shop.domain.vo.ShopAiAnalysisVo;
import com.dzg.shop.mapper.ShopCreditRecordMapper;
import com.dzg.shop.mapper.ShopOrderItemMapper;
import com.dzg.shop.mapper.ShopOrderMapper;
import com.dzg.shop.mapper.ShopProductMapper;
import com.dzg.shop.mapper.ShopStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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

    private final ShopOrderMapper orderMapper;
    private final ShopOrderItemMapper orderItemMapper;
    private final ShopCreditRecordMapper creditRecordMapper;
    private final ShopStockMapper stockMapper;
    private final ShopProductMapper productMapper;

    public ShopAiAnalysisVo analysis() {
        Date todayBegin = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<ShopOrder> todayOrders = orderMapper.selectList(Wrappers.<ShopOrder>lambdaQuery().ge(ShopOrder::getOrderTime, todayBegin));
        List<ShopOrderItem> orderItems = orderItemMapper.selectList(Wrappers.lambdaQuery());
        List<ShopCreditRecord> unsettledCredits = creditRecordMapper.selectList(
            Wrappers.<ShopCreditRecord>lambdaQuery().ne(ShopCreditRecord::getStatus, ShopConstants.SETTLED)
        );
        List<ShopStock> stocks = stockMapper.selectList(Wrappers.lambdaQuery());
        Map<Long, ShopProduct> productMap = productMapper.selectList(Wrappers.lambdaQuery())
            .stream()
            .collect(Collectors.toMap(ShopProduct::getProductId, Function.identity(), (left, right) -> left));

        BigDecimal todaySales = todayOrders.stream()
            .map(ShopOrder::getPaidAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal unpaidTotal = unsettledCredits.stream()
            .map(ShopCreditRecord::getUnpaidAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<ShopStock> lowStocks = stocks.stream()
            .filter(stock -> stock.getQuantity() != null && stock.getWarningQty() != null && stock.getQuantity() <= stock.getWarningQty())
            .toList();

        ShopAiAnalysisVo vo = new ShopAiAnalysisVo();
        vo.setRiskLevel(riskLevel(lowStocks.size(), unpaidTotal));
        vo.setSummary("今日完成 " + todayOrders.size() + " 笔订单，实收 " + money(todaySales) + " 元；当前有 "
            + lowStocks.size() + " 个库存预警，未还赊账 " + money(unpaidTotal) + " 元。");
        vo.getInsights().add(replenishmentInsight(lowStocks, productMap));
        vo.getInsights().add(salesInsight(orderItems));
        vo.getInsights().add(creditInsight(unsettledCredits, unpaidTotal));
        vo.getInsights().add(purchaseInsight(lowStocks, productMap));
        return vo;
    }

    private ShopAiAnalysisVo.Insight replenishmentInsight(List<ShopStock> lowStocks, Map<Long, ShopProduct> productMap) {
        if (lowStocks.isEmpty()) {
            return insight("stock", "库存状态稳定", "当前没有低于预警值的商品，先保持正常巡检。", "success", "查看库存", "/shop/stock");
        }
        String products = lowStocks.stream()
            .limit(3)
            .map(stock -> productName(stock.getProductId(), productMap) + "剩余" + stock.getQuantity())
            .collect(Collectors.joining("、"));
        return insight("stock", "优先补货 " + lowStocks.size() + " 个商品", products + "，建议先安排采购或临时补货。", "danger", "去补货", "/shop/purchase");
    }

    private ShopAiAnalysisVo.Insight salesInsight(List<ShopOrderItem> orderItems) {
        Map<String, Integer> quantityByName = orderItems.stream()
            .filter(item -> item.getProductName() != null)
            .collect(Collectors.groupingBy(ShopOrderItem::getProductName, Collectors.summingInt(item -> item.getQuantity() == null ? 0 : item.getQuantity())));
        String topProduct = quantityByName.entrySet().stream()
            .max(Comparator.comparingInt(Map.Entry::getValue))
            .map(entry -> entry.getKey() + "累计售出" + entry.getValue() + "件")
            .orElse("暂无销售明细，先完成几笔收银订单再分析热销商品");
        return insight("sales", "热销商品观察", topProduct + "。可把高频商品放到收银台常用区并优先保证库存。", "info", "去收银", "/shop/cashier");
    }

    private ShopAiAnalysisVo.Insight creditInsight(List<ShopCreditRecord> unsettledCredits, BigDecimal unpaidTotal) {
        if (unsettledCredits.isEmpty()) {
            return insight("credit", "赊账风险较低", "当前没有未结清赊账，可以继续按现有额度管理。", "success", "查看赊账", "/shop/credit");
        }
        return insight("credit", "跟进 " + unsettledCredits.size() + " 笔未还赊账", "未还金额合计 " + money(unpaidTotal)
            + " 元，建议今天先联系金额较大的客户并登记还款。", "warning", "处理赊账", "/shop/credit");
    }

    private ShopAiAnalysisVo.Insight purchaseInsight(List<ShopStock> lowStocks, Map<Long, ShopProduct> productMap) {
        if (lowStocks.isEmpty()) {
            return insight("purchase", "采购节奏正常", "没有紧急补货项，下一次采购可按日常销量安排。", "success", "看报表", "/shop/report");
        }
        String names = lowStocks.stream()
            .limit(5)
            .map(stock -> productName(stock.getProductId(), productMap))
            .collect(Collectors.joining("、"));
        return insight("purchase", "生成采购清单建议", "建议采购清单先覆盖：" + names + "，避免热销基础货断档。", "warning", "采购入库", "/shop/purchase");
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

    private String money(BigDecimal value) {
        return value == null ? "0.00" : value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
