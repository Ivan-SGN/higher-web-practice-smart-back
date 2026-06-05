package ru.yandex.practicum.smart.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.smart.dto.MessageResponse;
import ru.yandex.practicum.smart.model.Message;
import ru.yandex.practicum.smart.client.llm.dto.LlmMessage;
import ru.yandex.practicum.smart.model.Chat;
import ru.yandex.practicum.smart.model.enums.MessageRole;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    MessageResponse toDto(Message message);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chat", source = "chat")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "createdAt", ignore = true)
    Message toEntity(Chat chat, MessageRole role, String content);

    @Mapping(target = "role", expression = "java(message.getRole().name().toLowerCase())")
    @Mapping(target = "content", source = "content")
    LlmMessage toLlmMessage(Message message);
}