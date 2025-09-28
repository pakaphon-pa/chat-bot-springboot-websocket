package com.chatbot.backend.client.impl;

import com.chatbot.backend.client.CoreSystemClient;
import com.chatbot.backend.model.IntentDefinition;
import com.chatbot.backend.model.UserAccountData;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class CoreSystemClientImpl implements CoreSystemClient {

    @Override
    public UserAccountData getUserAccountData(String userId) {
        List<UserAccountData> data = List.of(
                new UserAccountData(
                        userId,
                        90000.0,
                        LocalDate.of(2025, 9, 1).plusYears(50),
                        List.of(5000.0, 5000.0)       // last transactions (duplicate case)
                ),
                new UserAccountData(
                        userId,
                        90000.0,
                        LocalDate.of(2025, 9, 1),     // due date
                        List.of(5000.0)
                ),
                new UserAccountData(
                        userId,
                        120000.0,                     // outstanding balance
                        LocalDate.of(2025, 9, 1).plusYears(2),
                        List.of(5000.0)
                )
//                new UserAccountData(
//                        userId,
//                        80000.0,
//                        LocalDate.of(2025, 9, 1).plusYears(50),
//                        List.of(5000.0)
//                )
        );

        List<UserAccountData> random3rdPartyData = new ArrayList<>(data);
        Collections.shuffle(random3rdPartyData);

        // Mock data
        return random3rdPartyData.get(0);
    }
}
