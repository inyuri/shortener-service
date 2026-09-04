package com.timofey.shortener_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient analyticsWebClient(
            @Value("${analytics.service.url}") String url
    ) {
        return WebClient.builder()
                .baseUrl(url)
                .build();
    }
}