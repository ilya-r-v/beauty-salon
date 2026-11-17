package ru.mirea.beautysalon.controller;

import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.mirea.beautysalon.service.BookingNotes;

@RestController
@RequestMapping("/api/bookings/{booking}/notes")
@RequiredArgsConstructor
public class BookingNotesController {

    private final BookingNotes notes;

    @GetMapping
    public List<Map<String, Object>> list(@PathVariable UUID booking) {
        return notes.list(booking);
    }

    @PostMapping
    public UUID add(
        @PathVariable UUID booking,
        Authentication auth,
        @Valid @RequestBody BookingNotes.Note note
    ) {
        return notes.create(booking, (UUID) auth.getPrincipal(), note);
    }

    @PutMapping("/{id}")
    public void edit(
        @PathVariable UUID booking,
        @PathVariable UUID id,
        @Valid @RequestBody BookingNotes.Note note
    ) {
        notes.edit(booking, id, note);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID booking, @PathVariable UUID id) {
        notes.remove(booking, id);
    }
}
