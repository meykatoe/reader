package com.reader.user;

import com.reader.common.ApiException;
import com.reader.common.ErrorCode;
import com.reader.limit.LimitProperties;
import com.reader.limit.MailGuard;
import com.reader.mail.MailException;
import com.reader.mail.Mailer;
import com.reader.verification.Purpose;
import com.reader.verification.Ticket;
import com.reader.verification.VerificationService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final MailGuard mailGuard;
    private final Mailer mailer;
    private final LimitProperties limits;
    private final TransactionTemplate transaction;

    public RegistrationService(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               VerificationService verificationService,
                               MailGuard mailGuard,
                               Mailer mailer,
                               LimitProperties limits,
                               TransactionTemplate transaction) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.mailGuard = mailGuard;
        this.mailer = mailer;
        this.limits = limits;
        this.transaction = transaction;
    }

    public void sendCode(String email, String ip) {
        if (!mailer.isReady()) {
            throw new MailException(new IllegalStateException("Mail is not configured"));
        }
        mailGuard.request(email, ip);
        try {
            mailGuard.reserveDelivery();
            // 回應無差異
            AccountMails.Mail mail = userRepository.findByEmail(email)
                    .map(RegistrationService::existingAccountMail)
                    .orElseGet(() -> AccountMails.registrationCode(
                            verificationService.issue(email, Purpose.REGISTER),
                            limits.verificationTtl()));
            mailer.send(email, mail.subject(), mail.text());
        } catch (RuntimeException ex) {
            mailGuard.release(email);
            throw ex;
        }
    }

    private static AccountMails.Mail existingAccountMail(User user) {
        return user.isDeleted()
                ? AccountMails.accountDisabled()
                : AccountMails.alreadyRegistered();
    }

    public void register(String username, String password, String email, String code) {
        // 先查帳號名
        if (userRepository.existsByUsername(username)) {
            throw new ApiException(ErrorCode.USERNAME_TAKEN);
        }
        Ticket ticket = verificationService.verifyCode(email, Purpose.REGISTER, code);
        String passwordHash = passwordEncoder.encode(password);
        try {
            transaction.executeWithoutResult(status -> {
                verificationService.consume(ticket);
                if (userRepository.existsByEmail(email)) {
                    throw new ApiException(ErrorCode.EMAIL_TAKEN);
                }
                userRepository.save(new User(username, passwordHash, email));
            });
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(ErrorCode.USERNAME_TAKEN);
        }
    }
}
