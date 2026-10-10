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
import ru.mirea.beautysalon.model.ServiceEntity;
import ru.mirea.beautysalon.model.ServiceVariant;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

public class VariantDialogController {

    @FXML private ComboBox<ServiceEntity> serviceCombo;
    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private TextField priceField;
    @FXML private Label errorLabel;

    private boolean saved = false;
    private ServiceVariant result;

    @FXML
    private void initialize() {
        serviceCombo.getItems().addAll(ServiceRegistry.serviceCatalogService().findAllServices());
    }

    public static Optional<ServiceVariant> open(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(VariantDialogController.class.getResource("/fxml/variant-dialog.fxml"));
            Parent root = loader.load();
            VariantDialogController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Новый вариант услуги");
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            return controller.saved ? Optional.of(controller.result) : Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось открыть диалог варианта услуги: " + e.getMessage(), e);
        }
    }

    @FXML
    private void onSave() {
        errorLabel.setText(null);
        ServiceEntity service = serviceCombo.getValue();
        String title = titleField.getText() == null ? null : titleField.getText().trim();
        String description = descriptionField.getText() == null ? "" : descriptionField.getText().trim();
        String priceText = priceField.getText() == null ? "" : priceField.getText().trim();

        if (service == null) {
            errorLabel.setText("Выберите услугу.");
            return;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceText);
        } catch (NumberFormatException e) {
            errorLabel.setText("Цена должна быть числом, например 1500 или 1500.50.");
            return;
        }

        try {
            ServiceVariant variant = new ServiceVariant(null, title, description, price, service.getId());
            result = ServiceRegistry.serviceCatalogService().createVariant(variant);
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
