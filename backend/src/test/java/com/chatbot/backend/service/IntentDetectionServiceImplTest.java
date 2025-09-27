package com.chatbot.backend.service;

import com.chatbot.backend.dto.ChatMessage;
import com.chatbot.backend.dto.ChatResponse;
import com.chatbot.backend.model.IntentDefinition;
import com.chatbot.backend.repository.IntentRepository;
import com.chatbot.backend.service.Impl.IntentDetectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class IntentDetectionServiceImplTest {
    private IntentRepository intentRepository;
    private IntentDetectionServiceImpl intentService;

    @BeforeEach
    void setUp() {
        intentRepository = Mockito.mock(IntentRepository.class);

        List<IntentDefinition> mockIntents = List.of(
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

        when(intentRepository.findAll()).thenReturn(mockIntents);

        intentService = new IntentDetectionServiceImpl(intentRepository);
    }

    @Test
    void shouldDetectRequestStatement() {
        ChatMessage msg = new ChatMessage();
        msg.setContent("Can I get last month statement?");

        Optional<ChatResponse> response = intentService.detectIntent(msg);

        assertThat(response).isPresent();
        assertThat(response.get().getContent()).contains("statement");
    }

    @Test
    void shouldDetectCheckCreditLimit() {
        ChatMessage msg = new ChatMessage();
        msg.setContent("How much credit limit left?");

        Optional<ChatResponse> response = intentService.detectIntent(msg);

        assertThat(response).isPresent();
        assertThat(response.get().getContent()).contains("credit limit");
    }

    @Test
    void shouldDetectViewTransactions() {
        ChatMessage msg = new ChatMessage();
        msg.setContent("Show my recent transactions");

        Optional<ChatResponse> response = intentService.detectIntent(msg);

        assertThat(response).isPresent();
        assertThat(response.get().getContent()).contains("transactions");
    }

    @Test
    void shouldDetectPromotionInquiry() {
        ChatMessage msg = new ChatMessage();
        msg.setContent("ตอนนี้มีโปรอะไรบ้าง");

        Optional<ChatResponse> response = intentService.detectIntent(msg);

        assertThat(response).isPresent();
        assertThat(response.get().getContent())
                .satisfiesAnyOf(
                        content -> assertThat(content).contains("promotion"),
                        content -> assertThat(content).contains("โปร")
                );
    }

    @Test
    void shouldDetectReportLostCard() {
        ChatMessage msg = new ChatMessage();
        msg.setContent("I lost card yesterday");

        Optional<ChatResponse> response = intentService.detectIntent(msg);

        assertThat(response).isPresent();
        assertThat(response.get().getContent()).contains("blocked");
    }

    @Test
    void shouldReturnEmptyIfNoIntentMatched() {
        ChatMessage msg = new ChatMessage();
        msg.setContent("Hello bot, how are you?");

        Optional<ChatResponse> response = intentService.detectIntent(msg);

        assertThat(response).isEmpty();
    }
}
