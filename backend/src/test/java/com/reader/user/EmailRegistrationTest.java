package com.reader.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.reader.common.ErrorCode;
import org.junit.jupiter.api.Test;

class EmailRegistrationTest extends EmailTestSupport {

    @Test
    void registersWithMailedCodeOnce() throws Exception {
        String user = name();
        String email = user + "@example.test";
        String code = requestCode("  " + email.toUpperCase() + " ");
        assertThat(mailer.sentTo(email)).hasSize(1);

        register(user, email, code).andExpect(status().isCreated());

        assertThat(userRepository.findByUsernameAndDeletedAtIsNull(user).orElseThrow().getEmail()).isEqualTo(email);
        // 僅能一次
        expect(register(name(), email, code), ErrorCode.VERIFICATION_INVALID);
    }

    @Test
    void newCodeReplacesOldAndIsBoundToEmail() throws Exception {
        String email = name() + "@example.test";
        String first = requestCode(email);
        String second = requestCode(email);

        if (!first.equals(second)) {
            expect(register(name(), email, first), ErrorCode.VERIFICATION_INVALID);
        }
        expect(register(name(), "other-" + email, second), ErrorCode.VERIFICATION_INVALID);
        register(name(), email, second).andExpect(status().isCreated());
    }

    @Test
    void locksCodeAfterFiveAttempts() throws Exception {
        String email = name() + "@example.test";
        String code = requestCode(email);
        for (int i = 0; i < 5; i++) {
            expect(register(name(), email, wrong(code)), ErrorCode.VERIFICATION_INVALID);
        }

        expect(register(name(), email, code), ErrorCode.VERIFICATION_INVALID);
    }

    @Test
    void takenUsernameKeepsCodeUsable() throws Exception {
        String taken = name();
        register(taken, taken + "@example.test", requestCode(taken + "@example.test"));
        String email = name() + "@example.test";
        String code = requestCode(email);

        for (int i = 0; i < 8; i++) {
            expect(register(taken, email, code), ErrorCode.USERNAME_TAKEN);
        }
        register(name(), email, code).andExpect(status().isCreated());
    }

    @Test
    void registeredEmailGetsNoticeWithoutRevealing() throws Exception {
        String user = name();
        String email = user + "@example.test";
        register(user, email, requestCode(email));
        pause();

        send("/api/auth/register/code", json("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        assertThat(mailer.sentTo(email).getLast().subject()).contains("通知");
        assertThat(mailer.sentTo(email).getLast().code()).isNull();
    }

    @Test
    void rejectsHeaderInjectionAndBadAddress() throws Exception {
        for (String email : new String[] {
                "a@example.test\\r\\nBcc:b@example.test", "a,b@example.test", "no-domain@host"}) {
            expect(send("/api/auth/register/code", json("email", email)),
                    ErrorCode.EMAIL_INVALID);
        }
    }

    @Test
    void mailFailureIsReportedAndRetryable() throws Exception {
        String email = name() + "@example.test";
        mailer.failing(true);
        try {
            expect(send("/api/auth/register/code", json("email", email)),
                    ErrorCode.MAIL_UNAVAILABLE);
        } finally {
            mailer.failing(false);
        }

        assertThat(requestCode(email)).hasSize(6);
    }
}
