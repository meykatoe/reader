package com.reader.user;

import com.reader.common.ApiException;
import com.reader.common.ErrorCode;
import com.reader.limit.LimitProperties;
import com.reader.limit.MailGuard;
import com.reader.mail.MailException;
import com.reader.mail.MailSettings;
import com.reader.mail.Mailer;
import com.reader.verification.IssuedSecret;
import com.reader.verification.Purpose;
import com.reader.verification.Ticket;
import com.reader.verification.VerificationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class PasswordResetService {

    // 不進伺服器
    private static final String LINK_PATH = "/reset-password#token=";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final MailGuard mailGuard;
    private final Mailer mailer;
    private final MailSettings mailSettings;
    private final BackgroundMailer backgroundMailer;
    private final LimitProperties limits;
    private final TransactionTemplate transaction;

    public PasswordResetService(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                VerificationService verificationService,
                                MailGuard mailGuard,
                                Mailer mailer,
                                MailSettings mailSettings,
                                BackgroundMailer backgroundMailer,
                                LimitProperties limits,
                                TransactionTemplate transaction) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.mailGuard = mailGuard;
        this.mailer = mailer;
        this.mailSettings = mailSettings;
        this.backgroundMailer = backgroundMailer;
        this.limits = limits;
        this.transaction = transaction;
    }

    // 回應一律相同
    public void requestReset(String email, String ip) {
        if (!mailer.isReady() || !mailSettings.hasPublicUrl()) {
            throw new MailException(new IllegalStateException("Mail or PUBLIC_URL not configured"));
        }
        mailGuard.request(email, ip);
        backgroundMailer.submit(() ->
                userRepository.findByEmail(email).ifPresent(this::sendResetMail));
    }

    public User resetByToken(String token, String newPassword) {
        return apply(verificationService.verifyToken(token, Purpose.PASSWORD_RESET), newPassword);
    }

    public User resetByCode(String email, String code, String newPassword) {
        return apply(verificationService.verifyCode(email, Purpose.PASSWORD_RESET, code),
                newPassword);
    }

    private void sendResetMail(User user) {
        mailGuard.reserveDelivery();
        IssuedSecret secret = verificationService.issue(user.getEmail(), Purpose.PASSWORD_RESET);
        AccountMails.Mail mail = AccountMails.passwordReset(
                user.getUsername(),
                secret,
                mailSettings.link(LINK_PATH + secret.token()),
                limits.verificationTtl());
        mailer.send(user.getEmail(), mail.subject(), mail.text());
    }

    private User apply(Ticket ticket, String newPassword) {
        String passwordHash = passwordEncoder.encode(newPassword);
        return transaction.execute(status -> {
            verificationService.consume(ticket);
            User user = userRepository.findByEmail(ticket.email())
                    .orElseThrow(() -> new ApiException(ErrorCode.VERIFICATION_INVALID));
            user.changePassword(passwordHash);
            return userRepository.save(user);
        });
    }
}
