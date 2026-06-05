package ru.yandex.practicum.smart.dto;

import ru.yandex.practicum.smart.model.enums.FeatureStatus;

public record FeatureResponse(
        Long id,
        String description,
        String content,
        FeatureStatus status
) {
}