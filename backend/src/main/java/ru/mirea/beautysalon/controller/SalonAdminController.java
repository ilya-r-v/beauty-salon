package ru.mirea.beautysalon.controller;

import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.mirea.beautysalon.dto.PageResult;
import ru.mirea.beautysalon.service.SalonAdminService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SalonAdminController {

    private final SalonAdminService service;

    @PostMapping("/admin/docs")
    public void docs(
        @RequestHeader("Authorization") String bearer,
        jakarta.servlet.http.HttpServletRequest req,
        jakarta.servlet.http.HttpServletResponse res
    ) {
        var cookie = org.springframework.http.ResponseCookie.from("salon_docs", bearer.substring(7))
            .httpOnly(true)
            .secure(req.isSecure())
            .sameSite("Strict")
            .path("/")
            .build();
        res.addHeader("Set-Cookie", cookie.toString());
    }

    @GetMapping("/admin/users")
    public PageResult<Map<String, Object>> users(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return service.users(page, size);
    }

    @PostMapping("/admin/users")
    public UUID create(@Valid @RequestBody SalonAdminService.UserCreate body) {
        return service.create(body);
    }

    @PutMapping("/admin/users/{id}")
    public void update(@PathVariable UUID id, @Valid @RequestBody SalonAdminService.UserFields body) {
        service.update(id, body);
    }

    @PostMapping("/admin/users/{id}/reset-password")
    public void reset(@PathVariable UUID id, @Valid @RequestBody SalonAdminService.Reset body) {
        service.reset(id, body);
    }

    @GetMapping("/admin/audit")
    public PageResult<Map<String, Object>> audit(
        @RequestParam(defaultValue = "") String search,
        @RequestParam(defaultValue = "desc") String direction,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return service.audit(search, direction, page, size);
    }

    @GetMapping("/analytics/summary")
    public Map<String, Object> summary() {
        return service.summary();
    }
}
