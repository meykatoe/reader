package com.reader.admin.auth;

import com.reader.audit.LogAccount;
import com.reader.common.ApiException;
import com.reader.common.ErrorCode;
import com.reader.common.Result;
import com.reader.limit.AccountGuard;
import com.reader.security.SessionLogin;
import com.reader.user.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
    public Result<AdminResponse> login(@Valid @RequestBody LoginRequest body,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        LogAccount.mark(request, null, body.username());
        Authentication authentication = accountGuard.login(
                "admin", body.username(), request.getRemoteAddr(), () -> authenticate(body));
        sessionLogin.establish(authentication, adminSession.repository(), request, response);
        AdminPrincipal admin = (AdminPrincipal) authentication.getPrincipal();
        LogAccount.mark(request, admin.id(), admin.username());
        return Result.ok(AdminResponse.of(admin));
    }

    @GetMapping("/me")
    public Result<AdminResponse> me(@AuthenticationPrincipal AdminPrincipal admin) {
        return Result.ok(AdminResponse.of(admin));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        adminSession.end(request);
        return Result.ok();
    }

    private Authentication authenticate(LoginRequest body) {
        try {
            return provider.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                    body.username(), body.password()));
        } catch (AuthenticationException ex) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }
    }
}
