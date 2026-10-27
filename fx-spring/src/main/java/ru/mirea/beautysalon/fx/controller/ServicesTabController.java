package ru.mirea.beautysalon.fx.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.mirea.beautysalon.client.ApiException;
import ru.mirea.beautysalon.client.dto.MasterResponse;
import ru.mirea.beautysalon.client.dto.ServiceResponse;
import ru.mirea.beautysalon.client.dto.ServiceTypeResponse;
import ru.mirea.beautysalon.client.dto.VariantResponse;
import ru.mirea.beautysalon.fx.ApiRegistry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ServicesTabController {

    @FXML private ComboBox<MasterResponse> masterFilterCombo;
    @FXML private ComboBox<ServiceTypeResponse> typeFilterCombo;

    @FXML private TableView<ServiceResponse> serviceTable;
    @FXML private TableColumn<ServiceResponse, String> serviceTitleColumn;
    @FXML private TableColumn<ServiceResponse, String> serviceMasterColumn;
    @FXML private TableColumn<ServiceResponse, String> serviceTypeColumn;

    @FXML private TableView<VariantResponse> variantTable;
    @FXML private TableColumn<VariantResponse, String> variantTitleColumn;
    @FXML private TableColumn<VariantResponse, String> variantServiceColumn;
    @FXML private TableColumn<VariantResponse, String> variantPriceColumn;

    @FXML private Label messageLabel;

    private static final String ALL = "Все";
    private final ObservableList<ServiceResponse> serviceRows = FXCollections.observableArrayList();
    private final ObservableList<VariantResponse> variantRows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        serviceTitleColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().title()));
        serviceMasterColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().masterName()));
        serviceTypeColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().serviceTypeName()));
        serviceTable.setItems(serviceRows);

        variantTitleColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().title()));
        variantServiceColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().serviceTitle()));
        variantPriceColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().price() + " руб."));
        variantTable.setItems(variantRows);

        reloadFilterCombos();
        loadAll();
    }

    private void reloadFilterCombos() {
        masterFilterCombo.getItems().clear();
        masterFilterCombo.getItems().add(null);
        try { masterFilterCombo.getItems().addAll(ApiRegistry.api().getMasters()); }
        catch (ApiException e) { showMessage("Ошибка загрузки мастеров: " + e.getMessage()); }
        masterFilterCombo.setValue(null);
        masterFilterCombo.setButtonCell(masterCell());
        masterFilterCombo.setCellFactory(lv -> masterCell());

        typeFilterCombo.getItems().clear();
        typeFilterCombo.getItems().add(null);
        try { typeFilterCombo.getItems().addAll(ApiRegistry.api().getServiceTypes()); }
        catch (ApiException e) { showMessage("Ошибка загрузки типов: " + e.getMessage()); }
        typeFilterCombo.setValue(null);
        typeFilterCombo.setButtonCell(typeCell());
        typeFilterCombo.setCellFactory(lv -> typeCell());
    }

    private void loadAll() {
        try {
            serviceRows.setAll(ApiRegistry.api().getServices());
            variantRows.setAll(ApiRegistry.api().getVariants());
            showMessage(serviceRows.size() + " услуг(и), " + variantRows.size() + " вариант(ов).");
        } catch (ApiException e) {
            showMessage("Ошибка загрузки: " + e.getMessage());
        }
    }

    @FXML
    private void onApplyFilter() {
        MasterResponse master = masterFilterCombo.getValue();
        ServiceTypeResponse type = typeFilterCombo.getValue();
        UUID masterId = master != null ? master.clientId() : null;
        UUID typeId = type != null ? type.id() : null;
        try {
            serviceRows.setAll(ApiRegistry.api().getServices(masterId, typeId));
        } catch (ApiException e) {
            showMessage("Ошибка фильтрации: " + e.getMessage());
        }
    }

    @FXML
    private void onResetFilter() {
        masterFilterCombo.setValue(null);
        typeFilterCombo.setValue(null);
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
            ApiRegistry.api().createServiceType(name.get().trim());
            reloadFilterCombos();
            showMessage("Тип услуги создан.");
        } catch (ApiException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onAddService() {
        Optional<ServiceResponse> result = ServiceDialogController.open(serviceTable.getScene().getWindow());
        result.ifPresent(s -> loadAll());
    }

    @FXML
    private void onDeleteService() {
        ServiceResponse selected = serviceTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage("Выберите услугу в таблице."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Удалить услугу \"" + selected.title() + "\"?");
        confirm.setHeaderText("Подтверждение удаления");
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isPresent() && answer.get() == ButtonType.OK) {
            try {
                ApiRegistry.api().deleteService(selected.id());
                loadAll();
            } catch (ApiException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    private void onAddVariant() {
        Optional<VariantResponse> result = VariantDialogController.open(variantTable.getScene().getWindow());
        result.ifPresent(v -> loadAll());
    }

    @FXML
    private void onDeleteVariant() {
        VariantResponse selected = variantTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage("Выберите вариант в таблице."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Удалить вариант \"" + selected.title() + "\"?");
        confirm.setHeaderText("Подтверждение удаления");
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isPresent() && answer.get() == ButtonType.OK) {
            try {
                ApiRegistry.api().deleteVariant(selected.id());
                loadAll();
            } catch (ApiException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    private void onRefresh() {
        reloadFilterCombos();
        loadAll();
    }

    private ListCell<MasterResponse> masterCell() {
        return new ListCell<>() {
            @Override protected void updateItem(MasterResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? ALL : item.name()));
            }
        };
    }

    private ListCell<ServiceTypeResponse> typeCell() {
        return new ListCell<>() {
            @Override protected void updateItem(ServiceTypeResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? ALL : item.name()));
            }
        };
    }

    private void showMessage(String text) {
        messageLabel.setText(text);
    }
}
