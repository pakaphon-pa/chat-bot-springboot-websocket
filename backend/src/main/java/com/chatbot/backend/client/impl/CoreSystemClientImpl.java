package com.chatbot.backend.client.impl;

import com.chatbot.backend.client.CoreSystemClient;
import com.chatbot.backend.model.UserAccountData;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class CoreSystemClientImpl implements CoreSystemClient {

    @Override
    public UserAccountData getUserAccountData(String userId) {
        // Mock data
        return new UserAccountData(
                userId,
                120000.0,                     // outstanding balance
                LocalDate.of(2025, 9, 1),     // due date
                List.of(5000.0, 5000.0)       // last transactions (duplicate case)
        );
    }
}
