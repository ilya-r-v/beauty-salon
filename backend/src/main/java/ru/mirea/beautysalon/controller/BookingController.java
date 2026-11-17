package ru.mirea.beautysalon.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
import ru.mirea.beautysalon.dto.booking.BookingResponse;
import ru.mirea.beautysalon.dto.booking.CreateBookingRequest;
import ru.mirea.beautysalon.dto.booking.StatusRequest;
import ru.mirea.beautysalon.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public ru.mirea.beautysalon.dto.PageResult<BookingResponse> list(
        ru.mirea.beautysalon.filter.BookingFilter filter,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return bookingService.list(filter, page, size);
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public BookingResponse edit(
        @PathVariable UUID id,
        @jakarta.validation.Valid @RequestBody ru.mirea.beautysalon.dto.booking.BookingEdit request
    ) {
        return bookingService.edit(id, request);
    }

    @PostMapping("/{id}/restore")
    public BookingResponse restore(@PathVariable UUID id) {
        return bookingService.restore(id);
    }

    @DeleteMapping("/{id}/permanent")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void permanentDelete(@PathVariable UUID id) {
        bookingService.permanentDelete(id);
    }

    @GetMapping("/{id}")
    public BookingResponse getBookingById(@PathVariable UUID id) {
        return bookingService.getBookingById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(
        @jakarta.validation.Valid @RequestBody CreateBookingRequest request
    ) {
        return bookingService.createBooking(request);
    }

    @PatchMapping("/{id}/status")
    public BookingResponse updateStatus(
        @PathVariable UUID id,
        @jakarta.validation.Valid @RequestBody StatusRequest request
    ) {
        return bookingService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBooking(@PathVariable UUID id) {
        bookingService.deleteBooking(id);
    }
}
