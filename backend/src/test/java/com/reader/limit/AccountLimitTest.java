package com.reader.limit;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import com.reader.support.Registrations;
import com.reader.verification.VerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(properties = "reader.limits.registrations=3")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountLimitTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private VerificationService verifications;

    @Test
    void limitsRegistrationPerAddress() throws Exception {
        String first = name();
        register(first, "10.1.0.1").andExpect(status().isCreated());
        // 失敗不佔名額
        register(first, "10.1.0.1").andExpect(status().isConflict());
        register(name(), "10.1.0.1").andExpect(status().isCreated());
        register(name(), "10.1.0.1").andExpect(status().isCreated());

        register(name(), "10.1.0.1").andExpect(status().isTooManyRequests());
        register(name(), "10.1.0.2").andExpect(status().isCreated());
    }

    @Test
    void locksLoginAfterFiveFailures() throws Exception {
        String user = name();
        register(user, "10.2.0.1");
        for (int i = 0; i < 4; i++) {
            login("/api/auth/login", user, "wrong-pass", "10.2.0.1")
                    .andExpect(status().isUnauthorized());
        }
        // 成功後歸零
        login("/api/auth/login", user, "password123", "10.2.0.1").andExpect(status().isOk());
        for (int i = 0; i < 5; i++) {
            login("/api/auth/login", user, "wrong-pass", "10.2.0.1")
                    .andExpect(status().isUnauthorized());
        }

        login("/api/auth/login", user.toUpperCase(), "password123", "10.2.0.1")
                .andExpect(status().isTooManyRequests());
        // 他人不受牽連
        login("/api/auth/login", user, "password123", "10.2.0.2").andExpect(status().isOk());
    }

    @Test
    void locksAdminLogin() throws Exception {
        String admin = name();
        for (int i = 0; i < 5; i++) {
            login("/api/admin/auth/login", admin, "wrong-pass", "10.3.0.1")
                    .andExpect(status().isUnauthorized());
        }

        login("/api/admin/auth/login", admin, "wrong-pass", "10.3.0.1")
                .andExpect(status().isTooManyRequests());
    }

    private static String name() {
        return "l" + UUID.randomUUID().toString().substring(0, 8);
    }

    private ResultActions register(String username, String ip) throws Exception {
        return post("/api/auth/register",
                Registrations.body(verifications, username, "password123"), ip);
    }

    private ResultActions login(String path, String username, String password, String ip)
            throws Exception {
        return send(path, username, password, ip);
    }

    private ResultActions send(String path, String username, String password, String ip)
            throws Exception {
        return post(path,
                "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}", ip);
    }

    private ResultActions post(String path, String body, String ip) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post(path).with(csrf())
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
