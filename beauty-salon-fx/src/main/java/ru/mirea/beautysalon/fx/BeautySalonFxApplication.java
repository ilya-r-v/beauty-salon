package ru.mirea.beautysalon.fx;

import javafx.application.Application;
import javafx.stage.Stage;


// точка входа приложение открывается с экрана входа основное окно показывается только после успешной авторизации
public class BeautySalonFxApplication extends Application {

    @Override
    public void start(Stage stage) {
        Navigator.init(stage);
        Navigator.showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
