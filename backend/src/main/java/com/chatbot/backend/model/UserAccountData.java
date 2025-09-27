package com.chatbot.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserAccountData {
    private String userId;
    private Double outstandingBalance;
    private LocalDate dueDate;
    private List<Double> lastTransactions;
}
