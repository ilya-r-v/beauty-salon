package ru.mirea.beautysalon.fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ru.mirea.beautysalon.client.ApiException;
import ru.mirea.beautysalon.client.dto.AuthResponse;
import ru.mirea.beautysalon.fx.ApiRegistry;
import ru.mirea.beautysalon.fx.AppState;
import ru.mirea.beautysalon.fx.BeautySalonFxApplication;

import java.io.IOException;

public class LoginController {

    @FXML private TabPane tabPane;
    @FXML private TextField loginPhoneField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Label loginErrorLabel;

    @FXML private TextField regNameField;
    @FXML private TextField regPhoneField;
    @FXML private PasswordField regPasswordField;
    @FXML private Label regErrorLabel;

    @FXML
    private void onLogin() {
        loginErrorLabel.setText(null);
        String phone = loginPhoneField.getText().trim();
        String password = loginPasswordField.getText();

        if (phone.isEmpty() || password.isEmpty()) {
            loginErrorLabel.setText("Введите телефон и пароль.");
            return;
        }

        try {
            AuthResponse response = ApiRegistry.api().login(phone, password);
            AppState.current().login(response);
            openMain();
        } catch (ApiException e) {
            loginErrorLabel.setText(e.getMessage());
        } catch (IOException e) {
            loginErrorLabel.setText("Ошибка открытия главного окна: " + e.getMessage());
        }
    }

    @FXML
    private void onRegister() {
        regErrorLabel.setText(null);
        String name = regNameField.getText().trim();
        String phone = regPhoneField.getText().trim();
        String password = regPasswordField.getText();

        if (name.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            regErrorLabel.setText("Заполните все поля.");
            return;
        }

        try {
            AuthResponse response = ApiRegistry.api().register(name, phone, password);
            AppState.current().login(response);
            openMain();
        } catch (ApiException e) {
            regErrorLabel.setText(e.getMessage());
        } catch (IOException e) {
            regErrorLabel.setText("Ошибка открытия главного окна: " + e.getMessage());
        }
    }

    private void openMain() throws IOException {
        Stage stage = (Stage) tabPane.getScene().getWindow();
        BeautySalonFxApplication.showMain(stage);
    }
}
