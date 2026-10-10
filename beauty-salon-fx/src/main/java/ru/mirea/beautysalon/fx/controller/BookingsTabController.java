package ru.mirea.beautysalon.fx.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.fx.ServiceRegistry;
import ru.mirea.beautysalon.fx.viewmodel.BookingRow;
import ru.mirea.beautysalon.model.Booking;
import ru.mirea.beautysalon.model.BookingStatus;
import ru.mirea.beautysalon.model.Client;
import ru.mirea.beautysalon.model.ServiceVariant;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

// контроллер вкладки записи на услугу — основная сущность варианта
// все данные получает через BookingService, ClientService и ServiceCatalogService
public class BookingsTabController {

    @FXML private ChoiceBox<String> searchTypeChoice;
    @FXML private TextField searchField;

    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> masterFilterCombo;
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
        statusColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
        timeColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTime().format(TIME_FMT)));
        bookingTable.setItems(rows);

        searchTypeChoice.getItems().addAll("По имени клиента", "По названию услуги");
        searchTypeChoice.setValue("По имени клиента");

        reloadFilterCombos();
        loadAll();
    }

    private void reloadFilterCombos() {
        statusFilterCombo.getItems().setAll(ALL, "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED");
        statusFilterCombo.setValue(ALL);

        masterFilterCombo.getItems().clear();
        masterFilterCombo.getItems().add(ALL);
        ServiceRegistry.clientService().findAll().stream()
                .filter(c -> ServiceRegistry.clientService().isMaster(c.getId()))
                .forEach(c -> masterFilterCombo.getItems().add(c.getName()));
        masterFilterCombo.setValue(ALL);
    }

    private void loadAll() {
        showRows(ServiceRegistry.bookingService().findAll());
    }

    // превращает список Booking в строки для таблицы с готовыми именами
    private void showRows(List<Booking> bookings) {
        rows.clear();
        for (Booking b : bookings) {
            String clientName = safeName(() -> ServiceRegistry.clientService().getById(b.getClientId()).getName());
            String masterName = safeName(() -> ServiceRegistry.clientService().getById(b.getMasterId()).getName());
            ServiceVariant variant;
            try {
                variant = ServiceRegistry.serviceCatalogService().getVariantById(b.getServiceVariantId());
            } catch (EntityNotFoundException e) {
                variant = null;
            }
            String variantTitle = variant != null ? variant.getTitle() : "—";
            var price = variant != null ? variant.getPrice() : java.math.BigDecimal.ZERO;
            rows.add(new BookingRow(b, clientName, masterName, variantTitle, price));
        }
        messageLabel.setText(rows.isEmpty() ? "Ничего не найдено." : rows.size() + " запис(ей).");
    }

    private String safeName(java.util.function.Supplier<String> supplier) {
        try {
            return supplier.get();
        } catch (EntityNotFoundException e) {
            return "—";
        }
    }

    // поиск

    @FXML
    private void onSearch() {
        String text = searchField.getText() == null ? "" : searchField.getText().trim();
        if (text.isEmpty()) {
            messageLabel.setText("Введите текст для поиска.");
            return;
        }
        List<Booking> result = "По имени клиента".equals(searchTypeChoice.getValue())
                ? ServiceRegistry.bookingService().searchByClientName(text)
                : ServiceRegistry.bookingService().searchByServiceTitle(text);
        showRows(result);
    }

    @FXML
    private void onResetSearch() {
        searchField.clear();
        loadAll();
    }

    //фильтры
    @FXML
    private void onApplyFilter() {
        try {
            // Каждый фильтр даёт набор ID подходящих записей
            // пересечение всех включённых фильтров
            List<Booking> all = ServiceRegistry.bookingService().findAll();
            java.util.Set<java.util.UUID> matchingIds = null;

            String status = statusFilterCombo.getValue();
            if (status != null && !ALL.equals(status)) {
                matchingIds = toIdSet(ServiceRegistry.bookingService().filterByStatus(BookingStatus.valueOf(status)));
            }

            String masterName = masterFilterCombo.getValue();
            if (masterName != null && !ALL.equals(masterName)) {
                Optional<Client> master = ServiceRegistry.clientService().findAll().stream()
                        .filter(c -> c.getName().equals(masterName))
                        .findFirst();
                if (master.isPresent()) {
                    java.util.Set<java.util.UUID> byMaster =
                            toIdSet(ServiceRegistry.bookingService().filterByMaster(master.get().getId()));
                    matchingIds = intersect(matchingIds, byMaster);
                }
            }

            LocalDate from = dateFromPicker.getValue();
            LocalDate to = dateToPicker.getValue();
            if (from != null && to != null) {
                java.util.Set<java.util.UUID> byDate = toIdSet(ServiceRegistry.bookingService()
                        .filterByDateRange(from.atStartOfDay(), to.atTime(23, 59)));
                matchingIds = intersect(matchingIds, byDate);
            }

            if (matchingIds == null) {
                showRows(all); //ни один фильтр не выбран
            } else {
                java.util.Set<java.util.UUID> finalIds = matchingIds;
                showRows(all.stream().filter(b -> finalIds.contains(b.getId())).toList());
            }
        } catch (BusinessException e) {
            messageLabel.setText("Ошибка фильтрации: " + e.getMessage());
        }
    }

    private java.util.Set<java.util.UUID> toIdSet(List<Booking> bookings) {
        return bookings.stream().map(Booking::getId).collect(java.util.stream.Collectors.toSet());
    }

    private java.util.Set<java.util.UUID> intersect(java.util.Set<java.util.UUID> current, java.util.Set<java.util.UUID> next) {
        if (current == null) return next;
        current.retainAll(next);
        return current;
    }

    @FXML
    private void onResetFilter() {
        statusFilterCombo.setValue(ALL);
        masterFilterCombo.setValue(ALL);
        dateFromPicker.setValue(null);
        dateToPicker.setValue(null);
        loadAll();
    }

    //сортировка

    @FXML
    private void onSortByTime() {
        List<Booking> current = rows.stream().map(BookingRow::getBooking).toList();
        showRows(ServiceRegistry.bookingService().sortByTime(current));
    }

    @FXML
    private void onSortByStatus() {
        List<Booking> current = rows.stream().map(BookingRow::getBooking).toList();
        showRows(ServiceRegistry.bookingService().sortByStatus(current));
    }

    //crud

    @FXML
    private void onAdd() {
        Optional<Booking> result = BookingDialogController.open(bookingTable.getScene().getWindow(), null);
        result.ifPresent(b -> loadAll());
    }

    @FXML
    private void onEdit() {
        BookingRow selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Выберите запись в таблице.");
            return;
        }
        Optional<Booking> result = BookingDialogController.open(bookingTable.getScene().getWindow(), selected.getBooking());
        result.ifPresent(b -> loadAll());
    }

    @FXML
    private void onDelete() {
        BookingRow selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Выберите запись в таблице.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить запись клиента \"" + selected.getClientName() + "\" на " +
                        selected.getTime().format(TIME_FMT) + "?");
        confirm.setHeaderText("Подтверждение удаления");
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isPresent() && answer.get() == ButtonType.OK) {
            try {
                ServiceRegistry.bookingService().delete(selected.getId());
                loadAll();
            } catch (BusinessException | EntityNotFoundException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    private void onRefresh() {
        reloadFilterCombos();
        loadAll();
    }

    //экспорт
    @FXML
    private void onExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить экспорт записей");
        chooser.setInitialFileName("bookings.xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        java.io.File file = chooser.showSaveDialog(bookingTable.getScene().getWindow());
        if (file == null) return;

        try {
            ServiceRegistry.excelExporter().exportBookings(ServiceRegistry.bookingService().findAll(), file.getAbsolutePath());
            new Alert(Alert.AlertType.INFORMATION, "Экспорт завершён: " + file.getName()).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка экспорта: " + e.getMessage()).showAndWait();
        }
    }
}
