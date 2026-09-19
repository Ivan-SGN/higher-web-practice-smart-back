package ru.yandex.practicum.smart.dto;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
        @NotBlank String content
) {
}