package com.chatbot.backend.repository.impl;

import com.chatbot.backend.repository.GreetingMessageRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class GreetingMessageRepositoryImpl implements GreetingMessageRepository {
    private final Map<String, Map<String, String>> messages = new HashMap<>();

    public GreetingMessageRepositoryImpl() {
        // Morning
        Map<String, String> morning = new HashMap<>();
        morning.put("Sunny", "Good morning, on a sunshine day!");
        morning.put("Cloudy", "Good morning, a bit cloudy but I’m here to help!");
        morning.put("Rainy", "Good morning, stay dry out there!");
        morning.put("Stormy", "Good morning, let me help make your stormy day better.");
        messages.put("morning", morning);

        // Afternoon
        Map<String, String> afternoon = new HashMap<>();
        afternoon.put("Sunny", "Good afternoon, a bright sunny day!");
        afternoon.put("Cloudy", "Good afternoon, it’s cloudy but I’m still here with you!");
        afternoon.put("Rainy", "Good afternoon, don’t forget your umbrella!");
        afternoon.put("Stormy", "Good afternoon, stay safe in the storm!");
        messages.put("afternoon", afternoon);

        // Evening
        Map<String, String> evening = new HashMap<>();
        evening.put("Sunny", "Good evening, hope you enjoyed the sunshine!");
        evening.put("Cloudy", "Good evening, the clouds are calming tonight.");
        evening.put("Rainy", "Good evening, stay cozy while it rains.");
        evening.put("Stormy", "Good evening, let’s make this stormy night better together.");
        messages.put("evening", evening);
    }

    @Override
    public String getGreetingMessage(String weather, String timeOfDay) {
        return messages
                .getOrDefault(timeOfDay.toLowerCase(), new HashMap<>())
                .getOrDefault(weather, "Hello! How can I help you today?");
    }

}
