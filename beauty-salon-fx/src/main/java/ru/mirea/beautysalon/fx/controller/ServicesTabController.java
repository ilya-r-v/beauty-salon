package ru.mirea.beautysalon.fx.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.fx.ServiceRegistry;
import ru.mirea.beautysalon.model.ServiceEntity;
import ru.mirea.beautysalon.model.ServiceType;
import ru.mirea.beautysalon.model.ServiceVariant;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ServicesTabController {

    @FXML private ComboBox<String> masterFilterCombo;
    @FXML private ComboBox<String> typeFilterCombo;

    @FXML private TableView<ServiceEntity> serviceTable;
    @FXML private TableColumn<ServiceEntity, String> serviceTitleColumn;
    @FXML private TableColumn<ServiceEntity, String> serviceMasterColumn;
    @FXML private TableColumn<ServiceEntity, String> serviceTypeColumn;

    @FXML private TableView<ServiceVariant> variantTable;
    @FXML private TableColumn<ServiceVariant, String> variantTitleColumn;
    @FXML private TableColumn<ServiceVariant, String> variantServiceColumn;
    @FXML private TableColumn<ServiceVariant, String> variantPriceColumn;

    @FXML private Label messageLabel;

    private static final String ALL = "Все";
    private final ObservableList<ServiceEntity> serviceRows = FXCollections.observableArrayList();
    private final ObservableList<ServiceVariant> variantRows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        serviceTitleColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        serviceMasterColumn.setCellValueFactory(d -> new SimpleStringProperty(masterName(d.getValue().getMasterId())));
        serviceTypeColumn.setCellValueFactory(d -> new SimpleStringProperty(typeName(d.getValue().getServiceTypeId())));
        serviceTable.setItems(serviceRows);

        variantTitleColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        variantServiceColumn.setCellValueFactory(d -> new SimpleStringProperty(serviceTitle(d.getValue().getServiceId())));
        variantPriceColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrice() + " руб."));
        variantTable.setItems(variantRows);

        reloadFilterCombos();
        loadAll();
    }

    private void reloadFilterCombos() {
        masterFilterCombo.getItems().clear();
        masterFilterCombo.getItems().add(ALL);
        ServiceRegistry.clientService().findAll().stream()
                .filter(c -> ServiceRegistry.clientService().isMaster(c.getId()))
                .forEach(c -> masterFilterCombo.getItems().add(c.getName()));
        masterFilterCombo.setValue(ALL);

        typeFilterCombo.getItems().clear();
        typeFilterCombo.getItems().add(ALL);
        ServiceRegistry.serviceCatalogService().findAllTypes().forEach(t -> typeFilterCombo.getItems().add(t.getName()));
        typeFilterCombo.setValue(ALL);
    }

    private String masterName(UUID masterId) {
        try {
            return ServiceRegistry.clientService().getById(masterId).getName();
        } catch (EntityNotFoundException e) {
            return "—";
        }
    }

    private String typeName(UUID typeId) {
        return ServiceRegistry.serviceCatalogService().findAllTypes().stream()
                .filter(t -> t.getId().equals(typeId)).map(ServiceType::getName).findFirst().orElse("—");
    }

    private String serviceTitle(UUID serviceId) {
        try {
            return ServiceRegistry.serviceCatalogService().getServiceById(serviceId).getTitle();
        } catch (EntityNotFoundException e) {
            return "—";
        }
    }

    private void loadAll() {
        serviceRows.setAll(ServiceRegistry.serviceCatalogService().findAllServices());
        variantRows.setAll(ServiceRegistry.serviceCatalogService().findAllVariants());
        messageLabel.setText(serviceRows.size() + " услуг(и), " + variantRows.size() + " вариант(ов).");
    }

    @FXML
    private void onApplyFilter() {
        List<ServiceEntity> result = ServiceRegistry.serviceCatalogService().findAllServices();

        String masterName = masterFilterCombo.getValue();
        if (masterName != null && !ALL.equals(masterName)) {
            Optional<UUID> masterId = ServiceRegistry.clientService().findAll().stream()
                    .filter(c -> c.getName().equals(masterName)).map(c -> c.getId()).findFirst();
            if (masterId.isPresent()) {
                result = ServiceRegistry.serviceCatalogService().filterByMaster(masterId.get());
            }
        }

        String typeName = typeFilterCombo.getValue();
        if (typeName != null && !ALL.equals(typeName)) {
            Optional<UUID> typeId = ServiceRegistry.serviceCatalogService().findAllTypes().stream()
                    .filter(t -> t.getName().equals(typeName)).map(t -> t.getId()).findFirst();
            if (typeId.isPresent()) {
                var byType = ServiceRegistry.serviceCatalogService().filterByType(typeId.get());
                var ids = byType.stream().map(ServiceEntity::getId).collect(java.util.stream.Collectors.toSet());
                result = result.stream().filter(s -> ids.contains(s.getId())).toList();
            }
        }

        serviceRows.setAll(result);
    }

    @FXML
    private void onResetFilter() {
        masterFilterCombo.setValue(ALL);
        typeFilterCombo.setValue(ALL);
        loadAll();
    }

    @FXML
    private void onAddType() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Новый тип услуги");
        dialog.setHeaderText(null);
        dialog.setContentText("Название типа:");
        Optional<String> name = dialog.showAndWait();
        if (name.isEmpty()) return;

        try {
            ServiceRegistry.serviceCatalogService().createType(name.get());
            reloadFilterCombos();
            messageLabel.setText("Тип услуги создан.");
        } catch (BusinessException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onAddService() {
        Optional<ServiceEntity> result = ServiceDialogController.open(serviceTable.getScene().getWindow());
        result.ifPresent(s -> loadAll());
    }

    @FXML
    private void onDeleteService() {
        ServiceEntity selected = serviceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Выберите услугу в таблице.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Удалить услугу \"" + selected.getTitle() + "\"?");
        confirm.setHeaderText("Подтверждение удаления");
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isPresent() && answer.get() == ButtonType.OK) {
            try {
                ServiceRegistry.serviceCatalogService().deleteService(selected.getId());
                loadAll();
            } catch (BusinessException | EntityNotFoundException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    private void onAddVariant() {
        Optional<ServiceVariant> result = VariantDialogController.open(variantTable.getScene().getWindow());
        result.ifPresent(v -> loadAll());
    }

    @FXML
    private void onDeleteVariant() {
        ServiceVariant selected = variantTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Выберите вариант в таблице.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Удалить вариант \"" + selected.getTitle() + "\"?");
        confirm.setHeaderText("Подтверждение удаления");
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isPresent() && answer.get() == ButtonType.OK) {
            try {
                ServiceRegistry.serviceCatalogService().deleteVariant(selected.getId());
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
}
