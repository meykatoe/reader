package com.reader.quota;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

class DownloadQuotaTest extends QuotaTestSupport {

    @Test
    void freeUserGetsOneBookPerDay() throws Exception {
        RequestPostProcessor reader = account(null);
        long first = platformBook("第一本: A/B");
        long second = platformBook("第二本");

        mvc.perform(get("/api/books/" + first + "/download").with(reader))
                .andExpect(status().isForbidden());
        requestDownload(first, reader).andExpect(jsonPath("$.data.downloadUsed").value(1));
        // 重複不扣次
        requestDownload(first, reader).andExpect(status().isOk());
        requestDownload(second, reader).andExpect(status().isTooManyRequests());
        mvc.perform(get("/api/books/" + first + "/download").with(reader))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        containsString("attachment")))
                .andExpect(header().string("Content-Disposition",
                        containsString("A_B.txt")));
    }

    @Test
    void memberGetsTenUntilExpiry() throws Exception {
        RequestPostProcessor member = account(Instant.now().plus(Duration.ofDays(3)));
        List<Long> books = IntStream.range(0, 11).mapToObj(i -> platformBook("書" + i)).toList();

        for (long book : books.subList(0, 10)) {
            requestDownload(book, member).andExpect(status().isOk());
        }
        requestDownload(books.get(10), member).andExpect(status().isTooManyRequests());
        mvc.perform(get("/api/quota").with(member))
                .andExpect(jsonPath("$.data.member").value(true))
                .andExpect(jsonPath("$.data.downloadLimit").value(10))
                .andExpect(jsonPath("$.data.downloadUsed").value(10));
    }

    @Test
    void expiredMemberIsFreeUser() throws Exception {
        mvc.perform(get("/api/quota").with(account(Instant.now().minusSeconds(1))))
                .andExpect(jsonPath("$.data.member").value(false))
                .andExpect(jsonPath("$.data.downloadLimit").value(1));
    }

    @Test
    void ownBooksAreFreeAndPrivateStaysHidden() throws Exception {
        Account owner = newAccount(null);
        long mine = book("自己的", owner.id(), true);
        long other = platformBook("平臺書");

        mvc.perform(get("/api/books/" + mine + "/download").with(owner.auth()))
                .andExpect(status().isOk());
        requestDownload(other, owner.auth()).andExpect(status().isOk());
        requestDownload(mine, account(null)).andExpect(status().isNotFound());
    }

    @Test
    void requiresCsrfToken() throws Exception {
        mvc.perform(post("/api/books/" + platformBook("書") + "/downloads").with(account(null)))
                .andExpect(status().isForbidden());
    }

    @Test
    void parallelRequestsCannotExceedLimit() throws Exception {
        RequestPostProcessor reader = account(null);
        List<Long> books = IntStream.range(0, 8).mapToObj(i -> platformBook("併發" + i)).toList();
        CountDownLatch start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(books.size())) {
            var results = books.stream().map(book -> executor.submit(() -> {
                start.await();
                return mvc.perform(post("/api/books/" + book + "/downloads")
                                .with(reader).with(csrf()))
                        .andReturn().getResponse().getStatus();
            })).toList();
            start.countDown();

            int accepted = 0;
            for (var result : results) {
                accepted += result.get() == 200 ? 1 : 0;
            }
            assertThat(accepted).isEqualTo(1);
        }
    }

    private ResultActions requestDownload(
            long book, RequestPostProcessor viewer) throws Exception {
        return mvc.perform(post("/api/books/" + book + "/downloads").with(viewer).with(csrf()));
    }
}
