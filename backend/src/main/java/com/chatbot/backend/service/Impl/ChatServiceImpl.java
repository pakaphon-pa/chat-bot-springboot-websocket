package com.chatbot.backend.service.Impl;

import com.chatbot.backend.client.CoreSystemClient;
import com.chatbot.backend.client.WeatherClient;
import com.chatbot.backend.dto.ChatMessage;
import com.chatbot.backend.dto.ChatResponse;
import com.chatbot.backend.model.UserAccountData;
import com.chatbot.backend.repository.GreetingMessageRepository;
import com.chatbot.backend.service.ChatService;
import com.chatbot.backend.util.ConversationManager;
import com.chatbot.backend.util.constant.ConversationState;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private final WeatherClient weatherClient;
    private final GreetingMessageRepository greetingMessageRepository;
    private final CoreSystemClient coreSystemClient;
    private final ConversationManager conversationManager;



    public ChatServiceImpl(WeatherClient weatherClient,
                           GreetingMessageRepository greetingMessageRepository,
                           CoreSystemClient coreSystemClient,
                           ConversationManager conversationManager) {
        this.weatherClient = weatherClient;
        this.greetingMessageRepository = greetingMessageRepository;
        this.coreSystemClient = coreSystemClient;
        this.conversationManager = conversationManager;
    }

    @Override
    public List<ChatResponse> handleMessage(ChatMessage message) {
        List<ChatResponse> responses = new ArrayList<>();
        String userId = message.getSender();

        ConversationState state = conversationManager.getState(userId);

        // Case: User JOIN
        if ("JOIN".equalsIgnoreCase(message.getType())) {
            return handleGreeting(userId, message.getTimezone());
        }

        return switch (state) {
            case WAITING_FOR_OVERDUE_CONFIRMATION -> handleBalanceConfirmation(userId, message, true);
            case WAITING_FOR_UPDATED_BALANCE_CONFIRMATION -> handleBalanceConfirmation(userId, message, false);
            case WAITING_FOR_DUPLICATE_CONFIRMATION -> handleDuplicateConfirmation(userId, message);
            default -> handleDefault(userId, message);
        };
    }

    private List<ChatResponse> handleGreeting(String userId, String timezone) {
        List<ChatResponse> responses = new ArrayList<>();

        // Greeting
        String weather = weatherClient.getCurrentWeather();
        String timeOfDay = getTimeOfDay(timezone);
        String greet = greetingMessageRepository.getGreetingMessage(weather, timeOfDay);
        responses.add(new ChatResponse("Bot", greet));

        responses.addAll(handlePostGreetingPrediction(userId));

        return responses;
    }

    private List<ChatResponse> handlePostGreetingPrediction(String userId) {
        List<ChatResponse> responses = new ArrayList<>();
        UserAccountData data = coreSystemClient.getUserAccountData(userId);

        if (data.getDueDate().isBefore(LocalDate.now())) {
            responses.addAll(handleOverdueCase(userId));
        } else if (data.getOutstandingBalance() < 100000) {
            responses.addAll(handlePaymentConfirmationCase(userId));
        } else if (data.getLastTransactions().size() >= 2 &&
                data.getLastTransactions().get(0).equals(data.getLastTransactions().get(1))) {
            responses.addAll(handleDuplicateTransactionCase(userId));
        }

        return responses;
    }

    private List<ChatResponse> handleOverdueCase(String userId) {
        conversationManager.setState(userId, ConversationState.WAITING_FOR_OVERDUE_CONFIRMATION);
        return List.of(new ChatResponse("Bot",
                "Looks like your payment is overdue. Would you like to check your current outstanding balance?"));
    }

    private List<ChatResponse> handlePaymentConfirmationCase(String userId) {
        conversationManager.setState(userId, ConversationState.WAITING_FOR_UPDATED_BALANCE_CONFIRMATION);
        return List.of(new ChatResponse("Bot",
                "I see you made a payment today. Would you like me to show your updated balance?"));
    }

    private List<ChatResponse> handleDuplicateTransactionCase(String userId) {
        conversationManager.setState(userId, ConversationState.WAITING_FOR_DUPLICATE_CONFIRMATION);
        return List.of(new ChatResponse("Bot",
                "I noticed 2 similar transactions. Do you want to cancel or report them?"));
    }

    private List<ChatResponse> handleBalanceConfirmation(String userId, ChatMessage message, boolean isOverdue) {
        String content = message.getContent().toLowerCase();
        UserAccountData data = coreSystemClient.getUserAccountData(userId);

        if (content.equals("yes")) {
            if (isOverdue) {
                conversationManager.setState(userId, ConversationState.COMPLETED);
                return List.of(new ChatResponse("Bot",
                        String.format("Your current outstanding balance is %.0f THB, and your due date was %s",
                                data.getOutstandingBalance(), data.getDueDate())));
            } else {
                conversationManager.setState(userId, ConversationState.COMPLETED);
                return List.of(new ChatResponse("Bot",
                        "Your updated balance is " + data.getOutstandingBalance() + " THB."));
            }
        } else if (content.equals("no")) {
            conversationManager.setState(userId, ConversationState.IDLE);
            return List.of(new ChatResponse("Bot", "Okay, let me know if you need anything else."));
        }

        return List.of(new ChatResponse("Bot", "Would you like me to show your balance? (Yes/No)"));
    }

    private List<ChatResponse> handleDuplicateConfirmation(String userId, ChatMessage message) {
        String content = message.getContent().toLowerCase();

        if (content.equals("cancel")) {
            // call api 3rd party cancel
            conversationManager.setState(userId, ConversationState.COMPLETED);
            return List.of(new ChatResponse("Bot", "Your duplicate transaction has been canceled (mock)."));
        } else if (content.equals("report")) {
            // call api 3rd party cancel send report in email
            conversationManager.setState(userId, ConversationState.COMPLETED);
            return List.of(new ChatResponse("Bot", "Your duplicate transaction has been reported (mock)."));
        }

        return List.of(new ChatResponse("Bot", "Would you like to cancel or report the duplicate transaction?"));
    }

    private List<ChatResponse> handleDefault(String userId, ChatMessage message) {
        String text = message.getContent().toLowerCase();
        return List.of(new ChatResponse("Bot", "คุณพูดว่า: " + message.getContent()));
    }


    private String getTimeOfDay(String timezone) {
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timezone);
        } catch (Exception e) {
            zoneId = ZoneId.systemDefault();
        }

        int hour = ZonedDateTime.now(zoneId).getHour();

        if (hour >= 5 && hour < 12) return "morning";
        else if (hour >= 12 && hour < 17) return "afternoon";
        return "evening";
    }

}
