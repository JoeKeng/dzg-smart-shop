package com.dzg.shop.controller;

import com.dzg.common.core.domain.R;
import com.dzg.shop.service.ShopNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class ShopNotificationController {

    private final ShopNotificationService notificationService;

    @GetMapping("/notification/list")
    public R<?> notificationList() {
        return R.ok(notificationService.listNotifications());
    }

    @PutMapping("/notification/{noticeId}/status/{status}")
    public R<Void> updateNotificationStatus(@PathVariable Long noticeId, @PathVariable String status) {
        notificationService.updateStatus(noticeId, status);
        return R.ok();
    }

    @PutMapping("/notification/read-all")
    public R<Void> readAllNotifications() {
        notificationService.readAll();
        return R.ok();
    }
}
