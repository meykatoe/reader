package com.reader.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Result<Object>> handleApi(ApiException ex, HttpServletRequest request) {
        if (ex.code().status().is5xxServerError()) {
            log.error("Request failed with code {}", ex.code().code(), ex);
        }
        Map<String, Object> data = ex.data() == null || ex.data().isEmpty() ? null : ex.data();
        return respond(request, ex.code(), data, ex);
    }

    // 依注解取碼
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Object>> handleInvalid(MethodArgumentNotValidException ex,
                                                        HttpServletRequest request) {
        // 取最小碼
        ErrorCode code = ex.getBindingResult().getFieldErrors().stream()
                .flatMap(error -> ErrorCode.byName(error.getDefaultMessage()).stream())
                .min(Comparator.comparingInt(ErrorCode::code))
                .orElse(ErrorCode.INVALID_PARAMETER);
        return respond(request, code, null, ex);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result<Object>> handleTooLarge(Exception ex, HttpServletRequest request) {
        return respond(request, ErrorCode.FILE_TOO_LARGE, null, ex);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MultipartException.class})
    public ResponseEntity<Result<Object>> handleMalformed(Exception ex, HttpServletRequest request) {
        return respond(request, ErrorCode.MALFORMED_REQUEST, null, ex);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Object>> handleMissing(Exception ex, HttpServletRequest request) {
        return respond(request, ErrorCode.RESOURCE_NOT_FOUND, null, ex);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Object>> handleMethod(Exception ex, HttpServletRequest request) {
        return respond(request, ErrorCode.METHOD_NOT_ALLOWED, null, ex);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Result<Object>> handleMediaType(Exception ex, HttpServletRequest request) {
        return respond(request, ErrorCode.UNSUPPORTED_MEDIA_TYPE, null, ex);
    }

    // 用戶端已斷線
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleDisconnect(AsyncRequestNotUsableException ex) {
        log.debug("Client disconnected", ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleUnexpected(Exception ex,
                                                           HttpServletRequest request,
                                                           HttpServletResponse response) {
        // 已送出則略過
        if (response.isCommitted()) {
            log.debug("Response already committed", ex);
            return null;
        }
        log.error("Unhandled error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return respond(request, ErrorCode.INTERNAL_ERROR, null, ex);
    }

    private static ResponseEntity<Result<Object>> respond(HttpServletRequest request,
                                                          ErrorCode code,
                                                          Object data,
                                                          Throwable cause) {
        Outcome.mark(request, code, cause);
        // 忽略內容協商
        return ResponseEntity.status(code.status())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Result.error(code, data));
    }
}
