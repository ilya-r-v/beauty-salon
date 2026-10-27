package ru.mirea.beautysalon.fx.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.mirea.beautysalon.client.ApiException;
import ru.mirea.beautysalon.client.dto.ClientResponse;
import ru.mirea.beautysalon.fx.ApiRegistry;

import java.util.List;
import java.util.Optional;

public class ClientsTabController {

    @FXML private ChoiceBox<String> searchTypeChoice;
    @FXML private TextField searchField;

    @FXML private TableView<ClientResponse> clientTable;
    @FXML private TableColumn<ClientResponse, String> nameColumn;
    @FXML private TableColumn<ClientResponse, String> phoneColumn;
    @FXML private TableColumn<ClientResponse, String> roleColumn;
    @FXML private TableColumn<ClientResponse, String> masterColumn;

    @FXML private Label messageLabel;

    private final ObservableList<ClientResponse> rows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        phoneColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().phone()));
        roleColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().role()));
        masterColumn.setCellValueFactory(d -> new SimpleStringProperty(
                Boolean.TRUE.equals(d.getValue().isMaster()) ? "да" : "нет"));
        clientTable.setItems(rows);

        searchTypeChoice.getItems().addAll("По имени", "По телефону");
        searchTypeChoice.setValue("По имени");

        loadAll();
    }

    private void loadAll() {
        try {
            List<ClientResponse> clients = ApiRegistry.api().getClients();
            rows.setAll(clients);
            showMessage(rows.isEmpty() ? "Клиентов нет." : rows.size() + " клиент(ов).");
        } catch (ApiException e) {
            showMessage("Ошибка загрузки: " + e.getMessage());
        }
    }

    @FXML
    private void onSearch() {
        String text = searchField.getText() == null ? "" : searchField.getText().trim();
        if (text.isEmpty()) { showMessage("Введите текст для поиска."); return; }

        String by = "По имени".equals(searchTypeChoice.getValue()) ? "name" : "phone";
        try {
            List<ClientResponse> result = ApiRegistry.api().searchClients(text, by);
            rows.setAll(result);
            showMessage(rows.isEmpty() ? "Ничего не найдено." : rows.size() + " результат(ов).");
        } catch (ApiException e) {
            showMessage("Ошибка поиска: " + e.getMessage());
        }
    }

    @FXML
    private void onResetSearch() {
        searchField.clear();
        loadAll();
    }

    @FXML
    private void onAdd() {
        Optional<ClientResponse> result = ClientDialogController.open(clientTable.getScene().getWindow(), null);
        result.ifPresent(c -> loadAll());
    }

    @FXML
    private void onEdit() {
        ClientResponse selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage("Выберите клиента в таблице."); return; }
        Optional<ClientResponse> result = ClientDialogController.open(clientTable.getScene().getWindow(), selected);
        result.ifPresent(c -> loadAll());
    }

    @FXML
    private void onDelete() {
        ClientResponse selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage("Выберите клиента в таблице."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Удалить клиента \"" + selected.name() + "\"?");
        confirm.setHeaderText("Подтверждение удаления");
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isPresent() && answer.get() == ButtonType.OK) {
            try {
                ApiRegistry.api().deleteClient(selected.id());
                loadAll();
            } catch (ApiException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    private void onPromote() {
        ClientResponse selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage("Выберите клиента в таблице."); return; }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Сделать мастером");
        dialog.setHeaderText("Клиент: " + selected.name());
        dialog.setContentText("Описание мастера:");
        Optional<String> description = dialog.showAndWait();
        if (description.isEmpty()) return;

        try {
            ApiRegistry.api().promoteToMaster(selected.id(), description.get());
            loadAll();
        } catch (ApiException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onRefresh() {
        loadAll();
    }

    private void showMessage(String text) {
        messageLabel.setText(text);
    }
}
