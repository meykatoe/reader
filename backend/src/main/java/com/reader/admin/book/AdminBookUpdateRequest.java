package com.reader.admin.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminBookUpdateRequest(
        @NotBlank(message = "TITLE_REQUIRED")
        @Size(max = 255, message = "TITLE_TOO_LONG")
        String title,

        @Size(max = 255, message = "AUTHOR_TOO_LONG")
        String author,

        @NotNull(message = "VISIBILITY_REQUIRED")
        Boolean isPrivate) {
}
