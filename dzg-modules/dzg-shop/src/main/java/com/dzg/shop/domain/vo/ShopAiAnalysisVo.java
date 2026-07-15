package com.dzg.shop.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ShopAiAnalysisVo {

    private String summary;

    private String riskLevel;

    private List<Insight> insights = new ArrayList<>();

    @Data
    public static class Insight {

        private String type;

        private String title;

        private String content;

        private String level;

        private String actionText;

        private String actionPath;
    }
}
