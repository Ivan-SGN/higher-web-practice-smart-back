package ru.yandex.practicum.smart.client.llm.dto;

public record LlmMessage(
        String role,
        String content
) {
}