package com.chatbot.backend.util;

import com.chatbot.backend.model.ConversationContext;
import com.chatbot.backend.util.constant.ConversationState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConversationManager {
    private final Map<String, ConversationContext> contextStore = new ConcurrentHashMap<>();

    public ConversationContext getContext(String userId) {
        return contextStore.computeIfAbsent(userId, ConversationContext::new);
    }

    public void setState(String userId, ConversationState state) {
        ConversationContext context = getContext(userId);
        context.setState(state);
    }

    public ConversationState getState(String userId) {
        return getContext(userId).getState();
    }
}
