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
        writer.error(request, response, ErrorCode.forStatus(status));
    }
}
