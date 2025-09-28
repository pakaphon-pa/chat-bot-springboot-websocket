package com.chatbot.backend.repository.impl;

import com.chatbot.backend.repository.FeedbackRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FeedbackRepositoryImpl implements FeedbackRepository {
    private final List<String> feedbackStore = new ArrayList<>();


    @Override
    public void SaveFeedback(String message) {
        feedbackStore.add(message);
        System.out.println("Save Feedback: " + message);
        System.out.println("Feedback: " + feedbackStore);
    }
}
