package com.reader.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.reader.common.ErrorCode;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SoftDeletedAccountTest extends EmailTestSupport {

    @Test
    void hardDeleteIsRejected() {
        User saved = userRepository.save(new User(name(), "unused"));

        assertThatThrownBy(() -> userRepository.delete(saved))
                .hasStackTraceContaining("Hard delete is not allowed");

        assertThat(userRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    void deletedAccountKeepsNameAndEmailReserved() throws Exception {
        String user = name();
        String email = user + "@example.test";
        register(user, email, requestCode(email)).andExpect(status().isCreated());
        softDelete(user);
        pause();

        send("/api/auth/register/code", json("email", email)).andExpect(status().isOk());

        assertThat(mailer.sentTo(email).getLast().text()).contains("停用");
        assertThat(mailer.sentTo(email).getLast().code()).isNull();
        String other = name() + "@example.test";
        expect(register(user, other, requestCode(other)), ErrorCode.USERNAME_TAKEN);
    }

    @Test
    void deletedAccountCannotResetPassword() throws Exception {
        String user = name();
        String email = user + "@example.test";
        register(user, email, requestCode(email)).andExpect(status().isCreated());
        softDelete(user);
        int before = mailer.sentTo(email).size();
        pause();

        send("/api/auth/password/forgot", json("email", email)).andExpect(status().isOk());

        Thread.sleep(300);
        assertThat(mailer.sentTo(email)).hasSize(before);
    }

    private void softDelete(String username) {
        User user = userRepository.findByUsernameAndDeletedAtIsNull(username).orElseThrow();
        user.softDelete(Instant.now());
        userRepository.save(user);
    }
}
