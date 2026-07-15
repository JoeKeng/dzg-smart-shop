package com.dzg.shop.controller;

import com.dzg.common.core.domain.R;
import com.dzg.shop.domain.vo.ShopReportVo;
import com.dzg.shop.service.ShopReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ShopReportController {

    private final ShopReportService reportService;

    @GetMapping("/report/summary")
    public R<ShopReportVo> report(@RequestParam(defaultValue = "today") String range) {
        return R.ok(reportService.report(range));
    }
}
