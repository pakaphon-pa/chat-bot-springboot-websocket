package com.chatbot.backend.repository.impl;

import com.chatbot.backend.model.IntentDefinition;
import com.chatbot.backend.repository.IntentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IntentRepositoryImpl implements IntentRepository {
    private final List<IntentDefinition> intents = List.of(
            new IntentDefinition("REQUEST_STATEMENT",
                    "User wants a monthly e-statement or transaction summary",
                    List.of("statement", "e-statement", "สเตทเมนท์"),
                    "Here is your statement download link: https://mock/statement.pdf"),

            new IntentDefinition("CHECK_CREDIT_LIMIT",
                    "User wants to know remaining credit limit",
                    List.of("credit limit", "วงเงิน", "เหลือวงเงิน"),
                    "Your available credit limit is 50,000 THB out of 100,000 THB."),

            new IntentDefinition("VIEW_TRANSACTIONS",
                    "User wants to see recent transactions",
                    List.of("transactions", "recent", "รายการใช้"),
                    "Here are your last 3 transactions: Starbucks 150 THB, Grab 300 THB, 7-11 90 THB."),

            new IntentDefinition("PROMOTION_INQUIRY",
                    "User asks about promotions",
                    List.of("promotion", "โปร", "ดีล"),
                    "Current promotion: 10% cashback at all supermarkets."),

            new IntentDefinition("REPORT_LOST_CARD",
                    "User lost their card",
                    List.of("lost card", "บัตรหาย", "card missing"),
                    "Your card has been temporarily blocked. Please call 111-222-333 for further assistance.")
    );

    public List<IntentDefinition> findAll() {
        return intents;
    }
}
