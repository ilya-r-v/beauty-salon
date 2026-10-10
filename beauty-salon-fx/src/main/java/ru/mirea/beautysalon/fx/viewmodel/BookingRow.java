package ru.mirea.beautysalon.fx.viewmodel;

import ru.mirea.beautysalon.model.Booking;
import ru.mirea.beautysalon.model.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


//  Строка таблицы записей на услугу: оборачивает Booking (там только UUID-ссылки) и добавляет уже подготовленные для отображения строки — имя клиента, имя
//  мастера, название услуги. TableView работает с этим классом через PropertyValueFactory/лямбды, сам Booking пользователю не показывается.

public class BookingRow {

    private final Booking booking;
    private final String clientName;
    private final String masterName;
    private final String variantTitle;
    private final BigDecimal price;

    public BookingRow(Booking booking, String clientName, String masterName, String variantTitle, BigDecimal price) {
        this.booking = booking;
        this.clientName = clientName;
        this.masterName = masterName;
        this.variantTitle = variantTitle;
        this.price = price;
    }

    public Booking getBooking() { return booking; }
    public UUID getId() { return booking.getId(); }
    public String getClientName() { return clientName; }
    public String getMasterName() { return masterName; }
    public String getVariantTitle() { return variantTitle; }
    public BigDecimal getPrice() { return price; }
    public BookingStatus getStatus() { return booking.getStatus(); }
    public LocalDateTime getTime() { return booking.getTime(); }
}
