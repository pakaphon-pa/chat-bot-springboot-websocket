package com.chatbot.backend.util;

import com.chatbot.backend.dto.ChatResponse;
import com.chatbot.backend.model.ConversationContext;
import com.chatbot.backend.util.constant.ConversationState;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ConversationManager {
    private final Map<String, ConversationContext> contextStore = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final long INACTIVITY_TIMEOUT = 10000;

    public ConversationManager(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        startMonitor();
    }

    public ConversationContext getContext(String userId) {
        return contextStore.computeIfAbsent(userId, ConversationContext::new);
    }

    public void setState(String userId, ConversationState state) {
        ConversationContext context = getContext(userId);
        context.setState(state);
        context.touch();
    }

    public ConversationState getState(String userId) {
        return getContext(userId).getState();
    }

    private void startMonitor() {
        scheduler.scheduleAtFixedRate(() -> {
            for (Map.Entry<String, ConversationContext> entry : contextStore.entrySet()) {
                String userId = entry.getKey();
                ConversationContext context = entry.getValue();

                if ((context.getState() == ConversationState.IDLE || context.getState() == ConversationState.COMPLETED) && context.isInactive(INACTIVITY_TIMEOUT)) {

                    ChatResponse followUp = new ChatResponse("Bot","Do you need any further assistance?");
                    List<ChatResponse> messages = List.of(followUp);
                    context.setState(ConversationState.WAITING_TO_END);
                    messagingTemplate.convertAndSendToUser(
                            userId,
                            "/queue/messages",
                            messages
                    );
                   continue;
                }

                if (context.getState() == ConversationState.WAITING_TO_END && context.isInactive(INACTIVITY_TIMEOUT)) {
                    ChatResponse survey = new ChatResponse(
                            "Bot", "Thanks for chatting with me today 🙏 Before you go, could you rate your experience? 👍 😐 👎"
                    );
                    List<ChatResponse> messages = List.of(survey);
                    messagingTemplate.convertAndSendToUser(userId, "/queue/messages", messages);

                    context.setState(ConversationState.WAITING_TO_FEEDBACK);
                }

                if (context.getState() == ConversationState.WAITING_TO_FEEDBACK && context.isInactive(INACTIVITY_TIMEOUT)) {
                    context.setState(ConversationState.END);
                }

            }
        }, 1, 1, TimeUnit.SECONDS);
    }
}
