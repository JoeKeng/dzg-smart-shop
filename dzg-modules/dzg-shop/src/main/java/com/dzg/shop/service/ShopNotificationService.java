package com.dzg.shop.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dzg.shop.domain.ShopCreditRecord;
import com.dzg.shop.domain.ShopNotification;
import com.dzg.shop.domain.ShopStock;
import com.dzg.shop.mapper.ShopCreditRecordMapper;
import com.dzg.shop.mapper.ShopNotificationMapper;
import com.dzg.shop.mapper.ShopStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ShopNotificationService {

    private final ShopStockMapper stockMapper;
    private final ShopCreditRecordMapper creditRecordMapper;
    private final ShopNotificationMapper notificationMapper;

    public List<ShopNotification> listNotifications() {
        List<ShopNotification> notices = new ArrayList<>();
        for (ShopNotification item : notificationMapper.selectList(Wrappers.<ShopNotification>lambdaQuery().eq(ShopNotification::getStatus, "0"))) {
            notices.add(item);
        }
        for (ShopStock stock : stockMapper.selectList()) {
            if (stock.getQuantity() != null && stock.getWarningQty() != null && stock.getQuantity() <= stock.getWarningQty()) {
                notices.add(buildNotice("stock", "库存预警", "有商品库存低于预警值，请及时补货", "stock", stock.getProductId()));
            }
        }
        for (ShopCreditRecord credit : creditRecordMapper.selectList(Wrappers.<ShopCreditRecord>lambdaQuery().ne(ShopCreditRecord::getStatus, ShopConstants.SETTLED))) {
            notices.add(buildNotice("credit", "赊账未还", "有客户赊账尚未结清，请记得提醒还款", "credit", credit.getCreditId()));
        }
        return notices;
    }

    private ShopNotification buildNotice(String type, String title, String content, String bizType, Long bizId) {
        ShopNotification notice = new ShopNotification();
        notice.setNoticeType(type);
        notice.setNoticeTitle(title);
        notice.setNoticeContent(content);
        notice.setBizType(bizType);
        notice.setBizId(bizId);
        notice.setStatus("0");
        return notice;
    }
}
