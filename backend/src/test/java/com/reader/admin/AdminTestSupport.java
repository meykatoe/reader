package com.reader.admin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.reader.admin.auth.Admin;
import com.reader.admin.auth.AdminRepository;
import com.reader.verification.VerificationService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class AdminTestSupport {

    protected static final String PASSWORD = "admin-password";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected VerificationService verifications;

    @Autowired
    protected AdminRepository adminRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected Admin createAdmin(int permission) {
        // 外部工具格式
        String hash = passwordEncoder.encode(PASSWORD).replaceFirst("^\\$2a\\$", "\\$2y\\$");
        return adminRepository.save(new Admin(randomName("a"), hash, permission));
    }

    protected MockHttpSession signIn(Admin admin) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/admin/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(admin.getUsername(), PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession();
    }

    protected static String randomName(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }

    protected static String credentials(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }
}
