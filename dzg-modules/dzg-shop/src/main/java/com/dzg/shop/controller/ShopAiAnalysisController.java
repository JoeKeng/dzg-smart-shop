package com.dzg.shop.controller;

import com.dzg.common.core.domain.R;
import com.dzg.shop.domain.bo.ShopAiChatBo;
import com.dzg.shop.domain.vo.ShopAiAnalysisVo;
import com.dzg.shop.domain.vo.ShopAiBusinessAnalysisVo;
import com.dzg.shop.domain.vo.ShopAiBusinessQuestionVo;
import com.dzg.shop.service.ShopAiAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ShopAiAnalysisController {

    private final ShopAiAnalysisService aiAnalysisService;

    @GetMapping("/ai/analysis")
    public R<ShopAiAnalysisVo> analysis(@RequestParam(defaultValue = "false") boolean force) {
        return R.ok(aiAnalysisService.analysis(force));
    }

    @GetMapping("/ai/business-analysis")
    public R<ShopAiBusinessAnalysisVo> businessAnalysis(@RequestParam(defaultValue = "false") boolean force) {
        return R.ok(aiAnalysisService.businessAnalysis(force));
    }

    @PostMapping("/ai/business-analysis/chat")
    public R<ShopAiBusinessQuestionVo> businessQuestion(@RequestBody ShopAiChatBo bo) {
        return R.ok(aiAnalysisService.businessQuestion(bo));
    }
}
