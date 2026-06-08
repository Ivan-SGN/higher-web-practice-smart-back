package ru.yandex.practicum.smart.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.smart.dto.FeatureResponse;
import ru.yandex.practicum.smart.dto.GeneratedFeature;
import ru.yandex.practicum.smart.model.Chat;
import ru.yandex.practicum.smart.model.Feature;

@Mapper(componentModel = "spring")
public interface FeatureMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chat", source = "chat")
    @Mapping(target = "content", source = "generatedFeature.code")
    @Mapping(target = "description", source = "generatedFeature.description")
    @Mapping(target = "type", source = "generatedFeature.type")
    @Mapping(target = "parameters", source = "generatedFeature.parameters")
    @Mapping(target = "results", source = "generatedFeature.results")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Feature toEntity(Chat chat, GeneratedFeature generatedFeature);

    FeatureResponse toDto(Feature feature);
}