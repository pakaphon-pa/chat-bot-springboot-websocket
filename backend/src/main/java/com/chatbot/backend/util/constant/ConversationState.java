package com.chatbot.backend.util.constant;

public enum ConversationState {
    IDLE,                               // ยังไม่มีคำถามค้าง
    WAITING_FOR_OVERDUE_CONFIRMATION,
    WAITING_FOR_UPDATED_BALANCE_CONFIRMATION,
    WAITING_FOR_DUPLICATE_CONFIRMATION,
    WAITING_TO_END,
    WAITING_TO_FEEDBACK,
    END,
    COMPLETED                           // จบ flow หนึ่งรอบ
}
