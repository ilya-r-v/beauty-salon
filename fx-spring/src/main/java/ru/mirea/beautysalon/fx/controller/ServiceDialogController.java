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
import ru.mirea.beautysalon.client.dto.MasterResponse;
import ru.mirea.beautysalon.client.dto.ServiceResponse;
import ru.mirea.beautysalon.client.dto.ServiceTypeResponse;
import ru.mirea.beautysalon.fx.ApiRegistry;

import java.io.IOException;
import java.util.Optional;

// Диалог создания услуги.
public class ServiceDialogController {

    @FXML private TextField titleField;
    @FXML private ComboBox<MasterResponse> masterCombo;
    @FXML private ComboBox<ServiceTypeResponse> typeCombo;
    @FXML private Label errorLabel;

    private boolean saved = false;
    private ServiceResponse result;

    public static Optional<ServiceResponse> open(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ServiceDialogController.class.getResource("/fxml/service-dialog.fxml"));
            Parent root = loader.load();
            ServiceDialogController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Новая услуга");
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            return controller.saved ? Optional.of(controller.result) : Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось открыть диалог услуги: " + e.getMessage(), e);
        }
    }

    @FXML
    private void initialize() {
        try {
            masterCombo.getItems().addAll(ApiRegistry.api().getMasters());
        } catch (ApiException e) {
            errorLabel.setText("Ошибка загрузки мастеров: " + e.getMessage());
        }
        masterCombo.setButtonCell(masterCell());
        masterCombo.setCellFactory(lv -> masterCell());

        try {
            typeCombo.getItems().addAll(ApiRegistry.api().getServiceTypes());
        } catch (ApiException e) {
            errorLabel.setText("Ошибка загрузки типов: " + e.getMessage());
        }
        typeCombo.setButtonCell(typeCell());
        typeCombo.setCellFactory(lv -> typeCell());
    }

    @FXML
    private void onSave() {
        errorLabel.setText(null);
        String title = titleField.getText().trim();
        MasterResponse master = masterCombo.getValue();
        ServiceTypeResponse type = typeCombo.getValue();

        if (title.isEmpty() || master == null || type == null) {
            errorLabel.setText("Заполните все поля.");
            return;
        }

        try {
            result = ApiRegistry.api().createService(title, master.clientId(), type.id());
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

    private ListCell<MasterResponse> masterCell() {
        return new ListCell<>() {
            @Override protected void updateItem(MasterResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name());
            }
        };
    }

    private ListCell<ServiceTypeResponse> typeCell() {
        return new ListCell<>() {
            @Override protected void updateItem(ServiceTypeResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name());
            }
        };
    }
}
