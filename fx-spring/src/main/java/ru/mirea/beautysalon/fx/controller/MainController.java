package ru.mirea.beautysalon.fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ru.mirea.beautysalon.fx.AppState;

public class MainController {

    @FXML private Label userLabel;

    @FXML
    private void initialize() {
        var profile = AppState.current().getProfile();
        if (profile != null) {
            userLabel.setText(profile.name() + " (" + profile.role() + ")");
        }
    }
}
