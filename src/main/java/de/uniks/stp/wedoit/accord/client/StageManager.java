package de.uniks.stp.wedoit.accord.client;

import de.uniks.stp.wedoit.accord.client.controller.LoginScreenController;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StageManager extends Application {

    private static Editor editor;
    private static LocalUser model;
    private static LoginScreenController loginScreenController;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        editor = new Editor();
        model = editor.haveLocalUser();
        showLoginScreen();
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        cleanup();
    }

    private static Stage stage;

    public static void showLoginScreen() {
        cleanup();

        try {
            //load view
            Parent root = FXMLLoader.load(StageManager.class.getResource("view/LoginScreen.fxml"));
            Scene scene = new Scene(root);

            loginScreenController = new LoginScreenController(root, model, editor);
            loginScreenController.init();

            //display
            stage.setTitle("Login");
            stage.setScene(scene);
            stage.centerOnScreen();
            
            stage.setResizable(false);

        } catch (Exception e) {
            System.err.println("Error on showing start screen");
            e.printStackTrace();
        }
    }

    public static void showMainScreen() {
    }

    public static void showWelcomeScreen() {
    }

    public static void showServerScreen() {

    }

    private static void cleanup() {
        if (loginScreenController != null) {
            loginScreenController.stop();
            loginScreenController = null;
        }
    }

}

