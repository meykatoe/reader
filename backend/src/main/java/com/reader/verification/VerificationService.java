package com.reader.verification;

import com.reader.common.ApiException;
import com.reader.common.ErrorCode;
import com.reader.limit.LimitProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerificationService {

    private static final int CODE_BOUND = 1_000_000;
    private static final int TOKEN_BYTES = 32;

    private final EmailVerificationRepository repository;
    private final PasswordEncoder encoder;
    private final LimitProperties limits;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public VerificationService(EmailVerificationRepository repository,
                               PasswordEncoder encoder,
                               LimitProperties limits,
                               Clock clock) {
        this.repository = repository;
        this.encoder = encoder;
        this.limits = limits;
        this.clock = clock;
    }

    // 新碼取代舊碼
    @Transactional
    public IssuedSecret issue(String email, Purpose purpose) {
        String code = "%06d".formatted(random.nextInt(CODE_BOUND));
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Instant now = clock.instant();
        repository.deleteTarget(email, purpose);
        repository.save(new EmailVerification(email, purpose, encoder.encode(code),
                sha256(token), now, now.plus(limits.verificationTtl())));
        return new IssuedSecret(code, token);
    }

    // 勿包進交易
    public Ticket verifyCode(String email, Purpose purpose, String code) {
        EmailVerification record = usable(repository.findByEmailAndPurpose(email, purpose)
                .orElseThrow(VerificationService::invalid));
        if (repository.claimAttempt(record.getId(), limits.codeAttempts()) == 0
                || !encoder.matches(code, record.getCodeHash())) {
            throw invalid();
        }
        return ticket(record);
    }

    // 連結不限次
    public Ticket verifyToken(String token, Purpose purpose) {
        return ticket(usable(repository.findByTokenHashAndPurpose(sha256(token), purpose)
                .orElseThrow(VerificationService::invalid)));
    }

    public void consume(Ticket ticket) {
        if (repository.consume(ticket.id(), ticket.codeHash()) == 0) {
            throw invalid();
        }
    }

    @Scheduled(fixedDelayString = "PT1H")
    public void purgeExpired() {
        repository.deleteExpired(clock.instant());
    }

    private EmailVerification usable(EmailVerification record) {
        if (record.isExpired(clock.instant())) {
            throw invalid();
        }
        return record;
    }

    private static Ticket ticket(EmailVerification record) {
        return new Ticket(record.getId(), record.getEmail(), record.getCodeHash());
    }

    // 不透露原因
    private static ApiException invalid() {
        return new ApiException(ErrorCode.VERIFICATION_INVALID);
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
