package com.reader.quota;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.reader.book.BookService;
import com.reader.user.AuthUser;
import com.reader.user.User;
import com.reader.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class QuotaTestSupport {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookService bookService;

    protected record Account(Long id, RequestPostProcessor auth) {
    }

    protected Account newAccount(Instant memberUntil) {
        String name = "q" + UUID.randomUUID().toString().substring(0, 8);
        User entity = new User(name, "unused");
        entity.changeMembership(memberUntil);
        Long id = userRepository.save(entity).getId();
        return new Account(id, user(new AuthUser(id, name, "unused")));
    }

    protected RequestPostProcessor account(Instant memberUntil) {
        return newAccount(memberUntil).auth();
    }

    protected long platformBook(String title) {
        return book(title, null, false);
    }

    protected long book(String title, Long ownerId, boolean isPrivate) {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.txt", "text/plain", "第一章\n內容".getBytes(StandardCharsets.UTF_8));
        return bookService.upload(ownerId, file, title, isPrivate).getId();
    }
}
