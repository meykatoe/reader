package com.reader.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.reader.admin.auth.Admin;
import com.reader.admin.auth.AdminRepository;
import com.reader.common.ErrorCode;
import com.reader.common.RequestIdFilter;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiLogTest {

    private static final String PASSWORD = "top-secret-pass";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApiLogWriter writer;

    @Autowired
    private UserApiLogRepository userLogs;

    @Autowired
    private AdminApiLogRepository adminLogs;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void recordsUserCallsWithAccountAndResult() throws Exception {
        String name = name();
        send(login("/api/auth/register", name, PASSWORD));
        UserApiLog failed = userLog(send(login("/api/auth/login", name, "wrong-password")));
        MvcResult signedIn = send(login("/api/auth/login", name, PASSWORD));
        MockHttpSession session = (MockHttpSession) signedIn.getRequest().getSession();
        UserApiLog listed = userLog(send(get("/api/books?mine=true").session(session)));
        UserApiLog missing = userLog(send(get("/api/books/999999").session(session)));
        UserApiLog noToken = userLog(send(post("/api/books/1/downloads").session(session)));
        UserApiLog loggedOut = userLog(send(post("/api/auth/logout").session(session).with(csrf())));

        assertThat(failed.getAccount()).isEqualTo(name);
        assertThat(failed.getAccountId()).isNull();
        assertThat(failed.getResultCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS.code());
        assertThat(userLog(signedIn).getAccountId()).isNotNull();
        // 不含查詢參數
        assertThat(listed.getPath()).isEqualTo("/api/books");
        assertThat(listed.getResultCode()).isZero();
        assertThat(listed.getCreatedAt()).isNotNull();
        assertThat(missing.getHttpStatus()).isEqualTo(404);
        assertThat(missing.getResultMessage()).isEqualTo("Book not found");
        assertThat(noToken.getAccount()).isEqualTo(name);
        assertThat(noToken.getResultCode()).isEqualTo(ErrorCode.CSRF_INVALID.code());
        assertThat(loggedOut.getAccount()).isEqualTo(name);
        assertThat(userLogs.findAll()).allSatisfy(entry -> assertThat(
                entry.getAccount() + entry.getPath() + entry.getErrorDetail())
                .doesNotContain(PASSWORD, "wrong-password"));
    }

    @Test
    void recordsAnonymousCall() throws Exception {
        UserApiLog entry = userLog(send(get("/api/books")));

        assertThat(entry.getAccount()).isNull();
        assertThat(entry.getResultCode()).isEqualTo(ErrorCode.AUTH_REQUIRED.code());
    }

    @Test
    void keepsAdminCallsInSeparateTable() throws Exception {
        Admin admin = adminRepository.save(
                new Admin(name(), passwordEncoder.encode(PASSWORD), 2));
        MvcResult signedIn = send(login("/api/admin/auth/login", admin.getUsername(), PASSWORD));
        MockHttpSession session = (MockHttpSession) signedIn.getRequest().getSession();
        MvcResult denied = send(get("/api/admin/users").session(session));
        writer.flush();

        // 即時可查
        AdminApiLog entry = adminLogs.findByRequestId(requestId(denied)).getFirst();
        assertThat(entry.getAccount()).isEqualTo(admin.getUsername());
        assertThat(entry.getAccountId()).isEqualTo(admin.getId());
        assertThat(entry.getResultCode()).isEqualTo(ErrorCode.ACCESS_DENIED.code());
        assertThat(userLogs.findByRequestId(requestId(denied))).isEmpty();
    }

    private MvcResult send(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request).andReturn();
    }

    private UserApiLog userLog(MvcResult result) {
        writer.flush();
        return userLogs.findByRequestId(requestId(result)).getFirst();
    }

    private static String requestId(MvcResult result) {
        return result.getResponse().getHeader(RequestIdFilter.HEADER);
    }

    private static MockHttpServletRequestBuilder login(String path, String name, String password) {
        return post(path).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + name + "\",\"password\":\"" + password + "\"}");
    }

    private static String name() {
        return "g" + UUID.randomUUID().toString().substring(0, 8);
    }
}
