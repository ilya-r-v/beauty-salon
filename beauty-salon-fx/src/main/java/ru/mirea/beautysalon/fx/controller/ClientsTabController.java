package ru.mirea.beautysalon.fx.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.fx.ServiceRegistry;
import ru.mirea.beautysalon.model.Client;
import ru.mirea.beautysalon.model.Role;

import java.util.List;
import java.util.Optional;

public class ClientsTabController {

    @FXML private ChoiceBox<String> searchTypeChoice;
    @FXML private TextField searchField;

    @FXML private TableView<Client> clientTable;
    @FXML private TableColumn<Client, String> nameColumn;
    @FXML private TableColumn<Client, String> phoneColumn;
    @FXML private TableColumn<Client, String> roleColumn;
    @FXML private TableColumn<Client, String> masterColumn;

    @FXML private Label messageLabel;

    private final ObservableList<Client> rows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        phoneColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));
        roleColumn.setCellValueFactory(d -> new SimpleStringProperty(roleName(d.getValue().getRoleId())));
        masterColumn.setCellValueFactory(d -> new SimpleStringProperty(
                ServiceRegistry.clientService().isMaster(d.getValue().getId()) ? "да" : "нет"));
        clientTable.setItems(rows);

        searchTypeChoice.getItems().addAll("По имени", "По телефону");
        searchTypeChoice.setValue("По имени");

        loadAll();
    }

    private String roleName(java.util.UUID roleId) {
        return ServiceRegistry.roleRepository().findById(roleId).map(Role::getName).orElse("—");
    }

    private void loadAll() {
        List<Client> clients = ServiceRegistry.clientService().findAll();
        rows.setAll(clients);
        messageLabel.setText(rows.isEmpty() ? "Клиентов нет." : rows.size() + " клиент(ов).");
    }

    @FXML
    private void onSearch() {
        String text = searchField.getText() == null ? "" : searchField.getText().trim();
        if (text.isEmpty()) {
            messageLabel.setText("Введите текст для поиска.");
            return;
        }
        List<Client> result = "По имени".equals(searchTypeChoice.getValue())
                ? ServiceRegistry.clientService().searchByName(text)
                : ServiceRegistry.clientService().searchByPhone(text);
        rows.setAll(result);
        messageLabel.setText(rows.isEmpty() ? "Ничего не найдено." : rows.size() + " результат(ов).");
    }

    @FXML
    private void onResetSearch() {
        searchField.clear();
        loadAll();
    }

    @FXML
    private void onAdd() {
        Optional<Client> result = ClientDialogController.open(clientTable.getScene().getWindow(), null);
        result.ifPresent(c -> loadAll());
    }

    @FXML
    private void onEdit() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Выберите клиента в таблице.");
            return;
        }
        Optional<Client> result = ClientDialogController.open(clientTable.getScene().getWindow(), selected);
        result.ifPresent(c -> loadAll());
    }

    @FXML
    private void onDelete() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Выберите клиента в таблице.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Удалить клиента \"" + selected.getName() + "\"?");
        confirm.setHeaderText("Подтверждение удаления");
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isPresent() && answer.get() == ButtonType.OK) {
            try {
                ServiceRegistry.clientService().delete(selected.getId());
                loadAll();
            } catch (BusinessException | EntityNotFoundException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    private void onPromote() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Выберите клиента в таблице.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Сделать мастером");
        dialog.setHeaderText("Клиент: " + selected.getName());
        dialog.setContentText("Описание мастера:");
        Optional<String> description = dialog.showAndWait();
        if (description.isEmpty()) return;

        try {
            ServiceRegistry.clientService().promoteToMaster(selected.getId(), description.get());
            loadAll();
        } catch (BusinessException | EntityNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onRefresh() {
        loadAll();
    }
}
