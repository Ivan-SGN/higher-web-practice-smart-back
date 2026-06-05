package ru.yandex.practicum.smart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.smart.client.llm.LlmClient;
import ru.yandex.practicum.smart.client.llm.dto.LlmResponse;
import ru.yandex.practicum.smart.dto.ChatResponse;
import ru.yandex.practicum.smart.dto.CreateChatRequest;
import ru.yandex.practicum.smart.dto.SendMessageResponse;
import ru.yandex.practicum.smart.dto.SendMessageRequest;
import ru.yandex.practicum.smart.dto.MessageResponse;

import java.util.List;
import ru.yandex.practicum.smart.model.Message;
import ru.yandex.practicum.smart.repository.MessageRepository;
import ru.yandex.practicum.smart.client.llm.dto.LlmMessage;
import ru.yandex.practicum.smart.model.enums.MessageRole;
import ru.yandex.practicum.smart.exception.NotFoundException;
import ru.yandex.practicum.smart.mapper.ChatMapper;
import ru.yandex.practicum.smart.mapper.MessageMapper;
import ru.yandex.practicum.smart.model.Chat;
import ru.yandex.practicum.smart.repository.ChatRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final LlmClient llmClient;
    private final ChatRepository chatRepository;
    private final ChatMapper chatMapper;
    private final MessageMapper messageMapper;
    private final MessageRepository messageRepository;

    public SendMessageResponse sendMessage(Long chatId, SendMessageRequest request) {
        Chat chat = getChatOrThrow(chatId);
        saveMessage(chat, MessageRole.USER, request.content());
        LlmResponse response = sendToLlm(chatId);
        saveMessage(chat, MessageRole.ASSISTANT, response.message().content());
        log.info("Message processed chatId={}", chatId);
        return new SendMessageResponse(response.message().content());
    }

    public ChatResponse createChat(CreateChatRequest request) {
        Chat chat = chatMapper.toEntity(request);
        Chat savedChat = chatRepository.save(chat);
        log.info("Chat created id={}", savedChat.getId());
        return chatMapper.toDto(savedChat);
    }

    public List<MessageResponse> getMessages(Long chatId) {
        getChatOrThrow(chatId);

        return messageRepository.findAllByChatIdOrderByCreatedAtAsc(chatId)
                .stream()
                .map(messageMapper::toDto)
                .toList();
    }

    private Chat getChatOrThrow(Long chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> {
                    log.warn("Chat not found id={}", chatId);
                    return new NotFoundException("Chat not found = " + chatId);
                });
    }

    private void saveMessage(Chat chat, MessageRole role, String content) {
        Message message = messageMapper.toEntity(chat, role, content);
        messageRepository.save(message);
    }

    private LlmResponse sendToLlm(Long chatId) {
        List<LlmMessage> messages = getChatHistory(chatId);
        return llmClient.send(messages);
    }

    private List<LlmMessage> getChatHistory(Long chatId) {
        return messageRepository.findAllByChatIdOrderByCreatedAtAsc(chatId)
                .stream()
                .map(messageMapper::toLlmMessage)
                .toList();
    }
}