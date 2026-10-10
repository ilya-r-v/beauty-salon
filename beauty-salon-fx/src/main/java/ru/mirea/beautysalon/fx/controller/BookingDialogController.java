package ru.mirea.beautysalon.fx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.fx.ServiceRegistry;
import ru.mirea.beautysalon.model.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;


//Контроллер диалога добавления/редактирования записи на услугу. диалог только собирает данные формы и показывает сообщение об ошибке
public class BookingDialogController {

    @FXML private ComboBox<Client> clientCombo;
    @FXML private ComboBox<Client> masterCombo;
    @FXML private ComboBox<ServiceVariant> variantCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private VBox statusBox;
    @FXML private ComboBox<BookingStatus> statusCombo;
    @FXML private Label errorLabel;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private Booking editingBooking; // null создание новой записи
    private boolean saved = false;

    @FXML
    private void initialize() {
        List<Client> clients = ServiceRegistry.clientService().findAll();
        clientCombo.getItems().addAll(clients);

        List<Client> masters = clients.stream()
                .filter(c -> ServiceRegistry.clientService().isMaster(c.getId()))
                .toList();
        masterCombo.getItems().addAll(masters);

        variantCombo.getItems().addAll(ServiceRegistry.serviceCatalogService().findAllVariants());

        statusCombo.getItems().addAll(BookingStatus.values());
        statusBox.setVisible(false);
        statusBox.setManaged(false);
    }

    // открывает диалог existing == null приводит к созданию новой записи, иначе редактирование
    public static Optional<Booking> open(Window owner, Booking existing) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    BookingDialogController.class.getResource("/fxml/booking-dialog.fxml"));
            Parent root = loader.load();
            BookingDialogController controller = loader.getController();
            controller.setEditingBooking(existing);

            Stage stage = new Stage();
            stage.setTitle(existing == null ? "Новая запись" : "Редактирование записи");
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            return controller.saved ? Optional.of(controller.editingBooking) : Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось открыть диалог записи: " + e.getMessage(), e);
        }
    }

    private void setEditingBooking(Booking existing) {
        this.editingBooking = existing;
        if (existing != null) {
            statusBox.setVisible(true);
            statusBox.setManaged(true);
            selectById(clientCombo, existing.getClientId());
            selectById(masterCombo, existing.getMasterId());
            selectVariantById(existing.getServiceVariantId());
            datePicker.setValue(existing.getTime().toLocalDate());
            timeField.setText(existing.getTime().toLocalTime().format(TIME_FMT));
            statusCombo.setValue(existing.getStatus());
        } else {
            this.editingBooking = new Booking();
            datePicker.setValue(LocalDate.now());
        }
    }

    private void selectById(ComboBox<Client> combo, java.util.UUID id) {
        combo.getItems().stream().filter(c -> c.getId().equals(id)).findFirst().ifPresent(combo::setValue);
    }

    private void selectVariantById(java.util.UUID id) {
        variantCombo.getItems().stream().filter(v -> v.getId().equals(id)).findFirst().ifPresent(variantCombo::setValue);
    }

    @FXML
    private void onSave() {
        errorLabel.setText(null);

        Client client = clientCombo.getValue();
        Client master = masterCombo.getValue();
        ServiceVariant variant = variantCombo.getValue();
        LocalDate date = datePicker.getValue();
        String timeText = timeField.getText() == null ? "" : timeField.getText().trim();

        if (client == null || master == null || variant == null || date == null || timeText.isEmpty()) {
            errorLabel.setText("Заполните все поля.");
            return;
        }

        LocalTime time;
        try {
            time = LocalTime.parse(timeText, TIME_FMT);
        } catch (Exception e) {
            errorLabel.setText("Время должно быть в формате ЧЧ:ММ, например 14:00.");
            return;
        }

        editingBooking.setClientId(client.getId());
        editingBooking.setMasterId(master.getId());
        editingBooking.setServiceVariantId(variant.getId());
        editingBooking.setTime(LocalDateTime.of(date, time));
        if (statusBox.isVisible()) {
            editingBooking.setStatus(statusCombo.getValue());
        }

        try {
            if (editingBooking.getId() == null) {
                ServiceRegistry.bookingService().create(editingBooking);
            } else {
                ServiceRegistry.bookingService().update(editingBooking);
            }
            saved = true;
            ((Stage) errorLabel.getScene().getWindow()).close();
        } catch (BusinessException | EntityNotFoundException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        saved = false;
        ((Stage) errorLabel.getScene().getWindow()).close();
    }
}
