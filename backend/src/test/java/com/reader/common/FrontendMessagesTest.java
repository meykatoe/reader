package com.reader.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class FrontendMessagesTest {

    private static final Path MESSAGES = Path.of("../frontend/src/api/errorMessages.js");

    @Test
    void everyCodeHasTranslation() throws IOException {
        List<Integer> translated = Pattern.compile("(?m)^\\s+(\\d{5}):")
                .matcher(Files.readString(MESSAGES))
                .results()
                .map(match -> Integer.parseInt(match.group(1)))
                .toList();

        assertThat(translated).containsExactlyInAnyOrderElementsOf(
                Arrays.stream(ErrorCode.values()).map(ErrorCode::code).toList());
    }
}
