package com.reader.admin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.reader.admin.auth.Admin;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

class AdminAuthTest extends AdminTestSupport {

    @Test
    void reportsPermissionAndAuthorities() throws Exception {
        MockHttpSession superSession = signIn(createAdmin(1));
        mvc.perform(get("/api/admin/auth/me").session(superSession))
                .andExpect(jsonPath("$.permission").value(1))
                .andExpect(jsonPath("$.authorities.length()").value(3));

        mvc.perform(get("/api/admin/auth/me").session(signIn(createAdmin(2))))
                .andExpect(jsonPath("$.authorities.length()").value(1))
                .andExpect(jsonPath("$.authorities[0]").value("BOOK_MANAGE"));
    }

    @Test
    void hasNoRegistrationAndRejectsBadPassword() throws Exception {
        mvc.perform(post("/api/admin/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("intruder", PASSWORD)))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/admin/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(createAdmin(1).getUsername(), "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userAndAdminSessionsAreSeparate() throws Exception {
        String user = credentials(randomName("u"), "password123");
        mvc.perform(post("/api/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(user));
        MockHttpSession userSession = (MockHttpSession) mvc.perform(post("/api/auth/login")
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON).content(user))
                .andReturn().getRequest().getSession();

        mvc.perform(get("/api/admin/auth/me").session(userSession))
                .andExpect(status().isUnauthorized());
        MockHttpSession adminSession = signIn(createAdmin(1));
        mvc.perform(get("/api/books").session(adminSession))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminLogoutEndsAccess() throws Exception {
        MockHttpSession session = signIn(createAdmin(1));

        mvc.perform(post("/api/admin/auth/logout").session(session).with(csrf()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/admin/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void databaseChangesApplyImmediately() throws Exception {
        Admin admin = createAdmin(1);
        MockHttpSession session = signIn(admin);

        admin.changePermission(2);
        adminRepository.save(admin);
        mvc.perform(get("/api/admin/auth/me").session(session))
                .andExpect(jsonPath("$.permission").value(2));

        adminRepository.delete(admin);
        mvc.perform(get("/api/admin/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }
}
