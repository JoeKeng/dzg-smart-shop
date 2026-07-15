package com.dzg.shop.controller;

import com.dzg.common.core.domain.R;
import com.dzg.shop.domain.vo.ShopAiAnalysisVo;
import com.dzg.shop.service.ShopAiAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/shop")
public class ShopAiAnalysisController {

    private final ShopAiAnalysisService aiAnalysisService;

    @GetMapping("/ai/analysis")
    public R<ShopAiAnalysisVo> analysis() {
        return R.ok(aiAnalysisService.analysis());
    }
}
