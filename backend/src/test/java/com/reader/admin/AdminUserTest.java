package com.reader.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.reader.book.BookRepository;
import com.reader.content.ChapterRepository;
import com.reader.support.Registrations;
import com.reader.user.UserRepository;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;

class AdminUserTest extends AdminTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Test
    void standardAdminCannotTouchUsers() throws Exception {
        MockHttpSession session = signIn(createAdmin(2));
        String name = randomName("u");
        registerAndSignIn(name);
        Long id = userRepository.findByUsername(name).orElseThrow().getId();

        mvc.perform(get("/api/admin/users").session(session)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/admin/users/" + id).session(session).with(csrf()))
                .andExpect(status().isForbidden());
        // 未開放接口
        mvc.perform(put("/api/admin/users/" + id).session(session).with(csrf()))
                .andExpect(status().isForbidden());
        assertThat(userRepository.existsById(id)).isTrue();
    }

    @Test
    void superAdminDeletesUserWithBooks() throws Exception {
        MockHttpSession admin = signIn(createAdmin(1));
        String name = randomName("u");
        MockHttpSession user = registerAndSignIn(name);
        upload(user);
        Long id = userRepository.findByUsername(name).orElseThrow().getId();
        Long bookId = bookRepository.findOwned(id).getFirst().getId();

        mvc.perform(get("/api/admin/users").param("keyword", name).session(admin))
                .andExpect(jsonPath("$.data.items[0].bookCount").value(1))
                .andExpect(jsonPath("$.data.items[0].passwordHash").doesNotExist());
        mvc.perform(delete("/api/admin/users/" + id).session(admin).with(csrf()))
                .andExpect(status().isOk());

        assertThat(userRepository.existsById(id)).isFalse();
        assertThat(bookRepository.existsById(bookId)).isFalse();
        assertThat(chapterRepository.countByBookId(bookId)).isZero();
        // 舊登入失效
        mvc.perform(get("/api/books").session(user)).andExpect(status().isUnauthorized());
    }

    @Test
    void onlySuperAdminManagesMembership() throws Exception {
        String name = randomName("u");
        registerAndSignIn(name);
        Long id = userRepository.findByUsername(name).orElseThrow().getId();
        String path = "/api/admin/users/" + id + "/membership";
        String future = "{\"memberUntil\":\"2099-12-31T15:59:59Z\"}";

        mvc.perform(put(path).session(signIn(createAdmin(2))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(future))
                .andExpect(status().isForbidden());

        MockHttpSession admin = signIn(createAdmin(1));
        mvc.perform(put(path).session(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(future))
                .andExpect(jsonPath("$.data.member").value(true))
                .andExpect(jsonPath("$.data.memberUntil").value("2099-12-31T15:59:59Z"));
        mvc.perform(put(path).session(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberUntil\":\"2000-01-01T00:00:00Z\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put(path).session(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"memberUntil\":null}"))
                .andExpect(jsonPath("$.data.member").value(false));
    }

    private MockHttpSession registerAndSignIn(String name) throws Exception {
        String body = credentials(name, "password123");
        mvc.perform(post("/api/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(Registrations.body(verifications, name, "password123")));
        return (MockHttpSession) mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getRequest().getSession();
    }

    private void upload(MockHttpSession user) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.txt", "text/plain", "第一章\n內容".getBytes(StandardCharsets.UTF_8));
        mvc.perform(multipart("/api/books").file(file).session(user).with(csrf()))
                .andExpect(status().isCreated());
    }
}
