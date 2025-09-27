package com.chatbot.backend.repository;

public interface GreetingMessageRepository {
    String getGreetingMessage(String weather, String timeOfDay);
}
