package com.reader.support;

import com.reader.verification.Purpose;
import com.reader.verification.VerificationService;

public final class Registrations {

    private Registrations() {
    }

    // 直接簽發
    public static String body(VerificationService verifications, String username, String password) {
        String email = username.toLowerCase() + "@example.test";
        String code = verifications.issue(email, Purpose.REGISTER).code();
        return "{\"username\":\"" + username + "\",\"password\":\"" + password
                + "\",\"email\":\"" + email + "\",\"code\":\"" + code + "\"}";
    }
}
