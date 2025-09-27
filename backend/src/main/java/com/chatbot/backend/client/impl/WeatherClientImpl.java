package com.chatbot.backend.client.impl;

import com.chatbot.backend.client.WeatherClient;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class WeatherClientImpl implements WeatherClient {
    private static final String[] MOCK_WEATHER = {
            "Sunny", "Cloudy", "Rainy", "Stormy"
    };

    @Override
    public String getCurrentWeather() {
        int index = new Random().nextInt(MOCK_WEATHER.length);
        return MOCK_WEATHER[index];
    }
}
