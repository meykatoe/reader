package com.reader.user;

import com.reader.audit.LogAccount;
import com.reader.common.ApiException;
import com.reader.common.ErrorCode;
import com.reader.common.Result;
import com.reader.limit.AccountGuard;
import com.reader.limit.MailGuard;
import com.reader.user.dto.EmailRequest;
import com.reader.user.dto.PasswordResetRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/password")
public class PasswordController {

    private final PasswordResetService resetService;
    private final MailGuard mailGuard;
    private final AccountGuard accountGuard;

    public PasswordController(PasswordResetService resetService,
                              MailGuard mailGuard,
                              AccountGuard accountGuard) {
        this.resetService = resetService;
        this.mailGuard = mailGuard;
        this.accountGuard = accountGuard;
    }

    @PostMapping("/forgot")
    public Result<Void> forgot(@Valid @RequestBody EmailRequest body, HttpServletRequest request) {
        String email = Emails.normalize(body.email());
        LogAccount.mark(request, null, email);
        resetService.requestReset(email, request.getRemoteAddr());
        return Result.ok();
    }

    @PostMapping("/reset")
    public Result<Void> reset(@Valid @RequestBody PasswordResetRequest body,
                              HttpServletRequest request) {
        if (!body.usesToken() && !body.usesCode()) {
            throw new ApiException(ErrorCode.RESET_PROOF_REQUIRED);
        }
        mailGuard.verification(request.getRemoteAddr());
        User user = body.usesToken()
                ? resetService.resetByToken(body.token(), body.password())
                : resetByCode(body, request);
        LogAccount.mark(request, user.getId(), user.getUsername());
        // 解除登入鎖定
        accountGuard.unlock("user", user.getUsername(), request.getRemoteAddr());
        return Result.ok();
    }

    private User resetByCode(PasswordResetRequest body, HttpServletRequest request) {
        String email = Emails.normalize(body.email());
        LogAccount.mark(request, null, email);
        return resetService.resetByCode(email, body.code(), body.password());
    }
}
