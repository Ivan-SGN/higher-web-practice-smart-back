package ru.yandex.practicum.smart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.smart.model.Chat;

public interface ChatRepository extends JpaRepository<Chat, Long> {
}