package com.reader.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.reader.user.AuthUser;
import com.reader.user.User;
import com.reader.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

class AdminBookTest extends AdminTestSupport {

    @Autowired
    private UserRepository userRepository;

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void bothRolesCanManageBooks(int permission) throws Exception {
        MockHttpSession session = signIn(createAdmin(permission));
        String title = randomName("book");
        int id = create(session, title);

        mvc.perform(get("/api/admin/books").param("keyword", title).session(session))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].ownerName").value("平臺"));
        mvc.perform(put("/api/admin/books/" + id).session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"新書名\",\"author\":\" \",\"isPrivate\":true}"))
                .andExpect(jsonPath("$.title").value("新書名"))
                .andExpect(jsonPath("$.author").isEmpty());
        mvc.perform(delete("/api/admin/books/" + id).session(session).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/admin/books/" + id).session(session).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void platformBookFollowsVisibility() throws Exception {
        MockHttpSession session = signIn(createAdmin(2));
        int id = create(session, randomName("book"));
        RequestPostProcessor reader = createReader();

        assertThat(visibleIds(reader)).contains(id);
        mvc.perform(delete("/api/books/" + id).with(reader).with(csrf()))
                .andExpect(status().isForbidden());

        mvc.perform(put("/api/admin/books/" + id).session(session).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"下架\",\"isPrivate\":true}"));
        assertThat(visibleIds(reader)).doesNotContain(id);
        mvc.perform(get("/api/books/" + id).with(reader)).andExpect(status().isNotFound());
    }

    @Test
    void ordinaryUserCannotUseAdminApi() throws Exception {
        mvc.perform(get("/api/admin/books").with(createReader()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/books")).andExpect(status().isUnauthorized());
    }

    private RequestPostProcessor createReader() {
        User saved = userRepository.save(new User(randomName("r"), "unused"));
        return user(new AuthUser(saved.getId(), saved.getUsername(), "unused"));
    }

    private int create(MockHttpSession session, String title) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.txt", "text/plain", "第一章\n內容".getBytes(StandardCharsets.UTF_8));
        String json = mvc.perform(multipart("/api/admin/books").file(file)
                        .param("title", title).session(session).with(csrf()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(json, "$.id");
    }

    private List<Integer> visibleIds(RequestPostProcessor viewer) throws Exception {
        String json = mvc.perform(get("/api/books").with(viewer))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(json, "$[*].id");
    }
}
