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
import ru.mirea.beautysalon.client.dto.ClientResponse;
import ru.mirea.beautysalon.fx.ApiRegistry;

import java.io.IOException;
import java.util.Optional;

public class ClientDialogController {

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordHint;
    @FXML private Label errorLabel;

    private ClientResponse editingClient;
    private boolean saved = false;
    private ClientResponse result;

    public static Optional<ClientResponse> open(Window owner, ClientResponse existing) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ClientDialogController.class.getResource("/fxml/client-dialog.fxml"));
            Parent root = loader.load();
            ClientDialogController controller = loader.getController();
            controller.setClient(existing);

            Stage stage = new Stage();
            stage.setTitle(existing == null ? "Новый клиент" : "Редактирование клиента");
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            return controller.saved ? Optional.of(controller.result) : Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось открыть диалог клиента: " + e.getMessage(), e);
        }
    }

    private void setClient(ClientResponse client) {
        this.editingClient = client;
        if (client != null) {
            nameField.setText(client.name());
            phoneField.setText(client.phone());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordHint.setVisible(true);
            passwordHint.setManaged(true);
        }
    }

    @FXML
    private void onSave() {
        errorLabel.setText(null);
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            errorLabel.setText("Имя и телефон обязательны.");
            return;
        }

        try {
            if (editingClient == null) {
                String password = passwordField.getText();
                if (password.isEmpty()) {
                    errorLabel.setText("Введите пароль.");
                    return;
                }
                result = ApiRegistry.api().createClient(name, phone, password);
            } else {
                result = ApiRegistry.api().updateClient(editingClient.id(), name, phone);
            }
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
}
