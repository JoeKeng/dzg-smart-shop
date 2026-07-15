package com.dzg.shop.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ShopAiBusinessQuestionVo {

    private String answer;

    private Boolean generatedByAi = false;

    private String fallbackReason;

    private List<String> suggestions = new ArrayList<>();
}
