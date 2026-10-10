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
import ru.mirea.beautysalon.model.ServiceEntity;
import ru.mirea.beautysalon.model.ServiceType;

import java.io.IOException;
import java.util.Optional;

public class ServiceDialogController {

    @FXML private TextField titleField;
    @FXML private ComboBox<Client> masterCombo;
    @FXML private ComboBox<ServiceType> typeCombo;
    @FXML private Label errorLabel;

    private boolean saved = false;
    private ServiceEntity result;

    @FXML
    private void initialize() {
        ServiceRegistry.clientService().findAll().stream()
                .filter(c -> ServiceRegistry.clientService().isMaster(c.getId()))
                .forEach(masterCombo.getItems()::add);
        typeCombo.getItems().addAll(ServiceRegistry.serviceCatalogService().findAllTypes());
    }

    public static Optional<ServiceEntity> open(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(ServiceDialogController.class.getResource("/fxml/service-dialog.fxml"));
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
    private void onSave() {
        errorLabel.setText(null);
        String title = titleField.getText() == null ? null : titleField.getText().trim();
        Client master = masterCombo.getValue();
        ServiceType type = typeCombo.getValue();

        if (master == null || type == null) {
            errorLabel.setText("Выберите мастера и тип услуги.");
            return;
        }

        try {
            ServiceEntity service = new ServiceEntity(null, title, master.getId(), type.getId());
            result = ServiceRegistry.serviceCatalogService().createService(service);
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
