package ru.yandex.practicum.smart.dto;

import ru.yandex.practicum.smart.model.enums.MessageRole;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        MessageRole role,
        String content,
        LocalDateTime createdAt
) {
}