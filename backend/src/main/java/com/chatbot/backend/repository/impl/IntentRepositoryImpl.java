package com.chatbot.backend.repository.impl;

import com.chatbot.backend.model.IntentDefinition;
import com.chatbot.backend.repository.IntentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IntentRepositoryImpl implements IntentRepository {
    private final List<IntentDefinition> intents = List.of(
            new IntentDefinition("REQUEST_STATEMENT",
                    "I wants a monthly e-statement or transaction summary",
                    List.of("statement", "e-statement", "สเตทเมนท์"),
                    "Here is your statement download link: https://mock/statement.pdf"),

            new IntentDefinition("CHECK_CREDIT_LIMIT",
                    "I wants to know remaining credit limit",
                    List.of("credit limit", "วงเงิน", "เหลือวงเงิน"),
                    "Your available credit limit is 50,000 THB out of 100,000 THB."),

            new IntentDefinition("VIEW_TRANSACTIONS",
                    "I wants to see recent transactions",
                    List.of("transactions", "recent", "รายการใช้"),
                    "Here are your last 3 transactions: Starbucks 150 THB, Grab 300 THB, 7-11 90 THB."),

            new IntentDefinition("PROMOTION_INQUIRY",
                    "I asks about promotions",
                    List.of("promotion", "โปร", "ดีล"),
                    "Current promotion: 10% cashback at all supermarkets."),

            new IntentDefinition("REPORT_LOST_CARD",
                    "I lost my card",
                    List.of("lost my card", "บัตรหาย", "card missing"),
                    "Your card has been temporarily blocked. Please call 111-222-333 for further assistance."),
            new IntentDefinition("APPLY_NEW_CARD",
                    "Apply for a new credit card",
                    List.of("apply card", "new card", "สมัครบัตร", "บัตรใหม่"),
                    "You can apply for a new card here: https://mock/apply-card"),

            new IntentDefinition("INCREASE_CREDIT_LIMIT",
                    "Request to increase credit limit ",
                    List.of("increase limit", "raise credit", "วงเงินเพิ่ม", "เพิ่มวงเงิน"),
                    "We have received your request to increase the credit limit. Our team will contact you shortly."),

            new IntentDefinition("CHECK_REWARD_POINTS",
                    "Check reward points balance",
                    List.of("reward points", "points balance", "คะแนนสะสม", "แต้มสะสม"),
                    "Your current reward points balance is 12,450 points."),

            new IntentDefinition("UPDATE_CONTACT_INFO",
                    "Update contact information",
                    List.of("update contact", "change phone", "อัพเดทข้อมูล", "เปลี่ยนเบอร์", "แก้ไขข้อมูล"),
                    "Your contact information has been updated successfully."),

            new IntentDefinition("CLOSE_ACCOUNT",
                    "Request to close account",
                    List.of("close account", "terminate", "ปิดบัญชี", "ยกเลิกบัญชี"),
                    "We have received your account closure request. Our staff will reach out to confirm.")

    );

    public List<IntentDefinition> findAll() {
        return intents;
    }
}
