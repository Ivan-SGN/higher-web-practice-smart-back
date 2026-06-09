package ru.yandex.practicum.smart.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.smart.client.llm.LlmClient;
import ru.yandex.practicum.smart.client.llm.dto.LlmResponse;
import ru.yandex.practicum.smart.client.llm.dto.LlmResponseMessage;
import ru.yandex.practicum.smart.dto.ChatResponse;
import ru.yandex.practicum.smart.dto.CreateChatRequest;
import ru.yandex.practicum.smart.dto.MessageResponse;
import ru.yandex.practicum.smart.dto.SendMessageRequest;
import ru.yandex.practicum.smart.dto.SendMessageResponse;
import ru.yandex.practicum.smart.exception.NotFoundException;
import ru.yandex.practicum.smart.mapper.ChatMapper;
import ru.yandex.practicum.smart.mapper.MessageMapper;
import ru.yandex.practicum.smart.model.Chat;
import ru.yandex.practicum.smart.model.Message;
import ru.yandex.practicum.smart.model.enums.MessageRole;
import ru.yandex.practicum.smart.repository.ChatRepository;
import ru.yandex.practicum.smart.repository.MessageRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private LlmClient llmClient;
    @Mock private ChatRepository chatRepository;
    @Mock private ChatMapper chatMapper;
    @Mock private MessageMapper messageMapper;
    @Mock private MessageRepository messageRepository;

    @InjectMocks
    private ChatService chatService;

    @Test
    void createChatHappyPathTest() {
        CreateChatRequest request = new CreateChatRequest("My chat");
        Chat chat = createChat();
        when(chatMapper.toEntity(request)).thenReturn(chat);
        when(chatRepository.save(chat)).thenReturn(chat);
        when(chatMapper.toDto(chat)).thenReturn(new ChatResponse(1L, "My chat"));

        ChatResponse result = chatService.createChat(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("My chat");
    }

    @Test
    void sendMessageHappyPathTest() {
        Chat chat = createChat();
        Message userMessage = createMessage(chat, MessageRole.USER, "Hello");
        LlmResponse llmResponse = createLlmResponse("Hi there!");
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(messageRepository.findAllByChatIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(userMessage));
        when(messageMapper.toEntity(any(), any(), any())).thenReturn(userMessage);
        when(messageMapper.toLlmMessage(any())).thenReturn(null);
        when(llmClient.send(any())).thenReturn(llmResponse);

        SendMessageResponse result = chatService.sendMessage(1L, new SendMessageRequest("Hello"));

        assertThat(result.content()).isEqualTo("Hi there!");
        verify(messageRepository, times(2)).save(any());
    }

    @Test
    void sendMessageChatNotFoundTest() {
        when(chatRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.sendMessage(99L, new SendMessageRequest("Hello")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getMessagesHappyPathTest() {
        Chat chat = createChat();
        Message message = createMessage(chat, MessageRole.USER, "Hello");
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(messageRepository.findAllByChatIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(message));
        when(messageMapper.toDto(message)).thenReturn(new MessageResponse(1L, MessageRole.USER, "Hello", null));

        List<MessageResponse> result = chatService.getMessages(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).content()).isEqualTo("Hello");
    }

    @Test
    void getMessagesChatNotFoundTest() {
        when(chatRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getMessages(99L))
                .isInstanceOf(NotFoundException.class);
    }

    private Chat createChat() {
        Chat chat = new Chat();
        chat.setId(1L);
        chat.setTitle("My chat");
        return chat;
    }

    private Message createMessage(Chat chat, MessageRole role, String content) {
        Message message = new Message();
        message.setId(1L);
        message.setChat(chat);
        message.setRole(role);
        message.setContent(content);
        return message;
    }

    private LlmResponse createLlmResponse(String content) {
        return new LlmResponse("qwen3:1.7b", "2026-01-01T00:00:00Z",
                new LlmResponseMessage("assistant", content), true);
    }
}