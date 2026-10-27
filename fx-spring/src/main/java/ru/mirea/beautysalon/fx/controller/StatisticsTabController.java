package ru.mirea.beautysalon.fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ru.mirea.beautysalon.client.ApiException;
import ru.mirea.beautysalon.client.dto.StatisticsResponse;
import ru.mirea.beautysalon.fx.ApiRegistry;

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
    @FXML private Label errorLabel;

    @FXML
    private void initialize() {
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    private void refresh() {
        if (errorLabel != null) errorLabel.setText(null);
        try {
            StatisticsResponse stats = ApiRegistry.api().getStatistics();
            totalClientsLabel.setText(String.valueOf(stats.totalClients()));
            totalMastersLabel.setText(String.valueOf(stats.totalMasters()));
            totalBookingsLabel.setText(String.valueOf(stats.totalBookings()));

            var byStatus = stats.bookingsByStatus();
            pendingLabel.setText(String.valueOf(byStatus.getOrDefault("PENDING", 0L)));
            confirmedLabel.setText(String.valueOf(byStatus.getOrDefault("CONFIRMED", 0L)));
            completedLabel.setText(String.valueOf(byStatus.getOrDefault("COMPLETED", 0L)));
            cancelledLabel.setText(String.valueOf(byStatus.getOrDefault("CANCELLED", 0L)));

            revenueLabel.setText(stats.totalRevenue() + " руб.");
            averageCheckLabel.setText(stats.averageCheck() + " руб.");
        } catch (ApiException e) {
            if (errorLabel != null) errorLabel.setText("Ошибка: " + e.getMessage());
        }
    }
}
