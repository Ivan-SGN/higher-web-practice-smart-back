package ru.yandex.practicum.smart.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm")
public record LlmProperties(
        String baseUrl,
        String model
) {
}