package com.reader.quota;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.reader.book.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class HardDeleteBookTest extends QuotaTestSupport {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void hardDeleteIsRejected() {
        long id = platformBook("不可硬刪");

        assertThatThrownBy(() -> bookRepository.deleteById(id))
                .hasStackTraceContaining("Hard delete is not allowed");

        assertThat(bookRepository.existsById(id)).isTrue();
    }
}
