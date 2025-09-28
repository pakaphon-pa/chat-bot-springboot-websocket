package com.chatbot.backend.service;

import com.chatbot.backend.dto.ChatMessage;
import com.chatbot.backend.dto.ChatResponse;
import com.chatbot.backend.model.IntentDefinition;

import java.util.List;
import java.util.Optional;

public interface IntentDetectionService {
    Optional<ChatResponse> detectIntent(ChatMessage message);
    List<IntentDefinition> getAllIntent();
}
