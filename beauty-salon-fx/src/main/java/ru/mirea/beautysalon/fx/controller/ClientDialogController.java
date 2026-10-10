package ru.mirea.beautysalon.fx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.fx.ServiceRegistry;
import ru.mirea.beautysalon.model.Client;
import ru.mirea.beautysalon.model.Role;

import java.io.IOException;
import java.util.Optional;

public class ClientDialogController {

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<Role> roleCombo;
    @FXML private Label errorLabel;

    private Client editingClient;
    private boolean saved = false;

    @FXML
    private void initialize() {
        roleCombo.getItems().addAll(ServiceRegistry.roleRepository().findAll());
    }

    public static Optional<Client> open(Window owner, Client existing) {
        try {
            FXMLLoader loader = new FXMLLoader(ClientDialogController.class.getResource("/fxml/client-dialog.fxml"));
            Parent root = loader.load();
            ClientDialogController controller = loader.getController();
            controller.setEditingClient(existing);

            Stage stage = new Stage();
            stage.setTitle(existing == null ? "Новый клиент" : "Редактирование клиента");
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            return controller.saved ? Optional.of(controller.editingClient) : Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось открыть диалог клиента: " + e.getMessage(), e);
        }
    }

    private void setEditingClient(Client existing) {
        if (existing != null) {
            this.editingClient = existing;
            nameField.setText(existing.getName());
            phoneField.setText(existing.getPhone());
            roleCombo.getItems().stream().filter(r -> r.getId().equals(existing.getRoleId()))
                    .findFirst().ifPresent(roleCombo::setValue);
        } else {
            this.editingClient = new Client();
            this.editingClient.setPasswordHash("changeme");
        }
    }

    @FXML
    private void onSave() {
        errorLabel.setText(null);
        editingClient.setName(nameField.getText() == null ? null : nameField.getText().trim());
        editingClient.setPhone(phoneField.getText() == null ? null : phoneField.getText().trim());
        Role role = roleCombo.getValue();
        editingClient.setRoleId(role == null ? null : role.getId());

        try {
            if (editingClient.getId() == null) {
                ServiceRegistry.clientService().create(editingClient);
            } else {
                ServiceRegistry.clientService().update(editingClient);
            }
            saved = true;
            ((Stage) errorLabel.getScene().getWindow()).close();
        } catch (BusinessException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        saved = false;
        ((Stage) errorLabel.getScene().getWindow()).close();
    }
}
