package com.chatbot.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class IntentDefinition {
    private String name;
    private String description;
    private List<String> keywords;
    private String sampleResponse;
}