package com.reader.user;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.reader.common.ErrorCode;
import com.reader.support.RecordingMailer;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(properties = {
        "reader.limits.mail-cooldown=1ms",
        "reader.limits.mails-per-address=100",
        "reader.mail.public-url=https://reader.example.test/"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RecordingMailer.Config.class)
abstract class EmailTestSupport {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected RecordingMailer mailer;

    @Autowired
    protected UserRepository userRepository;

    protected static String name() {
        return "m" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected ResultActions send(String path, String json) throws Exception {
        return mvc.perform(post(path).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions register(String username, String email, String code) throws Exception {
        return send("/api/auth/register", json("username", username,
                "password", "password123", "email", email, "code", code));
    }

    protected String requestCode(String email) throws Exception {
        pause();
        send("/api/auth/register/code", json("email", email))
                .andExpect(jsonPath("$.code").value(0));
        return mailer.sentTo(Emails.normalize(email)).getLast().code();
    }

    // 等冷卻結束
    protected static void pause() throws InterruptedException {
        Thread.sleep(5);
    }

    protected static ResultActions expect(ResultActions actions, ErrorCode code)
            throws Exception {
        return actions.andExpect(jsonPath("$.code").value(code.code()));
    }

    protected static String wrong(String code) {
        return code.equals("000000") ? "111111" : "000000";
    }

    protected static String json(String... pairs) {
        StringBuilder body = new StringBuilder("{");
        for (int i = 0; i < pairs.length; i += 2) {
            body.append(i == 0 ? "" : ",")
                    .append('"').append(pairs[i]).append("\":\"").append(pairs[i + 1]).append('"');
        }
        return body.append('}').toString();
    }
}
