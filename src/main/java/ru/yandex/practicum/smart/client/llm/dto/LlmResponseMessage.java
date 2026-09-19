package ru.yandex.practicum.smart.client.llm.dto;

public record LlmResponseMessage(
        String role,
        String content
) {
}
