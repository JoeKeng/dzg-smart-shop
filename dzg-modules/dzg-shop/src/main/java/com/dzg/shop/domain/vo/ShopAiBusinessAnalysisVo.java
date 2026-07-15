package com.dzg.shop.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ShopAiBusinessAnalysisVo {

    private String summary;

    private String riskLevel;

    private String provider;

    private String model;

    private Boolean generatedByAi = false;

    private String fallbackReason;

    private LocalDateTime analysisTime;

    private List<Metric> metrics = new ArrayList<>();

    private List<Section> sections = new ArrayList<>();

    private List<String> suggestions = new ArrayList<>();

    @Data
    public static class Metric {

        private String label;

        private String value;

        private String hint;
    }

    @Data
    public static class Section {

        private String title;

        private String horizon;

        private String level;

        private String content;

        private List<String> actions = new ArrayList<>();
    }
}
