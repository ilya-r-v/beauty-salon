package ru.mirea.beautysalon.fx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ru.mirea.beautysalon.client.ApiException;
import ru.mirea.beautysalon.client.dto.BookingResponse;
import ru.mirea.beautysalon.client.dto.ClientResponse;
import ru.mirea.beautysalon.client.dto.TimeSlotResponse;
import ru.mirea.beautysalon.client.dto.VariantResponse;
import ru.mirea.beautysalon.fx.ApiRegistry;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class BookingDialogController {

    @FXML private ComboBox<ClientResponse> clientCombo;
    @FXML private ComboBox<TimeSlotResponse> slotCombo;
    @FXML private ComboBox<VariantResponse> variantCombo;
    @FXML private Label errorLabel;

    private static final DateTimeFormatter SLOT_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private boolean saved = false;
    private BookingResponse result;

    @FXML
    private void initialize() {
        try {
            clientCombo.getItems().addAll(ApiRegistry.api().getClients());
        } catch (ApiException e) {
            errorLabel.setText("Не удалось загрузить клиентов: " + e.getMessage());
        }
        clientCombo.setButtonCell(clientCell());
        clientCombo.setCellFactory(lv -> clientCell());

        try {
            slotCombo.getItems().addAll(ApiRegistry.api().getFreeSlots());
        } catch (ApiException e) {
            errorLabel.setText("Не удалось загрузить слоты: " + e.getMessage());
        }
        slotCombo.setButtonCell(slotCell());
        slotCombo.setCellFactory(lv -> slotCell());

        try {
            variantCombo.getItems().addAll(ApiRegistry.api().getVariants());
        } catch (ApiException e) {
            errorLabel.setText("Не удалось загрузить варианты: " + e.getMessage());
        }
        variantCombo.setButtonCell(variantCell());
        variantCombo.setCellFactory(lv -> variantCell());
    }

    public static Optional<BookingResponse> open(Window owner, BookingResponse existing) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    BookingDialogController.class.getResource("/fxml/booking-dialog.fxml"));
            Parent root = loader.load();
            BookingDialogController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Новая запись на услугу");
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            return controller.saved ? Optional.of(controller.result) : Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось открыть диалог записи: " + e.getMessage(), e);
        }
    }

    @FXML
    private void onSave() {
        errorLabel.setText(null);

        ClientResponse client = clientCombo.getValue();
        TimeSlotResponse slot = slotCombo.getValue();
        VariantResponse variant = variantCombo.getValue();

        if (client == null || slot == null || variant == null) {
            errorLabel.setText("Заполните все поля.");
            return;
        }

        try {
            result = ApiRegistry.api().createBooking(client.id(), slot.id(), variant.id());
            saved = true;
            ((Stage) errorLabel.getScene().getWindow()).close();
        } catch (ApiException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        saved = false;
        ((Stage) errorLabel.getScene().getWindow()).close();
    }

    private ListCell<ClientResponse> clientCell() {
        return new ListCell<>() {
            @Override protected void updateItem(ClientResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name() + " (" + item.phone() + ")");
            }
        };
    }

    private ListCell<TimeSlotResponse> slotCell() {
        return new ListCell<>() {
            @Override protected void updateItem(TimeSlotResponse item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                String start = item.startTime() != null ? item.startTime().format(SLOT_FMT) : "?";
                String end = item.endTime() != null ? item.endTime().format(SLOT_FMT) : "?";
                setText(item.masterName() + " | " + start + " – " + end);
            }
        };
    }

    private ListCell<VariantResponse> variantCell() {
        return new ListCell<>() {
            @Override protected void updateItem(VariantResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.title() + " — " + item.price() + " руб.");
            }
        };
    }
}
