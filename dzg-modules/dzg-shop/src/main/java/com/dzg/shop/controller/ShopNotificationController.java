package com.dzg.shop.controller;

import com.dzg.common.core.domain.R;
import com.dzg.shop.service.ShopNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ShopNotificationController {

    private final ShopNotificationService notificationService;

    @GetMapping("/notification/list")
    public R<?> notificationList() {
        return R.ok(notificationService.listNotifications());
    }
}
