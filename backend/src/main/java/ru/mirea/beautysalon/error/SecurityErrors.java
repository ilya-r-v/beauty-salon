package ru.mirea.beautysalon.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityErrors {

    private final ObjectMapper json;

    public void write(
        HttpServletRequest req,
        HttpServletResponse res,
        int status,
        String message
    ) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        json.writeValue(
            res.getOutputStream(),
            Map.of(
                "status",
                status,
                "message",
                message,
                "path",
                req.getRequestURI(),
                "timestamp",
                Instant.now().toString(),
                "correlationId",
                String.valueOf(req.getAttribute("correlationId"))
            )
        );
    }
}
