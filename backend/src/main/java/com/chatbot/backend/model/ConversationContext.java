package com.chatbot.backend.model;

import com.chatbot.backend.util.constant.ConversationState;
import lombok.Data;

@Data
public class ConversationContext {
    private String userId;
    private ConversationState state;

    public ConversationContext(String userId) {
        this.userId = userId;
        this.state = ConversationState.IDLE;
    }
}
