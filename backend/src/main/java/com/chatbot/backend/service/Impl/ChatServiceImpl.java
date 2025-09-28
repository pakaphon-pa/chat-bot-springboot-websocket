package com.chatbot.backend.service.Impl;

import com.chatbot.backend.client.CoreSystemClient;
import com.chatbot.backend.client.WeatherClient;
import com.chatbot.backend.dto.ChatMessage;
import com.chatbot.backend.dto.ChatResponse;
import com.chatbot.backend.model.ConversationContext;
import com.chatbot.backend.model.IntentDefinition;
import com.chatbot.backend.model.UserAccountData;
import com.chatbot.backend.repository.FeedbackRepository;
import com.chatbot.backend.repository.GreetingMessageRepository;
import com.chatbot.backend.service.ChatService;
import com.chatbot.backend.service.IntentDetectionService;
import com.chatbot.backend.util.ConversationManager;
import com.chatbot.backend.util.constant.ConversationState;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ChatServiceImpl implements ChatService {

    private final WeatherClient weatherClient;
    private final GreetingMessageRepository greetingMessageRepository;
    private final CoreSystemClient coreSystemClient;
    private final ConversationManager conversationManager;
    private final IntentDetectionService intentDetectionService;
    private final FeedbackRepository feedbackRepository;

    public ChatServiceImpl(WeatherClient weatherClient,
                           GreetingMessageRepository greetingMessageRepository,
                           CoreSystemClient coreSystemClient,
                           ConversationManager conversationManager,
                           IntentDetectionService intentDetectionService,
                           FeedbackRepository feedbackRepository) {
        this.weatherClient = weatherClient;
        this.greetingMessageRepository = greetingMessageRepository;
        this.coreSystemClient = coreSystemClient;
        this.conversationManager = conversationManager;
        this.intentDetectionService = intentDetectionService;
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    public List<ChatResponse> handleMessage(ChatMessage message) {
        List<ChatResponse> responses = new ArrayList<>();
        String userId = message.getUserId();

        ConversationContext context = conversationManager.getContext(userId);
        context.touch();
        ConversationState state = context.getState();
        // Case: User JOIN
        if ("JOIN".equalsIgnoreCase(message.getType())) {
            return handleGreeting(userId, message.getTimezone());
        }


        return switch (state) {
            case WAITING_FOR_OVERDUE_CONFIRMATION -> handleBalanceConfirmation(userId, message, true);
            case WAITING_FOR_UPDATED_BALANCE_CONFIRMATION -> handleBalanceConfirmation(userId, message, false);
            case WAITING_FOR_DUPLICATE_CONFIRMATION -> handleDuplicateConfirmation(userId, message);
            case WAITING_TO_FEEDBACK -> handleFeedback(userId, message);
            case WAITING_TO_END -> handleWaitingToEND(userId,message);
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
        } else if (data.getOutstandingBalance() > 100000) {
            responses.addAll(handlePaymentConfirmationCase(userId));
        } else if (data.getLastTransactions().size() >= 2 &&
                data.getLastTransactions().get(0).equals(data.getLastTransactions().get(1))) {
            responses.addAll(handleDuplicateTransactionCase(userId));
        } else {
            conversationManager.setState(userId, ConversationState.IDLE);
            return List.of(new ChatResponse("Bot",
                    "Do you need any further assistance?"));
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
            conversationManager.setState(userId, ConversationState.COMPLETED);
            return List.of(new ChatResponse("Bot", "Your duplicate transaction has been canceled."));
        } else if (content.equals("report")) {
            conversationManager.setState(userId, ConversationState.COMPLETED);
            return List.of(new ChatResponse("Bot", "Your duplicate transaction has been reported please check you email."));
        }

        return List.of(new ChatResponse("Bot", "Would you like to cancel or report the duplicate transaction?"));
    }

    private List<ChatResponse> handleDefault(String userId, ChatMessage message) {
        Optional<ChatResponse> detected = intentDetectionService.detectIntent(message);
        if (detected.isPresent()) {
            return List.of(detected.get());
        }

        List<IntentDefinition> shuffleIntent = shuffleIntent();

        StringBuilder sb = new StringBuilder();
        sb.append("I didn’t understand what you said 🤖\n");
        sb.append("Do you mean one of these? Please type the keyword:\n\n");

        for (int i = 0; i < 3 && i < shuffleIntent.size(); i++) {
            sb.append("- " + shuffleIntent.get(i).getDescription() + "\n");
        }

        conversationManager.setState(userId, ConversationState.IDLE);
        return List.of(new ChatResponse("Bot", sb.toString()));
    }

    private List<ChatResponse> handleFeedback(String userId, ChatMessage message) {
        conversationManager.setState(userId, ConversationState.END);
        feedbackRepository.SaveFeedback(message.getContent());
        return List.of(new ChatResponse("Bot", "Thanks for Feedback"));
    }

    private List<ChatResponse> handleWaitingToEND(String userId, ChatMessage message) {
        if (message.getContent().equals("no")) {
            conversationManager.setState(userId, ConversationState.WAITING_TO_FEEDBACK);
            return List.of(new ChatResponse("Bot", "Thanks for chatting with me today 🙏 Before you go, could you rate your experience? 👍 😐 👎"));
        }
        return  handleDefault(userId, message);
    }

    private List<IntentDefinition> shuffleIntent() {
        List<IntentDefinition> listIntent = intentDetectionService.getAllIntent();
        List<IntentDefinition> randomIntents = new ArrayList<>(listIntent);
        Collections.shuffle(randomIntents);

        return randomIntents;
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
