package com.reader.common;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 容器層級錯誤
@RestController
public class FallbackErrorController implements ErrorController {

    private final ResultWriter writer;

    public FallbackErrorController(ResultWriter writer) {
        this.writer = writer;
    }

    @RequestMapping("/error")
    public void handle(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) instanceof Integer value
                ? value
                : 404;
        ErrorCode code = switch (status) {
            case 404 -> ErrorCode.RESOURCE_NOT_FOUND;
            case 405 -> ErrorCode.METHOD_NOT_ALLOWED;
            case 413 -> ErrorCode.FILE_TOO_LARGE;
            default -> status < 500 ? ErrorCode.REQUEST_REJECTED : ErrorCode.INTERNAL_ERROR;
        };
        writer.error(request, response, code);
    }
}
