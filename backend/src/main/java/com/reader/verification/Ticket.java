package com.reader.verification;

// 驗證通過
public record Ticket(Long id, String email, String codeHash) {
}
