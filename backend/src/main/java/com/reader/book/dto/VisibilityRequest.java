package com.reader.book.dto;

import jakarta.validation.constraints.NotNull;

public record VisibilityRequest(@NotNull(message = "請指定是否私密") Boolean isPrivate) {
}
