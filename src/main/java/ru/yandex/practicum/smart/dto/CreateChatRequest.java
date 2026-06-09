package ru.yandex.practicum.smart.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateChatRequest(
        @NotBlank String title
) {
}