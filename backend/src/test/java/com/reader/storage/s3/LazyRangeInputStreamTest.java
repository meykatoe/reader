package com.reader.storage.s3;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.util.StreamUtils;

class LazyRangeInputStreamTest {

    private final List<Long> requestedOffsets = new ArrayList<>();

    @Test
    void requestsOnlyFromRangeStart() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (InputStream input = stream("0123456789".getBytes())) {
            // 框架分段做法
            StreamUtils.copyRange(input, output, 6, 8);
        }

        assertThat(output.toString()).isEqualTo("678");
        assertThat(requestedOffsets).containsExactly(6L);
    }

    @Test
    void doesNotRequestWhenUnread() throws IOException {
        stream(new byte[10]).close();

        assertThat(requestedOffsets).isEmpty();
    }

    private InputStream stream(byte[] data) {
        return new LazyRangeInputStream(data.length, offset -> {
            requestedOffsets.add(offset);
            int from = Math.toIntExact(offset);
            return new ByteArrayInputStream(data, from, data.length - from);
        });
    }
}
