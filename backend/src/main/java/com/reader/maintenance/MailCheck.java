package com.reader.maintenance;

import com.reader.mail.Mailer;

// 測試寄信
class MailCheck {

    private final Mailer mailer;

    MailCheck(Mailer mailer) {
        this.mailer = mailer;
    }

    boolean run(String recipient) {
        try {
            mailer.send(recipient, "Reader 郵件測試", "收到這封信代表郵件設定正確。");
            System.out.println("OK   已寄出至 " + recipient);
            return true;
        } catch (RuntimeException ex) {
            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
            System.out.println("FAIL " + cause.getMessage());
            return false;
        }
    }
}
