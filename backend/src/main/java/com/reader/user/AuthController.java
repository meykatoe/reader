package com.reader.user;

import com.reader.common.ApiException;
import com.reader.common.ErrorCode;
import com.reader.common.Result;
import com.reader.limit.AccountGuard;
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
    private final AccountGuard accountGuard;

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          SecurityContextRepository contextRepository,
                          SessionLogin sessionLogin,
                          AccountGuard accountGuard) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.contextRepository = contextRepository;
        this.sessionLogin = sessionLogin;
        this.accountGuard = accountGuard;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Void> register(@Valid @RequestBody RegisterRequest body,
                                 HttpServletRequest request) {
        accountGuard.register(request.getRemoteAddr(),
                () -> userService.register(body.username(), body.password()));
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<UserResponse> login(@Valid @RequestBody LoginRequest body,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        Authentication authentication = accountGuard.login(
                "user", body.username(), request.getRemoteAddr(), () -> authenticate(body));
        sessionLogin.establish(authentication, contextRepository, request, response);

        return Result.ok(toResponse((AuthUser) authentication.getPrincipal()));
    }

    @GetMapping("/me")
    public Result<UserResponse> me(@AuthenticationPrincipal AuthUser user) {
        return Result.ok(toResponse(user));
    }

    private Authentication authenticate(LoginRequest body) {
        try {
            return authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(body.username(), body.password()));
        } catch (AuthenticationException ex) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    private static UserResponse toResponse(AuthUser user) {
        return new UserResponse(user.id(), user.username());
    }
}
