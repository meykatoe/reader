package com.reader.common;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.HttpStatus;

// 數字錯誤碼
public enum ErrorCode {

    // 參數錯誤
    INVALID_PARAMETER(10001, HttpStatus.BAD_REQUEST, "Invalid request parameter"),
    // 無法解讀
    MALFORMED_REQUEST(10002, HttpStatus.BAD_REQUEST, "Malformed request"),
    // 資源不存在
    RESOURCE_NOT_FOUND(10003, HttpStatus.NOT_FOUND, "Resource not found"),
    // 方法不允許
    METHOD_NOT_ALLOWED(10004, HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed"),
    // 類型不支援
    UNSUPPORTED_MEDIA_TYPE(10005, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type"),
    // 請求被拒
    REQUEST_REJECTED(10006, HttpStatus.BAD_REQUEST, "Request rejected"),

    // 尚未登入
    AUTH_REQUIRED(20001, HttpStatus.UNAUTHORIZED, "Authentication required"),
    // 帳密錯誤
    INVALID_CREDENTIALS(20002, HttpStatus.UNAUTHORIZED, "Invalid username or password"),
    // 登入鎖定
    LOGIN_LOCKED(20003, HttpStatus.TOO_MANY_REQUESTS, "Too many login attempts"),
    // 註冊過多
    REGISTER_LIMITED(20004, HttpStatus.TOO_MANY_REQUESTS, "Too many registrations"),
    // 名稱已使用
    USERNAME_TAKEN(20005, HttpStatus.CONFLICT, "Username already taken"),
    // 缺少帳號
    USERNAME_REQUIRED(20006, HttpStatus.BAD_REQUEST, "Username is required"),
    // 帳號格式錯
    USERNAME_INVALID(20007, HttpStatus.BAD_REQUEST,
            "Username must be 3-32 letters, digits or underscores"),
    // 缺少密碼
    PASSWORD_REQUIRED(20008, HttpStatus.BAD_REQUEST, "Password is required"),
    // 密碼長度錯
    PASSWORD_LENGTH_INVALID(20009, HttpStatus.BAD_REQUEST, "Password must be 8-72 characters"),
    // 權限不足
    ACCESS_DENIED(20010, HttpStatus.FORBIDDEN, "Access denied"),
    // 安全驗證錯
    CSRF_INVALID(20011, HttpStatus.FORBIDDEN, "Missing or invalid CSRF token"),

    // 查無書籍
    BOOK_NOT_FOUND(30001, HttpStatus.NOT_FOUND, "Book not found"),
    // 非擁有者
    BOOK_NOT_OWNER(30002, HttpStatus.FORBIDDEN, "Only the owner can do this"),
    // 空白檔案
    FILE_EMPTY(30003, HttpStatus.BAD_REQUEST, "File is empty"),
    // 格式不支援
    FORMAT_UNSUPPORTED(30004, HttpStatus.BAD_REQUEST, "Only EPUB, TXT, PDF and MOBI are supported"),
    // 檔案過大
    FILE_TOO_LARGE(30005, HttpStatus.CONTENT_TOO_LARGE, "File exceeds the size limit"),
    // 解析失敗
    BOOK_PARSE_FAILED(30006, HttpStatus.BAD_REQUEST, "Failed to parse the file"),
    // 版權保護
    BOOK_DRM_PROTECTED(30007, HttpStatus.BAD_REQUEST, "DRM protected books are not supported"),
    // PDF已加密
    PDF_ENCRYPTED(30008, HttpStatus.BAD_REQUEST, "Encrypted PDF is not supported"),
    // 沒有內容
    BOOK_NO_CONTENT(30009, HttpStatus.BAD_REQUEST, "The book has no readable content"),
    // 內容過大
    BOOK_CONTENT_TOO_LARGE(30010, HttpStatus.BAD_REQUEST, "Book content is too large"),
    // 結構損毀
    BOOK_STRUCTURE_INVALID(30011, HttpStatus.BAD_REQUEST, "Book structure is invalid"),
    // 壓縮不支援
    COMPRESSION_UNSUPPORTED(30012, HttpStatus.BAD_REQUEST, "Unsupported MOBI compression"),
    // 查無章節
    CHAPTER_NOT_FOUND(30013, HttpStatus.NOT_FOUND, "Chapter not found"),
    // 查無圖片
    IMAGE_NOT_FOUND(30014, HttpStatus.NOT_FOUND, "Image not found"),
    // 查無原始檔
    BOOK_FILE_NOT_FOUND(30015, HttpStatus.NOT_FOUND, "Book file not found"),
    // 僅限PDF
    PDF_ONLY(30016, HttpStatus.NOT_FOUND, "Raw file is only served for PDF"),
    // 位置超範圍
    POSITION_OUT_OF_RANGE(30017, HttpStatus.BAD_REQUEST, "Reading position out of range"),
    // 進度值錯誤
    PROGRESS_INVALID(30018, HttpStatus.BAD_REQUEST, "Invalid reading progress"),
    // 缺私密設定
    VISIBILITY_REQUIRED(30019, HttpStatus.BAD_REQUEST, "Visibility is required"),
    // 缺少書名
    TITLE_REQUIRED(30020, HttpStatus.BAD_REQUEST, "Title is required"),
    // 書名過長
    TITLE_TOO_LONG(30021, HttpStatus.BAD_REQUEST, "Title is too long"),
    // 作者名過長
    AUTHOR_TOO_LONG(30022, HttpStatus.BAD_REQUEST, "Author is too long"),

    // 下載額度滿
    DOWNLOAD_QUOTA_EXCEEDED(40001, HttpStatus.TOO_MANY_REQUESTS, "Daily download limit reached"),
    // 開書額度滿
    READ_QUOTA_EXCEEDED(40002, HttpStatus.TOO_MANY_REQUESTS, "Daily reading limit reached"),
    // 翻閱過快
    READ_TOO_FAST(40003, HttpStatus.TOO_MANY_REQUESTS, "Reading too fast"),
    // 未獲下載權
    DOWNLOAD_NOT_GRANTED(40004, HttpStatus.FORBIDDEN, "Download has not been requested"),

    // 查無用戶
    USER_NOT_FOUND(50001, HttpStatus.NOT_FOUND, "User not found"),
    // 資料已變動
    USER_DATA_CHANGED(50002, HttpStatus.CONFLICT, "User data changed, please retry"),
    // 到期日錯誤
    MEMBERSHIP_EXPIRY_INVALID(50003, HttpStatus.BAD_REQUEST, "Expiry must be in the future"),

    // 系統錯誤
    INTERNAL_ERROR(90000, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    // 儲存故障
    STORAGE_UNAVAILABLE(90001, HttpStatus.SERVICE_UNAVAILABLE, "Storage service unavailable"),
    // 郵件故障
    MAIL_UNAVAILABLE(90002, HttpStatus.SERVICE_UNAVAILABLE, "Mail service unavailable");

    private final int code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(int code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public static Optional<ErrorCode> byName(String name) {
        return Arrays.stream(values()).filter(value -> value.name().equals(name)).findFirst();
    }

    // 容器狀態對應
    public static ErrorCode forStatus(int status) {
        return switch (status) {
            case 404 -> RESOURCE_NOT_FOUND;
            case 405 -> METHOD_NOT_ALLOWED;
            case 413 -> FILE_TOO_LARGE;
            default -> status < 500 ? REQUEST_REJECTED : INTERNAL_ERROR;
        };
    }

    public int code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }
}
