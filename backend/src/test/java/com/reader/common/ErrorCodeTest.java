package com.reader.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ErrorCodeTest {

    @Test
    void codesAreUniqueAndMessagesEnglish() {
        assertThat(Arrays.stream(ErrorCode.values()).map(ErrorCode::code))
                .doesNotHaveDuplicates()
                .doesNotContain(Result.SUCCESS);
        assertThat(Arrays.stream(ErrorCode.values()).map(ErrorCode::message))
                .allMatch(message -> message.matches("\\p{ASCII}+"));
    }
}
