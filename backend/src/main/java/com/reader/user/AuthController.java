package com.reader.user;

import com.reader.common.ApiException;
import com.reader.security.SessionLogin;
import com.reader.user.dto.LoginRequest;
import com.reader.user.dto.RegisterRequest;
import com.reader.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextRepository;
    private final SessionLogin sessionLogin;

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          SecurityContextRepository contextRepository,
                          SessionLogin sessionLogin) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.contextRepository = contextRepository;
        this.sessionLogin = sessionLogin;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request.username(), request.password());
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest body,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        Authentication authentication = authenticate(body);
        sessionLogin.establish(authentication, contextRepository, request, response);

        return toResponse((AuthUser) authentication.getPrincipal());
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthUser user) {
        return toResponse(user);
    }

    private Authentication authenticate(LoginRequest body) {
        try {
            return authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(body.username(), body.password()));
        } catch (AuthenticationException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "帳號或密碼錯誤");
        }
    }

    private static UserResponse toResponse(AuthUser user) {
        return new UserResponse(user.id(), user.username());
    }
}
