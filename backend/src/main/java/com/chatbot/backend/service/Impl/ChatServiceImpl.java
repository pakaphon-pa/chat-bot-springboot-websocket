package com.chatbot.backend.service.Impl;

import com.chatbot.backend.client.WeatherClient;
import com.chatbot.backend.dto.ChatMessage;
import com.chatbot.backend.dto.ChatResponse;
import com.chatbot.backend.repository.GreetingMessageRepository;
import com.chatbot.backend.service.ChatService;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class ChatServiceImpl implements ChatService {

    private final WeatherClient weatherClient;
    private final GreetingMessageRepository greetingMessageRepository;

    public ChatServiceImpl(WeatherClient weatherClient,
                           GreetingMessageRepository greetingMessageRepository) {
        this.weatherClient = weatherClient;
        this.greetingMessageRepository = greetingMessageRepository;
    }

    @Override
    public ChatResponse handleMessage(ChatMessage message) {
        if ("JOIN".equalsIgnoreCase(message.getType())) {
            return buildGreeting(message);
        }

        return new ChatResponse(message.getSender(), "คุณพูดว่า: " + message.getContent());
    }

    private ChatResponse buildGreeting(ChatMessage message) {
        String weather = weatherClient.getCurrentWeather();
        String timeOfDay = getTimeOfDay(message.getTimezone());
        String text = greetingMessageRepository.getGreetingMessage(weather, timeOfDay);
        return new ChatResponse("Bot", text + " " + message.getSender());
    }

    private String getTimeOfDay(String timezone) {
        ZoneId zoneId;

        try {
            zoneId = ZoneId.of(timezone);
        } catch (Exception e) {
            // fallback ถ้า timezone ที่ส่งมาไม่ถูกต้อง → ใช้ default server
            zoneId = ZoneId.systemDefault();
        }

        int hour = ZonedDateTime.now(zoneId).getHour();

        if (hour >= 5 && hour < 12) return "morning";
        else if (hour >= 12 && hour < 17) return "afternoon";
        return "evening";
    }

}
