package com.reader.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "請輸入使用者名稱") String username,
        @NotBlank(message = "請輸入密碼") String password) {
}
