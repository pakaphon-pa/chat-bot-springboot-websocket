package com.chatbot.backend.service;

import com.chatbot.backend.dto.ChatMessage;
import com.chatbot.backend.dto.ChatResponse;

import java.util.List;

public interface ChatService {
    List<ChatResponse> handleMessage(ChatMessage message);
}
