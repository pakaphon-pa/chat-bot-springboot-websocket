package com.chatbot.backend.model;

import com.chatbot.backend.util.constant.ConversationState;
import lombok.Data;

@Data
public class ConversationContext {
    private String userId;
    private ConversationState state;
    private long lastActiveTime;

    public ConversationContext(String userId) {
        this.userId = userId;
        this.state = ConversationState.IDLE;
        this.lastActiveTime = System.currentTimeMillis();
    }

    public void setState(ConversationState state) {
        this.state = state;
        touch();
    }

    public void touch() {
        this.lastActiveTime = System.currentTimeMillis();
    }

    public boolean isInactive(long timeoutMs) {
        return System.currentTimeMillis() - lastActiveTime > timeoutMs;
    }
}
