package com.reader.user;

import java.util.Locale;

public final class Emails {

    // 阻擋換行
    public static final String PATTERN =
            "\\s*[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+\\s*";

    public static final int MAX_LENGTH = 254;

    private Emails() {
    }

    public static String normalize(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
