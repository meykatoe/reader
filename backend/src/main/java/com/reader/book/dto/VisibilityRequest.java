package com.reader.book.dto;

import jakarta.validation.constraints.NotNull;

public record VisibilityRequest(@NotNull(message = "VISIBILITY_REQUIRED") Boolean isPrivate) {
}
