package com.dzg.shop.service;

import com.dzg.shop.config.ShopAiProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DeepSeekAnalysisClient {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ShopAiProperties aiProperties;
    private final ObjectMapper objectMapper;

    public String completeJson(String prompt) {
        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            throw new IllegalStateException("DeepSeek API Key 未配置");
        }
        try {
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(timeout())
                .build();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(chatUrl()))
                .timeout(timeout())
                .header("Authorization", "Bearer " + aiProperties.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody(prompt)))
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("DeepSeek 返回 HTTP " + response.statusCode());
            }
            return extractContent(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("DeepSeek 调用被中断", e);
        } catch (IOException e) {
            throw new IllegalStateException("DeepSeek 调用失败", e);
        }
    }

    private String requestBody(String prompt) throws JsonProcessingException {
        Map<String, Object> body = Map.of(
            "model", aiProperties.getModel(),
            "temperature", 0.2,
            "stream", false,
            "response_format", Map.of("type", "json_object"),
            "messages", List.of(
                Map.of("role", "system", "content", "你是面向乡镇小店老板的经营分析助手。只返回 JSON，不要返回 Markdown。"),
                Map.of("role", "user", "content", prompt)
            )
        );
        return objectMapper.writeValueAsString(body);
    }

    @SuppressWarnings("unchecked")
    private String extractContent(String body) throws JsonProcessingException {
        Map<String, Object> root = objectMapper.readValue(body, MAP_TYPE);
        Object choicesValue = root.get("choices");
        if (!(choicesValue instanceof List<?> choices) || choices.isEmpty()) {
            throw new IllegalStateException("DeepSeek 响应缺少 choices");
        }
        Object first = choices.get(0);
        if (!(first instanceof Map<?, ?> choice)) {
            throw new IllegalStateException("DeepSeek 响应格式异常");
        }
        Object messageValue = choice.get("message");
        if (!(messageValue instanceof Map<?, ?> message)) {
            throw new IllegalStateException("DeepSeek 响应缺少 message");
        }
        Object content = message.get("content");
        if (!(content instanceof String text) || !StringUtils.hasText(text)) {
            throw new IllegalStateException("DeepSeek 响应内容为空");
        }
        return text;
    }

    private String chatUrl() {
        String baseUrl = aiProperties.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            return baseUrl + "chat/completions";
        }
        return baseUrl + "/chat/completions";
    }

    private Duration timeout() {
        int seconds = aiProperties.getTimeoutSeconds() == null ? 8 : aiProperties.getTimeoutSeconds();
        return Duration.ofSeconds(Math.max(1, seconds));
    }
}
