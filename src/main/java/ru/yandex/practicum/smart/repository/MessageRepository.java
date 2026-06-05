package ru.yandex.practicum.smart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.smart.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
}