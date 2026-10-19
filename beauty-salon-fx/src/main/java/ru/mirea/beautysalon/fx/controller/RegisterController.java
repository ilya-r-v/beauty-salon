package ru.mirea.beautysalon.fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.fx.Navigator;
import ru.mirea.beautysalon.fx.ServiceRegistry;
import ru.mirea.beautysalon.fx.Session;
import ru.mirea.beautysalon.model.Client;

// Экран регистрации. Все проверки (формат телефона, уникальность, длина пароля)
public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;

    @FXML
    private void onRegister() {
        errorLabel.setText(null);
        try {
            Client user = ServiceRegistry.authService().register(
                    nameField.getText(), phoneField.getText(),
                    passwordField.getText(), confirmField.getText());
            // После успешной регистрации сразу входим под новым пользователем
            Session.login(user, ServiceRegistry.authService().roleName(user));
            Navigator.showMain();
        } catch (BusinessException e) {
            errorLabel.setText(e.getMessage());
        } catch (RuntimeException e) {
            errorLabel.setText("Ошибка подключения к БД: " + e.getMessage());
        }
    }

    @FXML
    private void onBack() {
        Navigator.showLogin();
    }
}
