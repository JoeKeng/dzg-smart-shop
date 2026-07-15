package com.dzg.shop.controller;

import com.dzg.common.core.domain.R;
import com.dzg.shop.domain.vo.ShopDashboardVo;
import com.dzg.shop.domain.vo.ShopPaymentSummaryVo;
import com.dzg.shop.service.ShopReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ShopDashboardController {

    private final ShopReportService reportService;

    @GetMapping("/dashboard")
    public R<ShopDashboardVo> dashboard() {
        return R.ok(reportService.dashboard());
    }

    @GetMapping("/dashboard/payment-summary")
    public R<ShopPaymentSummaryVo> paymentSummary(@RequestParam(defaultValue = "today") String range) {
        return R.ok(reportService.paymentSummary(range));
    }
}
