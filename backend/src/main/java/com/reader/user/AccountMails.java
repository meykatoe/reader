package com.reader.user;

import com.reader.verification.IssuedSecret;
import java.time.Duration;

// 信件內容
final class AccountMails {

    private AccountMails() {
    }

    record Mail(String subject, String text) {
    }

    static Mail registrationCode(IssuedSecret secret, Duration ttl) {
        return new Mail("Reader 註冊驗證碼", """
                您的註冊驗證碼：%s

                %d 分鐘內有效。若非本人操作，請忽略這封信。
                """.formatted(secret.code(), ttl.toMinutes()));
    }

    static Mail alreadyRegistered() {
        return new Mail("Reader 註冊通知", """
                有人嘗試用這個信箱註冊，但它已經註冊過帳號。

                若是您本人，請直接登入，忘記密碼可使用「忘記密碼」。
                若非本人操作，請忽略這封信。
                """);
    }

    static Mail accountDisabled() {
        return new Mail("Reader 帳號通知", """
                有人嘗試用這個信箱註冊，但它綁定的帳號目前已停用。

                若要恢復帳號，請聯絡網站管理員。
                若非本人操作，請忽略這封信。
                """);
    }

    // 兩者擇一
    static Mail passwordReset(String username, IssuedSecret secret, String link, Duration ttl) {
        return new Mail("Reader 重設密碼", """
                帳號 %s 申請重設密碼。以下兩種方式擇一使用：

                一、在忘記密碼頁面輸入驗證碼：%s

                二、開啟重設連結：
                %s

                %d 分鐘內有效，使用其中一種後另一種即失效。
                若非本人操作，請忽略這封信，您的密碼不會變更。
                """.formatted(username, secret.code(), link, ttl.toMinutes()));
    }
}
