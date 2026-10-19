package ru.mirea.beautysalon.fx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

//Переключает экраны в одном главном окне
public final class Navigator {

    private static Stage stage;

    private Navigator() {}

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void showLogin() {
        show("/fxml/login.fxml", "Салон красоты — вход", 420, 380, false);
    }

    public static void showRegister() {
        show("/fxml/register.fxml", "Салон красоты — регистрация", 420, 480, false);
    }

    public static void showMain() {
        show("/fxml/main.fxml", "Салон красоты — КР2 (JavaFX)", 1150, 720, true);
        stage.setMinWidth(950);
        stage.setMinHeight(600);
    }

    private static void show(String fxml, String title, double width, double height, boolean resizable) {
        try {
            Parent root = new FXMLLoader(Navigator.class.getResource(fxml)).load();
            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(
                    Objects.requireNonNull(Navigator.class.getResource("/css/app.css")).toExternalForm());
            stage.setMinWidth(0);
            stage.setMinHeight(0);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setResizable(resizable);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось открыть экран " + fxml + ": " + e.getMessage(), e);
        }
    }
}
