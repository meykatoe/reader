package com.reader.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.reader.common.ErrorCode;
import com.reader.support.RecordingMailer.Sent;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;

class PasswordResetTest extends EmailTestSupport {

    private static final String NEW_PASSWORD = "brand-new-pass";

    @Autowired
    private JdbcTemplate jdbc;

    private record Account(String username, String email) {
    }

    @Test
    void codeResetsPasswordAndKillsLink() throws Exception {
        Account account = createAccount();
        Sent mail = forgot(account);

        resetByCode(account, mail.code()).andExpect(status().isOk());

        login(account, "password123").andExpect(status().isUnauthorized());
        login(account, NEW_PASSWORD).andExpect(status().isOk());
        expect(resetByToken(mail.token()), ErrorCode.VERIFICATION_INVALID);
        expect(resetByCode(account, mail.code()), ErrorCode.VERIFICATION_INVALID);
    }

    @Test
    void linkResetsPasswordAndKillsCode() throws Exception {
        Account account = createAccount();
        Sent mail = forgot(account);
        assertThat(mail.text())
                .contains("https://reader.example.test/reset-password#token=")
                .contains("15 分鐘");

        resetByToken(mail.token()).andExpect(status().isOk());

        login(account, NEW_PASSWORD).andExpect(status().isOk());
        expect(resetByCode(account, mail.code()), ErrorCode.VERIFICATION_INVALID);
    }

    @Test
    void expiresAfterFifteenMinutes() throws Exception {
        Account account = createAccount();
        Sent mail = forgot(account);
        var times = jdbc.queryForMap(
                "select created_at, expires_at from email_verifications where email = ?",
                account.email());
        assertThat(Duration.between(
                ((Timestamp) times.get("created_at")).toInstant(),
                ((Timestamp) times.get("expires_at")).toInstant()))
                .isEqualTo(Duration.ofMinutes(15));

        jdbc.update("update email_verifications set expires_at = ? where email = ?",
                Timestamp.from(Instant.now().minusSeconds(1)), account.email());

        expect(resetByCode(account, mail.code()), ErrorCode.VERIFICATION_INVALID);
        expect(resetByToken(mail.token()), ErrorCode.VERIFICATION_INVALID);
    }

    @Test
    void wrongGuessesLockCodeButNotLink() throws Exception {
        Account account = createAccount();
        Sent mail = forgot(account);
        // 併發猜測
        race(20, () -> resetByCode(account, wrong(mail.code())));

        expect(resetByCode(account, mail.code()), ErrorCode.VERIFICATION_INVALID);
        assertThat(jdbc.queryForObject(
                "select attempts from email_verifications where email = ?",
                Integer.class, account.email())).isEqualTo(5);
        resetByToken(mail.token()).andExpect(status().isOk());
    }

    @Test
    void tokenWorksOnceUnderConcurrency() throws Exception {
        Sent mail = forgot(createAccount());

        assertThat(race(8, () -> resetByToken(mail.token()))
                .stream().filter(code -> code == 200)).hasSize(1);
    }

    @Test
    void endsExistingSessions() throws Exception {
        Account account = createAccount();
        MockHttpSession session = (MockHttpSession) login(account, "password123")
                .andReturn().getRequest().getSession();
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk());

        resetByToken(forgot(account).token());

        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isUnauthorized());
    }

    @Test
    void unknownEmailLooksTheSame() throws Exception {
        String email = name() + "@example.test";

        send("/api/auth/password/forgot", json("email", email)).andExpect(status().isOk());

        Thread.sleep(200);
        assertThat(mailer.sentTo(email)).isEmpty();
    }

    @Test
    void ignoresHostHeaderForLink() throws Exception {
        Account account = createAccount();
        int before = mailer.sentTo(account.email()).size();
        pause();
        mvc.perform(post("/api/auth/password/forgot").with(csrf())
                .header("Host", "evil.example").header("X-Forwarded-Host", "evil.example")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("email", account.email())));

        assertThat(awaitMail(account.email(), before).text())
                .doesNotContain("evil.example");
    }

    @Test
    void requiresExactlyOneMethod() throws Exception {
        expect(send("/api/auth/password/reset", json("password", NEW_PASSWORD)),
                ErrorCode.RESET_PROOF_REQUIRED);
        expect(send("/api/auth/password/reset", json("password", NEW_PASSWORD,
                        "token", "abc", "email", "a@example.test", "code", "123456")),
                ErrorCode.RESET_PROOF_REQUIRED);
    }

    private Account createAccount() throws Exception {
        String user = name();
        Account account = new Account(user, user + "@example.test");
        register(user, account.email(), requestCode(account.email()))
                .andExpect(status().isCreated());
        return account;
    }

    private Sent forgot(Account account) throws Exception {
        int before = mailer.sentTo(account.email()).size();
        pause();
        send("/api/auth/password/forgot", json("email", account.email()))
                .andExpect(status().isOk());
        return awaitMail(account.email(), before);
    }

    // 等背景寄出
    private Sent awaitMail(String email, int before) throws InterruptedException {
        for (int i = 0; i < 200 && mailer.sentTo(email).size() <= before; i++) {
            Thread.sleep(25);
        }
        assertThat(mailer.sentTo(email)).hasSizeGreaterThan(before);
        return mailer.sentTo(email).getLast();
    }

    private ResultActions resetByCode(
            Account account, String code) throws Exception {
        return send("/api/auth/password/reset", json(
                "password", NEW_PASSWORD, "email", account.email(), "code", code));
    }

    private ResultActions resetByToken(String token)
            throws Exception {
        return send("/api/auth/password/reset", json("password", NEW_PASSWORD, "token", token));
    }

    private ResultActions login(
            Account account, String password) throws Exception {
        return send("/api/auth/login",
                json("username", account.username(), "password", password));
    }

    private interface Call {

        ResultActions run() throws Exception;
    }

    private static List<Integer> race(int count, Call call) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(count)) {
            var futures = IntStream.range(0, count).mapToObj(i -> executor.submit(() -> {
                start.await();
                return call.run().andReturn().getResponse().getStatus();
            })).toList();
            start.countDown();
            List<Integer> results = new ArrayList<>();
            for (var future : futures) {
                results.add(future.get());
            }
            return results;
        }
    }
}
