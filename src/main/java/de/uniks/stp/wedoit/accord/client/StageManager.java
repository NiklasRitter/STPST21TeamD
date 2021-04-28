package de.uniks.stp.wedoit.accord.client;

import javafx.application.Application;
import javafx.stage.Stage;

public class StageManager extends Application {

    private static Stage stage;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        showStartScreen();
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        cleanup();
    }

    public static void showStartScreen() {
    }

    public static void showMainScreen() {
    }

    public static void showWelcomeScreen() {
    }

    public static void showServerScreen() {

    }

    private static void cleanup() {


    }

}

