package ru.mirea.beautysalon.fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ru.mirea.beautysalon.fx.ServiceRegistry;
import ru.mirea.beautysalon.model.Booking;
import ru.mirea.beautysalon.model.BookingStatus;
import ru.mirea.beautysalon.model.ServiceVariant;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

//7 показателей статистики 
public class StatisticsTabController {

    @FXML private Label totalClientsLabel;
    @FXML private Label totalMastersLabel;
    @FXML private Label totalBookingsLabel;
    @FXML private Label pendingLabel;
    @FXML private Label confirmedLabel;
    @FXML private Label completedLabel;
    @FXML private Label cancelledLabel;
    @FXML private Label revenueLabel;
    @FXML private Label averageCheckLabel;

    @FXML
    private void initialize() {
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    private void refresh() {
        List<Booking> bookings = ServiceRegistry.bookingService().findAll();
        Map<BookingStatus, Long> byStatus = ServiceRegistry.bookingService().countByStatus();
        List<ServiceVariant> variants = ServiceRegistry.serviceCatalogService().findAllVariants();

        totalClientsLabel.setText(String.valueOf(ServiceRegistry.clientService().findAll().size()));
        totalMastersLabel.setText(String.valueOf(ServiceRegistry.clientService().findAllMasters().size()));
        totalBookingsLabel.setText(String.valueOf(bookings.size()));
        pendingLabel.setText(String.valueOf(byStatus.getOrDefault(BookingStatus.PENDING, 0L)));
        confirmedLabel.setText(String.valueOf(byStatus.getOrDefault(BookingStatus.CONFIRMED, 0L)));
        completedLabel.setText(String.valueOf(byStatus.getOrDefault(BookingStatus.COMPLETED, 0L)));
        cancelledLabel.setText(String.valueOf(byStatus.getOrDefault(BookingStatus.CANCELLED, 0L)));

        List<BigDecimal> completedPrices = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .map(b -> variants.stream()
                        .filter(v -> v.getId().equals(b.getServiceVariantId()))
                        .findFirst()
                        .map(ServiceVariant::getPrice)
                        .orElse(BigDecimal.ZERO))
                .toList();

        BigDecimal revenue = completedPrices.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        revenueLabel.setText(revenue + " руб.");

        BigDecimal average = completedPrices.isEmpty()
                ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(completedPrices.size()), 2, RoundingMode.HALF_UP);
        averageCheckLabel.setText(average + " руб.");
    }
}
