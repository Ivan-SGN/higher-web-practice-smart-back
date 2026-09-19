package ru.yandex.practicum.smart.dto;

import ru.yandex.practicum.smart.model.enums.FeatureStatus;
import ru.yandex.practicum.smart.model.enums.FeatureType;

import java.util.List;

public record FeatureResponse(
        Long id,
        FeatureType type,
        String description,
        String content,
        FeatureStatus status,
        List<String> parameters,
        List<String> results,
        String errorMessage
) {
}