package com.reader.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

// 過濾層回應
@Component
public class ResultWriter {

    private final JsonMapper mapper;

    public ResultWriter(JsonMapper mapper) {
        this.mapper = mapper;
    }

    public void error(HttpServletRequest request, HttpServletResponse response, ErrorCode code)
            throws IOException {
        Outcome.mark(request, code, null);
        write(response, code.status().value(), Result.error(code, null));
    }

    public void success(HttpServletResponse response) throws IOException {
        write(response, HttpServletResponse.SC_OK, Result.ok());
    }

    private void write(HttpServletResponse response, int status, Result<?> result)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(mapper.writeValueAsString(result));
    }
}
