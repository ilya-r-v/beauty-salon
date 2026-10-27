package ru.mirea.beautysalon.fx.viewmodel;

import ru.mirea.beautysalon.client.dto.BookingResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class BookingRow {

    private final BookingResponse booking;

    public BookingRow(BookingResponse booking) {
        this.booking = booking;
    }

    public BookingResponse getBooking() { return booking; }

    public UUID getId() { return booking.id(); }
    public String getClientName() { return booking.clientName(); }
    public String getMasterName() { return booking.slot() != null ? booking.slot().masterName() : "—"; }
    public String getVariantTitle() { return booking.serviceVariantTitle(); }
    public BigDecimal getPrice() { return booking.price() != null ? booking.price() : BigDecimal.ZERO; }
    public String getStatus() { return booking.status(); }
    public OffsetDateTime getStartTime() { return booking.slot() != null ? booking.slot().startTime() : null; }
}
