package com.reader.user.dto;

import com.reader.user.Emails;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.util.StringUtils;

// 兩者擇一
public record PasswordResetRequest(
        @NotBlank(message = "PASSWORD_REQUIRED")
        @Size(min = 8, max = 72, message = "PASSWORD_LENGTH_INVALID")
        String password,

        @Size(max = 100, message = "VERIFICATION_INVALID")
        String token,

        @Size(max = Emails.MAX_LENGTH, message = "EMAIL_INVALID")
        @Pattern(regexp = Emails.PATTERN, message = "EMAIL_INVALID")
        String email,

        @Pattern(regexp = "\\d{6}", message = "VERIFICATION_INVALID")
        String code) {

    public boolean usesToken() {
        return StringUtils.hasText(token) && email == null && code == null;
    }

    public boolean usesCode() {
        return token == null && email != null && code != null;
    }

    @Override
    public String toString() {
        return "PasswordResetRequest[hidden]";
    }
}
