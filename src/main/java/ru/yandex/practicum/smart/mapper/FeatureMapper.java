package ru.yandex.practicum.smart.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.smart.dto.FeatureResponse;
import ru.yandex.practicum.smart.model.Chat;
import ru.yandex.practicum.smart.model.Feature;

@Mapper(componentModel = "spring")
public interface FeatureMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "description", ignore = true)
    Feature toEntity(Chat chat, String content);

    FeatureResponse toDto(Feature feature);

}