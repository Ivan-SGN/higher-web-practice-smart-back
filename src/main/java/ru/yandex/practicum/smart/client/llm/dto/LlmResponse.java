package ru.yandex.practicum.smart.client.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LlmResponse(
        String model,

        @JsonProperty("created_at")
        String createdAt,

        LlmResponseMessage message,

        boolean done
) {
}