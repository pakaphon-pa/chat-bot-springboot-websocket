package com.chatbot.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String userId;
    private String sender;
    private String content;
    private String type;
    private String timezone;
}
