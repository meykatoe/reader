package com.reader.quota;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@TestPropertySource(properties = {
        "reader.limits.daily-read-books=2",
        "reader.limits.chapters-per-minute=5"
})
class ReadGuardTest extends QuotaTestSupport {

    @Test
    void limitsDistinctBooksOnEveryContentApi() throws Exception {
        RequestPostProcessor reader = account(null);
        long first = platformBook("一");
        long second = platformBook("二");
        long third = platformBook("三");

        expect(reader, "/api/books/" + first + "/chapters", 200);
        expect(reader, "/api/books/" + second + "/chapters/0", 200);
        expect(reader, "/api/books/" + third + "/chapters", 429);
        expect(reader, "/api/books/" + third + "/chapters/0", 429);
        expect(reader, "/api/books/" + third + "/assets/img-1.png", 429);
        // 已開啟可續讀
        expect(reader, "/api/books/" + first + "/chapters/0", 200);
        // 書架不受限
        expect(reader, "/api/books/" + third, 200);
        expect(reader, "/api/books", 200);
    }

    @Test
    void limitsChapterRateExceptOwnBooks() throws Exception {
        Account reader = newAccount(null);
        long shared = platformBook("公開");
        long mine = book("自己的", reader.id(), true);

        for (int i = 0; i < 5; i++) {
            expect(reader.auth(), "/api/books/" + shared + "/chapters/0", 200);
        }
        expect(reader.auth(), "/api/books/" + shared + "/chapters/0", 429);
        for (int i = 0; i < 8; i++) {
            expect(reader.auth(), "/api/books/" + mine + "/chapters/0", 200);
        }
    }

    private void expect(RequestPostProcessor viewer, String path, int code) throws Exception {
        mvc.perform(get(path).with(viewer)).andExpect(status().is(code));
    }
}
