package com.reader.common;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ResultFormatTest.FailingController.class)
class ResultFormatTest {

    @Autowired
    private MockMvc mvc;

    @TestConfiguration
    @RestController
    static class FailingController {

        @GetMapping("/api/test/boom")
        Result<Void> boom() {
            throw new IllegalStateException("secret-internal-detail");
        }
    }

    @Test
    void securityErrorsUseResult() throws Exception {
        expect(mvc.perform(get("/api/books")), 401, ErrorCode.AUTH_REQUIRED);
        expect(mvc.perform(get("/api/admin/books")), 401, ErrorCode.AUTH_REQUIRED);
        expect(mvc.perform(post("/api/auth/login")), 403, ErrorCode.CSRF_INVALID);
        expect(mvc.perform(get("/api/admin/books").with(user("someone"))),
                403, ErrorCode.ACCESS_DENIED);
    }

    @Test
    void frameworkErrorsUseResult() throws Exception {
        expect(mvc.perform(get("/api/nothing").with(user("u"))),
                404, ErrorCode.RESOURCE_NOT_FOUND);
        expect(mvc.perform(post("/api/quota").with(user("u")).with(csrf())),
                405, ErrorCode.METHOD_NOT_ALLOWED);
        expect(mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{broken")),
                400, ErrorCode.MALFORMED_REQUEST);
        expect(mvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"valid_name\",\"password\":\"short\"}")),
                400, ErrorCode.PASSWORD_LENGTH_INVALID);
        // 圖片同格式
        expect(mvc.perform(get("/api/books/999999/cover").accept(MediaType.IMAGE_PNG)),
                401, ErrorCode.AUTH_REQUIRED);
    }

    @Test
    void unexpectedErrorHidesDetail() throws Exception {
        expect(mvc.perform(get("/api/test/boom").with(user("u"))), 500, ErrorCode.INTERNAL_ERROR)
                .andExpect(content().string(not(containsString("secret-internal-detail"))));
    }

    @Test
    void ignoresClientRequestId() throws Exception {
        mvc.perform(get("/api/books").header(RequestIdFilter.HEADER, "forged"))
                .andExpect(header().string(RequestIdFilter.HEADER, matchesPattern("[0-9a-f]{16}")));
    }

    private static ResultActions expect(ResultActions actions, int httpStatus, ErrorCode code)
            throws Exception {
        return actions.andExpect(status().is(httpStatus))
                .andExpect(jsonPath("$.code").value(code.code()))
                .andExpect(jsonPath("$.message").value(code.message()))
                .andExpect(jsonPath("$.requestId").value(matchesPattern("[0-9a-f]{16}")));
    }
}
