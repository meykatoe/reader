package com.reader.reading.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

public record ProgressDto(
        @Min(value = 0, message = "位置錯誤") int location,
        @DecimalMin(value = "0", message = "比例錯誤")
        @DecimalMax(value = "1", message = "比例錯誤") double ratio) {

    public static final ProgressDto START = new ProgressDto(0, 0);
}
