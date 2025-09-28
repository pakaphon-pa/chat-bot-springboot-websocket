package com.chatbot.backend.client;

import com.chatbot.backend.model.UserAccountData;

public interface CoreSystemClient {
    UserAccountData getUserAccountData(String userId);
}
