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

//Экран входа телефон пароль проверка выполняется в AuthService
public class LoginController {

    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void onLogin() {
        errorLabel.setText(null);
        try {
            Client user = ServiceRegistry.authService().login(phoneField.getText(), passwordField.getText());
            Session.login(user, ServiceRegistry.authService().roleName(user));
            Navigator.showMain();
        } catch (BusinessException e) {
            errorLabel.setText(e.getMessage());
            passwordField.clear();
        } catch (RuntimeException e) {
            errorLabel.setText("Ошибка подключения к БД: " + e.getMessage());
        }
    }

    @FXML
    private void onOpenRegister() {
        Navigator.showRegister();
    }
}
