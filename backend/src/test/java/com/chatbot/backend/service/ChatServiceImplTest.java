package com.chatbot.backend.service;

import com.chatbot.backend.client.CoreSystemClient;
import com.chatbot.backend.client.WeatherClient;
import com.chatbot.backend.dto.ChatMessage;
import com.chatbot.backend.dto.ChatResponse;
import com.chatbot.backend.model.ConversationContext;
import com.chatbot.backend.model.UserAccountData;
import com.chatbot.backend.repository.GreetingMessageRepository;
import com.chatbot.backend.service.Impl.ChatServiceImpl;
import com.chatbot.backend.util.ConversationManager;
import com.chatbot.backend.util.constant.ConversationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ChatServiceImplTest {

    private WeatherClient weatherClient;
    private GreetingMessageRepository greetingMessageRepository;
    private CoreSystemClient coreSystemClient;
    private ConversationManager conversationManager;
    private ChatServiceImpl chatService;
    private IntentDetectionService intentDetectionService;

    @BeforeEach
    void setUp() {
        weatherClient = Mockito.mock(WeatherClient.class);
        greetingMessageRepository = Mockito.mock(GreetingMessageRepository.class);
        coreSystemClient = Mockito.mock(CoreSystemClient.class);
        conversationManager = Mockito.mock(ConversationManager.class);
        intentDetectionService = Mockito.mock(IntentDetectionService.class);

        chatService = new ChatServiceImpl(
                weatherClient,
                greetingMessageRepository,
                coreSystemClient,
                conversationManager,
                intentDetectionService
        );
    }

    @Test
    void shouldHandleOverdueCase() {
        // Arrange
        ChatMessage join = new ChatMessage();
        join.setSender("Alice");
        join.setType("JOIN");
        join.setTimezone("Asia/Bangkok");

        when(weatherClient.getCurrentWeather()).thenReturn("Sunny");
        when(greetingMessageRepository.getGreetingMessage("Sunny", "morning"))
                .thenReturn("Good morning, on a sunshine day!");
        when(coreSystemClient.getUserAccountData("Alice"))
                .thenReturn(new UserAccountData("Alice", 120000.0, LocalDate.now().minusDays(1), List.of()));

        // Act
        List<ChatResponse> responses = chatService.handleMessage(join);

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getContent()).contains("Good morning");
        assertThat(responses.get(1).getContent()).contains("overdue");
    }

    @Test
    void shouldHandlePaymentConfirmationCase() {
        // Arrange
        ChatMessage join = new ChatMessage();
        join.setSender("Bob");
        join.setType("JOIN");
        join.setTimezone("Asia/Bangkok");

        when(weatherClient.getCurrentWeather()).thenReturn("Cloudy");
        when(greetingMessageRepository.getGreetingMessage("Cloudy", "morning"))
                .thenReturn("Good morning, a bit cloudy but I’m here to help!");
        when(coreSystemClient.getUserAccountData("Bob"))
                .thenReturn(new UserAccountData("Bob", 80000.0, LocalDate.now().plusDays(5), List.of()));

        // Act
        List<ChatResponse> responses = chatService.handleMessage(join);

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(1).getContent()).contains("payment today");
    }

    @Test
    void shouldHandleDuplicateTransactionCase() {
        // Arrange
        ChatMessage join = new ChatMessage();
        join.setSender("Carol");
        join.setType("JOIN");
        join.setTimezone("Asia/Bangkok");

        when(weatherClient.getCurrentWeather()).thenReturn("Rainy");
        when(greetingMessageRepository.getGreetingMessage("Rainy", "morning"))
                .thenReturn("Good morning, stay dry out there!");
        when(coreSystemClient.getUserAccountData("Carol"))
                .thenReturn(new UserAccountData("Carol", 110000.0, LocalDate.now().plusDays(5), List.of(5000.0, 5000.0)));
        ConversationContext dummyContext = new ConversationContext("test-user");
        when(conversationManager.getContext(Mockito.anyString()))
                .thenReturn(dummyContext);
        // Act
        List<ChatResponse> responses = chatService.handleMessage(join);

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(1).getContent()).contains("similar transactions");
    }

    @Test
    void shouldHandleYesResponseForOverdue() {
        // Arrange
        String userId = "Alice";
        conversationManager.setState(userId, ConversationState.WAITING_FOR_OVERDUE_CONFIRMATION);

        ChatMessage msg = new ChatMessage();
        msg.setSender(userId);
        msg.setContent("Yes");

        when(coreSystemClient.getUserAccountData(userId))
                .thenReturn(new UserAccountData(userId, 120000.0, LocalDate.of(2025, 9, 1), List.of()));

        // Act
        List<ChatResponse> responses = chatService.handleMessage(msg);

        // Assert
        assertThat(responses.get(0).getContent()).contains("Your current outstanding balance is 120000 THB, and your due date was 2025-09-01");
    }

    @Test
    void shouldHandleCancelDuplicateTransaction() {
        // Arrange
        String userId = "Carol";
        conversationManager.setState(userId, ConversationState.WAITING_FOR_DUPLICATE_CONFIRMATION);

        ChatMessage msg = new ChatMessage();
        msg.setSender(userId);
        msg.setContent("cancel");

        // Act
        List<ChatResponse> responses = chatService.handleMessage(msg);

        // Assert
        assertThat(responses.get(0).getContent()).contains("canceled");
    }
}
