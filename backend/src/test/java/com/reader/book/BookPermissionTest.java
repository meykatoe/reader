package com.reader.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.reader.user.AuthUser;
import com.reader.user.User;
import com.reader.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookPermissionTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    private RequestPostProcessor owner;
    private RequestPostProcessor guest;

    @BeforeEach
    void createUsers() {
        owner = createUser();
        guest = createUser();
    }

    @Test
    void privateBookIsHiddenFromOthers() throws Exception {
        int id = upload(true);

        mvc.perform(get("/api/books/" + id).with(owner)).andExpect(status().isOk());
        mvc.perform(get("/api/books/" + id).with(guest)).andExpect(status().isNotFound());
        assertThat(visibleIds(guest)).doesNotContain(id);
    }

    @Test
    void publicBookIsReadOnlyForOthers() throws Exception {
        int id = upload(false);

        assertThat(visibleIds(guest)).contains(id);
        mvc.perform(get("/api/books/" + id).with(guest))
                .andExpect(jsonPath("$.mine").value(false));
        mvc.perform(delete("/api/books/" + id).with(guest).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/books/" + id).with(owner).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"isPrivate\":true}"))
                .andExpect(jsonPath("$.isPrivate").value(true));
        mvc.perform(delete("/api/books/" + id).with(owner).with(csrf()))
                .andExpect(status().isNoContent());
    }

    private RequestPostProcessor createUser() {
        String name = "u" + UUID.randomUUID().toString().substring(0, 8);
        User saved = userRepository.save(new User(name, "unused"));
        return user(new AuthUser(saved.getId(), name, "unused"));
    }

    private int upload(boolean isPrivate) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.txt", "text/plain", "第一章\n內容".getBytes(StandardCharsets.UTF_8));
        String json = mvc.perform(multipart("/api/books").file(file)
                        .param("isPrivate", String.valueOf(isPrivate))
                        .with(owner).with(csrf()))
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
