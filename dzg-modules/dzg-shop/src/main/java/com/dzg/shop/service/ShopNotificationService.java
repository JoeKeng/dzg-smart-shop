package com.dzg.shop.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dzg.common.core.constant.TenantConstants;
import com.dzg.shop.domain.ShopCreditRecord;
import com.dzg.shop.domain.ShopNotification;
import com.dzg.shop.domain.ShopStock;
import com.dzg.shop.domain.vo.ShopAiAnalysisVo;
import com.dzg.shop.mapper.ShopCreditRecordMapper;
import com.dzg.shop.mapper.ShopNotificationMapper;
import com.dzg.shop.mapper.ShopStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class ShopNotificationService {

    private final ShopStockMapper stockMapper;
    private final ShopCreditRecordMapper creditRecordMapper;
    private final ShopNotificationMapper notificationMapper;
    private final ShopAiAnalysisService aiAnalysisService;

    @Transactional(rollbackFor = Exception.class)
    public List<ShopNotification> listNotifications() {
        Map<Long, ShopNotification> notices = new LinkedHashMap<>();
        notificationMapper.selectList(Wrappers.<ShopNotification>lambdaQuery()
                .orderByAsc(ShopNotification::getStatus)
                .orderByDesc(ShopNotification::getUpdateTime)
                .orderByDesc(ShopNotification::getCreateTime))
            .forEach(item -> addNotice(notices, item));

        for (ShopStock stock : stockMapper.selectList()) {
            if (stock.getQuantity() != null && stock.getWarningQty() != null && stock.getQuantity() <= stock.getWarningQty()) {
                addNotice(notices, resolveNotice("stock", "库存预警", "有商品库存低于预警值，请及时补货", "stock", stock.getProductId()));
            }
        }
        for (ShopCreditRecord credit : creditRecordMapper.selectList(Wrappers.<ShopCreditRecord>lambdaQuery().ne(ShopCreditRecord::getStatus, ShopConstants.SETTLED))) {
            addNotice(notices, resolveNotice("credit_unpaid", "赊账未还", "有客户赊账尚未结清，请记得提醒还款", "credit", credit.getCreditId()));
        }

        ShopAiAnalysisVo analysis = aiAnalysisService.analysis();
        if (analysis.getRiskLevel() != null && !"low".equals(analysis.getRiskLevel())) {
            ShopAiAnalysisVo.Insight insight = (analysis.getInsights() == null ? Collections.<ShopAiAnalysisVo.Insight>emptyList() : analysis.getInsights()).stream()
                .filter(item -> "danger".equals(item.getLevel()) || "warning".equals(item.getLevel()))
                .findFirst()
                .orElse(null);
            if (insight == null) {
                addNotice(notices, resolveNotice("suggestion", "经营建议", analysis.getSummary(), "report", 0L));
            } else {
                addNotice(notices, resolveNotice("suggestion", insight.getTitle(), insight.getContent(), "report", 0L));
            }
        }

        return notices.values().stream()
            .sorted(Comparator.comparing(ShopNotification::getStatus, Comparator.nullsLast(String::compareTo)))
            .toList();
    }

    public void updateStatus(Long noticeId, String status) {
        if (noticeId == null) {
            return;
        }
        ShopNotification notice = new ShopNotification();
        notice.setNoticeId(noticeId);
        notice.setStatus("1".equals(status) ? "1" : "0");
        notificationMapper.updateById(notice);
    }

    @Transactional(rollbackFor = Exception.class)
    public void readAll() {
        listNotifications();
        notificationMapper.update(null, Wrappers.<ShopNotification>lambdaUpdate()
            .set(ShopNotification::getStatus, "1")
            .eq(ShopNotification::getStatus, "0"));
    }

    private ShopNotification resolveNotice(String type, String title, String content, String bizType, Long bizId) {
        ShopNotification existed = notificationMapper.selectOne(Wrappers.<ShopNotification>lambdaQuery()
            .eq(ShopNotification::getNoticeType, type)
            .eq(ShopNotification::getBizType, bizType)
            .eq(ShopNotification::getBizId, bizId)
            .last("limit 1"));
        if (existed != null) {
            return existed;
        }
        ShopNotification notice = new ShopNotification();
        notice.setTenantId(TenantConstants.DEFAULT_TENANT_ID);
        notice.setNoticeType(type);
        notice.setNoticeTitle(title);
        notice.setNoticeContent(content);
        notice.setBizType(bizType);
        notice.setBizId(bizId);
        notice.setStatus("0");
        notice.setDelFlag(ShopConstants.NORMAL);
        notificationMapper.insert(notice);
        return notice;
    }

    private void addNotice(Map<Long, ShopNotification> notices, ShopNotification notice) {
        if (notice != null && notice.getNoticeId() != null) {
            notices.putIfAbsent(notice.getNoticeId(), notice);
        }
    }
}
