package ru.yandex.practicum.smart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.smart.dto.ChatResponse;
import ru.yandex.practicum.smart.dto.MessageResponse;
import ru.yandex.practicum.smart.dto.SendMessageResponse;
import ru.yandex.practicum.smart.exception.ConflictException;
import ru.yandex.practicum.smart.exception.NotFoundException;
import ru.yandex.practicum.smart.exception.ValidationException;
import ru.yandex.practicum.smart.model.enums.FeatureType;
import ru.yandex.practicum.smart.model.enums.MessageRole;
import ru.yandex.practicum.smart.service.ChatService;
import ru.yandex.practicum.smart.service.FeatureService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    @MockBean
    private FeatureService featureService;

    @Test
    void createChatValidRequestTest() throws Exception {
        when(chatService.createChat(any())).thenReturn(new ChatResponse(1L, "My chat"));

        mockMvc.perform(post("/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"My chat\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("My chat"));
    }

    @Test
    void createChatBlankTitleTest() throws Exception {
        mockMvc.perform(post("/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendMessageBlankContentTest() throws Exception {
        mockMvc.perform(post("/chats/1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendMessageValidRequestTest() throws Exception {
        when(chatService.sendMessage(eq(1L), any())).thenReturn(new SendMessageResponse("Hello back!"));

        mockMvc.perform(post("/chats/1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\": \"Hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello back!"));
    }

    @Test
    void sendMessageChatNotFoundTest() throws Exception {
        when(chatService.sendMessage(eq(99L), any()))
                .thenThrow(new NotFoundException("Chat not found = 99"));

        mockMvc.perform(post("/chats/99/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\": \"Hello\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMessagesHappyPathTest() throws Exception {
        List<MessageResponse> messages = List.of(
                new MessageResponse(1L, MessageRole.USER, "Hello", null),
                new MessageResponse(2L, MessageRole.ASSISTANT, "Hi!", null)
        );
        when(chatService.getMessages(1L)).thenReturn(messages);

        mockMvc.perform(get("/chats/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].content").value("Hello"))
                .andExpect(jsonPath("$[1].content").value("Hi!"));
    }

    @Test
    void getMessagesChatNotFoundTest() throws Exception {
        when(chatService.getMessages(99L))
                .thenThrow(new NotFoundException("Chat not found = 99"));

        mockMvc.perform(get("/chats/99/messages"))
                .andExpect(status().isNotFound());
    }

    @Test
    void generateFeatureConflictTest() throws Exception {
        when(featureService.generate(eq(1L), eq(FeatureType.SQL)))
                .thenThrow(new ConflictException("Chat already has a feature in DRAFT status"));

        mockMvc.perform(post("/chats/1/features/generate")
                        .param("type", "SQL"))
                .andExpect(status().isConflict());
    }

    @Test
    void executeFeatureNotFoundTest() throws Exception {
        when(featureService.execute(eq(99L), any()))
                .thenThrow(new NotFoundException("Feature not found = 99"));

        mockMvc.perform(post("/chats/features/99/execute")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void executeFeatureNotDraftStatusTest() throws Exception {
        when(featureService.execute(eq(1L), any()))
                .thenThrow(new ValidationException("Only DRAFT features can be executed"));

        mockMvc.perform(post("/chats/features/1/execute")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void retryFeatureNotFoundTest() throws Exception {
        when(featureService.retry(99L))
                .thenThrow(new NotFoundException("Feature not found = 99"));

        mockMvc.perform(post("/chats/features/99/retry"))
                .andExpect(status().isNotFound());
    }

    @Test
    void retryFeatureNotFailedStatusTest() throws Exception {
        when(featureService.retry(1L))
                .thenThrow(new ValidationException("Only FAILED features can be retried"));

        mockMvc.perform(post("/chats/features/1/retry"))
                .andExpect(status().isBadRequest());
    }
}