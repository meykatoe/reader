package com.reader.admin.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminBookUpdateRequest(
        @NotBlank(message = "請輸入書名")
        @Size(max = 255, message = "書名過長")
        String title,

        @Size(max = 255, message = "作者名稱過長")
        String author,

        @NotNull(message = "請指定是否私密")
        Boolean isPrivate) {
}
