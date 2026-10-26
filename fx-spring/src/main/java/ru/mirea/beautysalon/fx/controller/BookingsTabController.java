package ru.mirea.beautysalon.fx.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import ru.mirea.beautysalon.client.ApiException;
import ru.mirea.beautysalon.client.dto.BookingResponse;
import ru.mirea.beautysalon.client.dto.MasterResponse;
import ru.mirea.beautysalon.fx.ApiRegistry;
import ru.mirea.beautysalon.fx.viewmodel.BookingRow;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Вкладка «Записи на услугу» — основная сущность.
// Все данные получает через ApiRegistry.api(), фильтры и поиск передаются прямо в запрос.
public class BookingsTabController {

    @FXML private ChoiceBox<String> searchTypeChoice;
    @FXML private TextField searchField;

    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<MasterResponse> masterFilterCombo;
    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;

    @FXML private TableView<BookingRow> bookingTable;
    @FXML private TableColumn<BookingRow, String> clientColumn;
    @FXML private TableColumn<BookingRow, String> masterColumn;
    @FXML private TableColumn<BookingRow, String> variantColumn;
    @FXML private TableColumn<BookingRow, String> priceColumn;
    @FXML private TableColumn<BookingRow, String> statusColumn;
    @FXML private TableColumn<BookingRow, String> timeColumn;

    @FXML private Label messageLabel;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String ALL = "Все";

    private final ObservableList<BookingRow> rows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        clientColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClientName()));
        masterColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMasterName()));
        variantColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getVariantTitle()));
        priceColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrice() + " руб."));
        statusColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        timeColumn.setCellValueFactory(d -> {
            var t = d.getValue().getStartTime();
            return new SimpleStringProperty(t != null ? t.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "—");
        });
        bookingTable.setItems(rows);

        searchTypeChoice.getItems().addAll("По имени клиента", "По названию услуги");
        searchTypeChoice.setValue("По имени клиента");

        statusFilterCombo.getItems().setAll(ALL, "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED");
        statusFilterCombo.setValue(ALL);

        reloadMasterFilter();
        loadAll();
    }

    private void reloadMasterFilter() {
        masterFilterCombo.getItems().clear();
        // null-мастер = "Все"
        masterFilterCombo.getItems().add(null);
        try {
            masterFilterCombo.getItems().addAll(ApiRegistry.api().getMasters());
        } catch (ApiException e) {
            showMessage("Не удалось загрузить мастеров: " + e.getMessage());
        }
        masterFilterCombo.setValue(null);
        // Отображаем "Все" для null и имя для мастера
        masterFilterCombo.setButtonCell(masterCell());
        masterFilterCombo.setCellFactory(lv -> masterCell());
    }

    private ListCell<MasterResponse> masterCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(MasterResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? ALL : item.name()));
            }
        };
    }

    private void loadAll() {
        try {
            List<BookingResponse> bookings = ApiRegistry.api().getBookings();
            showRows(bookings);
        } catch (ApiException e) {
            showMessage("Ошибка загрузки: " + e.getMessage());
        }
    }

    private void showRows(List<BookingResponse> bookings) {
        rows.setAll(bookings.stream().map(BookingRow::new).toList());
        showMessage(rows.isEmpty() ? "Ничего не найдено." : rows.size() + " запис(ей).");
    }

    // ─── Поиск ────────────────────────────────────────────────────────────────

    @FXML
    private void onSearch() {
        String text = searchField.getText() == null ? "" : searchField.getText().trim();
        if (text.isEmpty()) {
            showMessage("Введите текст для поиска.");
            return;
        }
        String searchBy = "По имени клиента".equals(searchTypeChoice.getValue()) ? "clientName" : "serviceName";
        try {
            showRows(ApiRegistry.api().getBookings(text, searchBy, null, null, null, null, null));
        } catch (ApiException e) {
            showMessage("Ошибка поиска: " + e.getMessage());
        }
    }

    @FXML
    private void onResetSearch() {
        searchField.clear();
        loadAll();
    }

    // ─── Фильтры ──────────────────────────────────────────────────────────────

    @FXML
    private void onApplyFilter() {
        String status = statusFilterCombo.getValue();
        MasterResponse master = masterFilterCombo.getValue();
        LocalDate from = dateFromPicker.getValue();
        LocalDate to = dateToPicker.getValue();

        String statusParam = ALL.equals(status) ? null : status;
        UUID masterIdParam = master != null ? master.clientId() : null;
        LocalDateTime dateFromParam = from != null ? from.atStartOfDay() : null;
        LocalDateTime dateToParam = to != null ? to.atTime(23, 59) : null;

        try {
            showRows(ApiRegistry.api().getBookings(null, null, statusParam, masterIdParam,
                    dateFromParam, dateToParam, null));
        } catch (ApiException e) {
            showMessage("Ошибка фильтрации: " + e.getMessage());
        }
    }

    @FXML
    private void onResetFilter() {
        statusFilterCombo.setValue(ALL);
        masterFilterCombo.setValue(null);
        dateFromPicker.setValue(null);
        dateToPicker.setValue(null);
        loadAll();
    }

    // ─── Сортировка ───────────────────────────────────────────────────────────

    @FXML
    private void onSortByTime() {
        try {
            showRows(ApiRegistry.api().getBookings(null, null, null, null, null, null, "time"));
        } catch (ApiException e) {
            showMessage("Ошибка сортировки: " + e.getMessage());
        }
    }

    @FXML
    private void onSortByStatus() {
        try {
            showRows(ApiRegistry.api().getBookings(null, null, null, null, null, null, "status"));
        } catch (ApiException e) {
            showMessage("Ошибка сортировки: " + e.getMessage());
        }
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @FXML
    private void onAdd() {
        Optional<BookingResponse> result = BookingDialogController.open(bookingTable.getScene().getWindow(), null);
        result.ifPresent(b -> loadAll());
    }

    @FXML
    private void onChangeStatus() {
        BookingRow selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Выберите запись в таблице.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(selected.getStatus(),
                "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED");
        dialog.setTitle("Изменить статус");
        dialog.setHeaderText("Запись: " + selected.getClientName() + " → " + selected.getVariantTitle());
        dialog.setContentText("Новый статус:");
        Optional<String> newStatus = dialog.showAndWait();

        newStatus.ifPresent(status -> {
            try {
                ApiRegistry.api().updateBookingStatus(selected.getId(), status);
                loadAll();
            } catch (ApiException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            }
        });
    }

    @FXML
    private void onDelete() {
        BookingRow selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Выберите запись в таблице.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить запись клиента \"" + selected.getClientName() + "\"?");
        confirm.setHeaderText("Подтверждение удаления");
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isPresent() && answer.get() == ButtonType.OK) {
            try {
                ApiRegistry.api().deleteBooking(selected.getId());
                loadAll();
            } catch (ApiException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    private void onRefresh() {
        reloadMasterFilter();
        loadAll();
    }

    // ─── Экспорт ──────────────────────────────────────────────────────────────

    @FXML
    private void onExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить экспорт записей");
        chooser.setInitialFileName("bookings.xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        File file = chooser.showSaveDialog(bookingTable.getScene().getWindow());
        if (file == null) return;

        try {
            ExcelExporter.exportBookings(rows, file.getAbsolutePath());
            new Alert(Alert.AlertType.INFORMATION, "Экспорт завершён: " + file.getName()).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка экспорта: " + e.getMessage()).showAndWait();
        }
    }

    private void showMessage(String text) {
        messageLabel.setText(text);
    }
}
