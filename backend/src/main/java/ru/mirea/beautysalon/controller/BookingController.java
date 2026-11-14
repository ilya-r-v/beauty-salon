package ru.mirea.beautysalon.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.booking.BookingResponse;
import ru.mirea.beautysalon.dto.booking.CreateBookingRequest;
import ru.mirea.beautysalon.dto.booking.StatusRequest;
import ru.mirea.beautysalon.entity.BookingStatus;
import ru.mirea.beautysalon.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public List<BookingResponse> getAllBookings(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "clientName") String searchBy,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) UUID masterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false, defaultValue = "time") String sortBy) {
        return bookingService.getAllBookings(search, searchBy, status, masterId, dateFrom, dateTo, sortBy);
    }

    @PostMapping("/{id}/restore")
    public BookingResponse restore(@PathVariable UUID id) { return bookingService.restore(id); }
    @DeleteMapping("/{id}/permanent")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void permanentDelete(@PathVariable UUID id) { bookingService.permanentDelete(id); }
    @GetMapping("/{id}")
    public BookingResponse getBookingById(@PathVariable UUID id) {
        return bookingService.getBookingById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(request);
    }

    @PatchMapping("/{id}/status")
    public BookingResponse updateStatus(
            @PathVariable UUID id,
            @jakarta.validation.Valid @RequestBody StatusRequest request) {
        return bookingService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBooking(@PathVariable UUID id) {
        bookingService.deleteBooking(id);
    }
}