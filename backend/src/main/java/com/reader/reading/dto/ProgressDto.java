package com.reader.reading.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

public record ProgressDto(
        @Min(value = 0, message = "PROGRESS_INVALID") int location,
        @DecimalMin(value = "0", message = "PROGRESS_INVALID")
        @DecimalMax(value = "1", message = "PROGRESS_INVALID") double ratio) {

    public static final ProgressDto START = new ProgressDto(0, 0);
}
