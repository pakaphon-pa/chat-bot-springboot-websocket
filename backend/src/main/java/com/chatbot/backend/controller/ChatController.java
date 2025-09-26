package com.chatbot.backend.controller;

import com.chatbot.backend.dto.ChatMessage;
import com.chatbot.backend.dto.ChatResponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController extends BaseController {

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatResponse processMessage(ChatMessage message) {
        return switch (message.getType().toUpperCase()) {
            case "JOIN" -> new ChatResponse("Bot", "สวัสดี " + message.getSender() + " ยินดีต้อนรับครับ!");
            case "LEAVE" -> new ChatResponse("Bot", "ขอบคุณที่แวะมาครับ เจอกันใหม่ครับ!");
            default -> new ChatResponse("Bot", "คุณพูดว่า: " + message.getContent());
        };
    }

}
