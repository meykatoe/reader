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

import com.reader.book.Book;
import com.reader.book.BookRepository;
import com.reader.content.ChapterRepository;
import com.reader.storage.BookStorage;
import com.reader.support.Registrations;
import com.reader.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;

class AdminUserTest extends AdminTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private BookStorage bookStorage;

    @Test
    void standardAdminCannotTouchUsers() throws Exception {
        MockHttpSession session = signIn(createAdmin(2));
        String name = randomName("u");
        registerAndSignIn(name);
        Long id = userRepository.findByUsernameAndDeletedAtIsNull(name).orElseThrow().getId();

        mvc.perform(get("/api/admin/users").session(session)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/admin/users/" + id).session(session).with(csrf()))
                .andExpect(status().isForbidden());
        // 未開放接口
        mvc.perform(put("/api/admin/users/" + id).session(session).with(csrf()))
                .andExpect(status().isForbidden());
        assertThat(userRepository.existsById(id)).isTrue();
    }

    @Test
    void softDeletesAndRestoresUser() throws Exception {
        MockHttpSession admin = signIn(createAdmin(1));
        String name = randomName("u");
        MockHttpSession user = registerAndSignIn(name);
        upload(user, "公開作品", false);
        upload(user, "私密筆記", true);
        upload(user, "先前自刪", true);
        Long id = userRepository.findByUsernameAndDeletedAtIsNull(name).orElseThrow().getId();
        List<Book> owned = bookRepository.findOwned(id);
        long shared = idOf(owned, "公開作品");
        long secret = idOf(owned, "私密筆記");
        long removedEarlier = idOf(owned, "先前自刪");
        mvc.perform(delete("/api/books/" + removedEarlier).session(user).with(csrf()))
                .andExpect(status().isOk());
        String secretKey = bookRepository.findById(secret).orElseThrow().getStorageKey();
        MockHttpSession reader = registerAndSignIn(randomName("u"));

        mvc.perform(delete("/api/admin/users/" + id).session(admin).with(csrf()))
                .andExpect(jsonPath("$.data.deletedAt").isNotEmpty());

        // 資料全部保留
        assertThat(userRepository.existsById(id)).isTrue();
        assertThat(bookRepository.existsById(secret)).isTrue();
        assertThat(chapterRepository.countByBookId(secret)).isPositive();
        assertThat(bookStorage.source(secretKey, "txt")).isPresent();
        mvc.perform(get("/api/books").session(user)).andExpect(status().isUnauthorized());
        login(name).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/books/" + shared).session(reader))
                .andExpect(jsonPath("$.data.ownerName").value("已刪除用戶"));
        mvc.perform(get("/api/books/" + secret).session(reader))
                .andExpect(status().isNotFound());
        listUsers(admin, name, false).andExpect(jsonPath("$.data.total").value(0));
        listUsers(admin, name, true).andExpect(jsonPath("$.data.total").value(1));
        mvc.perform(delete("/api/admin/users/" + id).session(admin).with(csrf()))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/admin/users/" + id + "/restore").session(admin).with(csrf()))
                .andExpect(jsonPath("$.data.deletedAt").isEmpty())
                .andExpect(jsonPath("$.data.bookCount").value(2));

        login(name).andExpect(status().isOk());
        // 自刪的不復活
        assertThat(bookRepository.findOwned(id)).extracting(Book::getId)
                .containsExactlyInAnyOrder(shared, secret);
        mvc.perform(get("/api/books/" + shared).session(reader))
                .andExpect(jsonPath("$.data.ownerName").value(name));
        mvc.perform(post("/api/admin/users/" + id + "/restore").session(admin).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void standardAdminCannotRestoreUser() throws Exception {
        mvc.perform(post("/api/admin/users/1/restore").session(signIn(createAdmin(2)))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    private ResultActions login(String name) throws Exception {
        return mvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials(name, "password123")));
    }

    private ResultActions listUsers(MockHttpSession admin, String name, boolean deleted)
            throws Exception {
        return mvc.perform(get("/api/admin/users").param("keyword", name)
                .param("deleted", String.valueOf(deleted)).session(admin));
    }

    private static long idOf(List<Book> books, String title) {
        return books.stream()
                .filter(book -> book.getTitle().equals(title))
                .findFirst().orElseThrow().getId();
    }

    @Test
    void onlySuperAdminManagesMembership() throws Exception {
        String name = randomName("u");
        registerAndSignIn(name);
        Long id = userRepository.findByUsernameAndDeletedAtIsNull(name).orElseThrow().getId();
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

    private void upload(MockHttpSession user, String title, boolean isPrivate)
            throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.txt", "text/plain", "第一章\n內容".getBytes(StandardCharsets.UTF_8));
        mvc.perform(multipart("/api/books").file(file).param("title", title)
                        .param("isPrivate", String.valueOf(isPrivate))
                        .session(user).with(csrf()))
                .andExpect(status().isCreated());
    }
}
