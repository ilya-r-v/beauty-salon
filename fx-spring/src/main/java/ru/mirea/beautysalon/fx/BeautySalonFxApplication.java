package ru.mirea.beautysalon.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class BeautySalonFxApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        showLogin(stage);
    }

    public static void showLogin(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(BeautySalonFxApplication.class.getResource("/fxml/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 420, 520);
        scene.getStylesheets().add(
                Objects.requireNonNull(BeautySalonFxApplication.class.getResource("/css/app.css")).toExternalForm());

        stage.setTitle("Салон красоты — Вход");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void showMain(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(BeautySalonFxApplication.class.getResource("/fxml/main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1150, 720);
        scene.getStylesheets().add(
                Objects.requireNonNull(BeautySalonFxApplication.class.getResource("/css/app.css")).toExternalForm());

        stage.setTitle("Салон красоты — " + AppState.current().getProfile().name());
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(950);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
