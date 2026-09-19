package ru.yandex.practicum.smart.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.smart.dto.ChatResponse;
import ru.yandex.practicum.smart.dto.CreateChatRequest;
import ru.yandex.practicum.smart.model.Chat;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    Chat toEntity(CreateChatRequest request);

    ChatResponse toDto(Chat chat);
}