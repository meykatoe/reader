package com.reader.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "請輸入使用者名稱")
        @Pattern(regexp = "[A-Za-z0-9_]{3,32}", message = "使用者名稱需為3至32個英數字或底線")
        String username,

        @NotBlank(message = "請輸入密碼")
        @Size(min = 8, max = 72, message = "密碼長度需為8至72個字元")
        String password) {
}
