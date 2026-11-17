package ru.mirea.beautysalon.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.mirea.beautysalon.filter.BookingFilter;
import ru.mirea.beautysalon.service.BookingExchange;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class BookingExchangeController {

    private final BookingExchange exchange;

    @PostMapping("/bulk")
    public BookingExchange.BulkResult bulk(@Valid @RequestBody BookingExchange.Bulk body) {
        return exchange.bulk(body);
    }

    @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
    public String export(BookingFilter filter) throws IOException {
        return exchange.export(filter);
    }

    @PostMapping("/import/preview")
    public BookingExchange.Preview preview(
        Authentication auth,
        @RequestParam MultipartFile file,
        @RequestParam BookingExchange.Mode mode
    ) throws IOException {
        return exchange.preview(
            (UUID) auth.getPrincipal(),
            new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8),
            mode
        );
    }

    public record Apply(@NotNull UUID previewId) {}

    @PostMapping("/import/apply")
    public int apply(Authentication auth, @Valid @RequestBody Apply body) throws IOException {
        return exchange.apply((UUID) auth.getPrincipal(), body.previewId());
    }
}
