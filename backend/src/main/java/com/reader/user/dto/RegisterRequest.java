package com.reader.user.dto;

import com.reader.user.Emails;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "USERNAME_REQUIRED")
        @Pattern(regexp = "[A-Za-z0-9_]{3,32}", message = "USERNAME_INVALID")
        String username,

        @NotBlank(message = "PASSWORD_REQUIRED")
        @Size(min = 8, max = 72, message = "PASSWORD_LENGTH_INVALID")
        String password,

        @NotBlank(message = "EMAIL_REQUIRED")
        @Size(max = Emails.MAX_LENGTH, message = "EMAIL_INVALID")
        @Pattern(regexp = Emails.PATTERN, message = "EMAIL_INVALID")
        String email,

        @NotBlank(message = "CODE_REQUIRED")
        @Pattern(regexp = "\\d{6}", message = "VERIFICATION_INVALID")
        String code) {
}
