package com.dzg.shop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "shop.ai")
public class ShopAiProperties {

    private Boolean enabled = true;

    private String provider = "deepseek";

    private String baseUrl = "https://api.deepseek.com";

    private String apiKey;

    private String model = "deepseek-v4-flash";

    private Integer timeoutSeconds = 8;

    private Integer cacheSeconds = 300;
}
