package com.reader.admin.auth;

import com.reader.common.ApiException;
import com.reader.limit.AccountGuard;
import com.reader.security.SessionLogin;
import com.reader.user.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// 不提供註冊
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AuthenticationProvider provider;
    private final AdminSession adminSession;
    private final SessionLogin sessionLogin;
    private final AccountGuard accountGuard;

    public AdminAuthController(AdminRepository adminRepository,
                               PasswordEncoder passwordEncoder,
                               AdminSession adminSession,
                               SessionLogin sessionLogin,
                               AccountGuard accountGuard) {
        DaoAuthenticationProvider dao = new DaoAuthenticationProvider(
                username -> adminRepository.findByUsername(username)
                        .flatMap(AdminPrincipal::from)
                        .orElseThrow(() -> new UsernameNotFoundException(username)));
        dao.setPasswordEncoder(passwordEncoder);
        this.provider = dao;
        this.adminSession = adminSession;
        this.sessionLogin = sessionLogin;
        this.accountGuard = accountGuard;
    }

    @PostMapping("/login")
    public AdminResponse login(@Valid @RequestBody LoginRequest body,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        Authentication authentication = accountGuard.login(
                "admin", body.username(), request.getRemoteAddr(), () -> authenticate(body));
        sessionLogin.establish(authentication, adminSession.repository(), request, response);
        return AdminResponse.of((AdminPrincipal) authentication.getPrincipal());
    }

    @GetMapping("/me")
    public AdminResponse me(@AuthenticationPrincipal AdminPrincipal admin) {
        return AdminResponse.of(admin);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        adminSession.end(request);
    }

    private Authentication authenticate(LoginRequest body) {
        try {
            return provider.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                    body.username(), body.password()));
        } catch (AuthenticationException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "帳號或密碼錯誤");
        }
    }
}
