package com.chatbot.backend.repository;

import com.chatbot.backend.model.IntentDefinition;

import java.util.List;

public interface IntentRepository {
    List<IntentDefinition> findAll();
}
