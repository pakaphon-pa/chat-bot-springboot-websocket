package com.chatbot.backend.service.Impl;

import com.chatbot.backend.dto.ChatMessage;
import com.chatbot.backend.dto.ChatResponse;
import com.chatbot.backend.model.IntentDefinition;
import com.chatbot.backend.repository.IntentRepository;
import com.chatbot.backend.service.IntentDetectionService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IntentDetectionServiceImpl implements IntentDetectionService {
    private final IntentRepository intentRepository;

    public IntentDetectionServiceImpl(IntentRepository intentRepository) {
        this.intentRepository = intentRepository;
    }

    @Override
    public Optional<ChatResponse> detectIntent(ChatMessage message) {
        String content = message.getContent().toLowerCase();

        for (IntentDefinition intent : intentRepository.findAll()) {
            boolean match = intent.getKeywords().stream()
                    .anyMatch(content::contains);

            if (match) {
                return Optional.of(new ChatResponse("Bot", intent.getSampleResponse()));
            }
        }
        return Optional.empty();
    }
}
