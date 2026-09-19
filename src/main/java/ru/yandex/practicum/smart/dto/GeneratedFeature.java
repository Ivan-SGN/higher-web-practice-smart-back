package ru.yandex.practicum.smart.dto;

import ru.yandex.practicum.smart.model.enums.FeatureType;

import java.util.List;

public record GeneratedFeature(
        FeatureType type,
        String description,
        String code,
        List<String> parameters,
        List<String> results
) {
}