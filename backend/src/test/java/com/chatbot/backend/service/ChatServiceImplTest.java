package com.chatbot.backend.service;

import com.chatbot.backend.client.WeatherClient;
import com.chatbot.backend.dto.ChatMessage;
import com.chatbot.backend.dto.ChatResponse;
import com.chatbot.backend.repository.GreetingMessageRepository;
import com.chatbot.backend.service.Impl.ChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

public class ChatServiceImplTest {

    private WeatherClient weatherClient;
    private GreetingMessageRepository greetingMessageRepository;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        weatherClient = Mockito.mock(WeatherClient.class);
        greetingMessageRepository = Mockito.mock(GreetingMessageRepository.class);
        chatService = new ChatServiceImpl(weatherClient, greetingMessageRepository);
    }

    @Test
    void shouldReturnGreetingMessage_WhenUserJoin() {
        // Arrange
        ChatMessage joinMessage = new ChatMessage();
        joinMessage.setSender("Alice");
        joinMessage.setType("JOIN");
        joinMessage.setTimezone("Asia/Bangkok");

        when(weatherClient.getCurrentWeather()).thenReturn("Sunny");
        when(greetingMessageRepository.getGreetingMessage("Sunny", "morning"))
                .thenReturn("Good morning, on a sunshine day!");

        // Act
        ChatResponse response = chatService.handleMessage(joinMessage);

        // Assert
        assertThat(response.getContent()).contains("Good morning, on a sunshine day!");
    }



    @Test
    void shouldReturnEchoMessage_WhenNoIntentMatched() {
        // Arrange
        ChatMessage msg = new ChatMessage();
        msg.setSender("Alice");
        msg.setType("CHAT");
        msg.setContent("สวัสดีครับ");

        // Act
        ChatResponse response = chatService.handleMessage(msg);

        // Assert
        assertThat(response.getContent()).contains("คุณพูดว่า: สวัสดีครับ");
    }
}
