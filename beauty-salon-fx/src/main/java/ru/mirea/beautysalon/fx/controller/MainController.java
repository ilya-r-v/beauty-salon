package ru.mirea.beautysalon.fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ru.mirea.beautysalon.fx.Navigator;
import ru.mirea.beautysalon.fx.Session;


// контроллер главного окна показывает кто вошёл и обрабатывает выход
public class MainController {

    @FXML private Label userLabel;

    @FXML
    private void initialize() {
        if (Session.isLoggedIn()) {
            userLabel.setText(Session.currentUser().getName() + " (" + Session.currentRole() + ")");
        }
    }

    @FXML
    private void onLogout() {
        Session.logout();
        Navigator.showLogin();
    }
}
