package ru.yandex.practicum.smart.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.smart.dto.MessageResponse;
import ru.yandex.practicum.smart.model.Message;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    MessageResponse toDto(Message message);
}