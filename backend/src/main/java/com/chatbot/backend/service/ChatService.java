package com.chatbot.backend.service;

import com.chatbot.backend.dto.ChatMessage;
import com.chatbot.backend.dto.ChatResponse;

public interface ChatService {
    ChatResponse handleMessage(ChatMessage message);
}
