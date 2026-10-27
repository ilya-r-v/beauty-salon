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
import ru.mirea.beautysalon.client.dto.ServiceResponse;
import ru.mirea.beautysalon.client.dto.VariantResponse;
import ru.mirea.beautysalon.fx.ApiRegistry;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

public class VariantDialogController {

    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private TextField priceField;
    @FXML private ComboBox<ServiceResponse> serviceCombo;
    @FXML private Label errorLabel;

    private boolean saved = false;
    private VariantResponse result;

    public static Optional<VariantResponse> open(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    VariantDialogController.class.getResource("/fxml/variant-dialog.fxml"));
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
            throw new RuntimeException("Не удалось открыть диалог варианта: " + e.getMessage(), e);
        }
    }

    @FXML
    private void initialize() {
        try {
            serviceCombo.getItems().addAll(ApiRegistry.api().getServices());
        } catch (ApiException e) {
            errorLabel.setText("Ошибка загрузки услуг: " + e.getMessage());
        }
        serviceCombo.setButtonCell(serviceCell());
        serviceCombo.setCellFactory(lv -> serviceCell());
    }

    @FXML
    private void onSave() {
        errorLabel.setText(null);
        String title = titleField.getText().trim();
        String description = descriptionField.getText().trim();
        String priceText = priceField.getText().trim();
        ServiceResponse service = serviceCombo.getValue();

        if (title.isEmpty() || priceText.isEmpty() || service == null) {
            errorLabel.setText("Заполните название, цену и выберите услугу.");
            return;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceText.replace(',', '.'));
            if (price.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            errorLabel.setText("Цена должна быть положительным числом.");
            return;
        }

        try {
            result = ApiRegistry.api().createVariant(title, description.isEmpty() ? null : description,
                    price, service.id());
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

    private ListCell<ServiceResponse> serviceCell() {
        return new ListCell<>() {
            @Override protected void updateItem(ServiceResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.title() + " (" + item.masterName() + ")");
            }
        };
    }
}
