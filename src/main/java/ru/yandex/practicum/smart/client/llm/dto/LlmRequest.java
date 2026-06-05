package ru.yandex.practicum.smart.client.llm.dto;

import java.util.List;

public record LlmRequest(
        String model,
        List<LlmMessage> messages,
        boolean stream
) {
}