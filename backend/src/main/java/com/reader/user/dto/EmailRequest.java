package com.reader.user.dto;

import com.reader.user.Emails;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmailRequest(
        @NotBlank(message = "EMAIL_REQUIRED")
        @Size(max = Emails.MAX_LENGTH, message = "EMAIL_INVALID")
        @Pattern(regexp = Emails.PATTERN, message = "EMAIL_INVALID")
        String email) {
}
