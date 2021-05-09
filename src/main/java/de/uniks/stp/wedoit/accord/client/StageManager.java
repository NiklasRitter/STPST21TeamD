package de.uniks.stp.wedoit.accord.client;

import de.uniks.stp.wedoit.accord.client.controller.*;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.util.ResourceManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Popup;
import javafx.stage.Stage;
import kong.unirest.Unirest;

public class StageManager extends Application {

    private static Editor editor;
    private static LocalUser model;
    private static LoginScreenController loginScreenController;
    private static MainScreenController mainScreenController;
    private static RestClient restClient;
    private static Stage stage;
    private static WelcomeScreenController welcomeScreenController;
    private static OptionsScreenController optionsScreenController;
    private static CreateServerScreenController createServerScreenController;
    private static ServerScreenController serverScreenController;
    private static Scene scene;
    private static Popup popup;

    public static void showLoginScreen(RestClient restClient) {
        cleanup();

        try {
            editor = new Editor();
            model = editor.haveLocalUser();

            //load view
            Parent root = FXMLLoader.load(StageManager.class.getResource("view/LoginScreen.fxml"));
            scene = new Scene(root);

            updateDarkmode();

            loginScreenController = new LoginScreenController(root, model, editor, restClient);
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

    /**
     * load fxml of the MainScreen and show the MainScreen on the window
     */
    public static void showMainScreen(RestClient restClient) {
        cleanup();

        try {
            //load view
            Parent root = FXMLLoader.load(StageManager.class.getResource("view/MainScreen.fxml"));
            scene = new Scene(root);

            updateDarkmode();

            //init controller
            mainScreenController = new MainScreenController(root, model, editor, restClient);
            mainScreenController.init();

            // display
            stage.setTitle("Main");
            stage.setScene(scene);
            stage.centerOnScreen();

            stage.setResizable(true);

        } catch (Exception e) {
            System.err.println("Error on showing MainScreen");
            e.printStackTrace();
        }
    }

    public static void showCreateServerScreen() {
        cleanup();

        try {
            //load view
            Parent root = FXMLLoader.load(StageManager.class.getResource("view/CreateServerScreen.fxml"));
            scene = new Scene(root);

            updateDarkmode();

            //init controller
            createServerScreenController = new CreateServerScreenController(root, model, editor, restClient);
            createServerScreenController.init();

            //display
            stage.setTitle("Create Server");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setResizable(false);

        } catch (Exception e) {
            System.err.println("Error on showing CreateServerScreen");
            e.printStackTrace();
        }
    }

    public static void showWelcomeScreen(RestClient restClient) {
        cleanup();

        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("view/WelcomeScreen.fxml"));
            scene = new Scene(root);

            updateDarkmode();

            welcomeScreenController = new WelcomeScreenController(root, model, editor, restClient);
            welcomeScreenController.init();

            stage.setTitle("Welcome");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("Error on showing WelcomeScreen");
            e.printStackTrace();
        }
    }

    public static void showServerScreen(Server server) {
        cleanup();

        try {
            //load view
            Parent root = FXMLLoader.load(StageManager.class.getResource("view/ServerScreen.fxml"));
            scene = new Scene(root);

            updateDarkmode();

            //init controller
            serverScreenController = new ServerScreenController(root, model, editor, restClient, server);
            serverScreenController.init();

            //display
            stage.setTitle("Server");
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("Error on showing ServerScreenController");
            e.printStackTrace();
        }

    }

    public static void showOptionsScreen() {
        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("view/OptionsScreen.fxml"));
            popup = new Popup();
            popup.getContent().add(root);
            popup.setWidth(200);
            popup.setHeight(100);
            popup.setAutoHide(true);
            popup.show(stage);

            optionsScreenController = new OptionsScreenController(root, model, editor);
            optionsScreenController.init();
        } catch (Exception e) {
            System.err.println("Error on showing OptionsScreen");
            e.printStackTrace();
        }
    }

    private static void cleanup() {
        if (loginScreenController != null) {
            loginScreenController.stop();
            loginScreenController = null;
        }
        if (mainScreenController != null) {
            mainScreenController.stop();
            mainScreenController = null;
        }
        if (welcomeScreenController != null) {
            welcomeScreenController.stop();
            welcomeScreenController = null;
        }
        if (optionsScreenController != null) {
            optionsScreenController.stop();
            optionsScreenController = null;
        }
    }

    public static void changeDarkmode(boolean darkmode) {
        if (scene != null) {
            if (darkmode) {
                scene.getStylesheets().remove(StageManager.class.getResource(
                        "light-theme.css").toExternalForm());
                scene.getStylesheets().add(StageManager.class.getResource(
                        "dark-theme.css").toExternalForm());
            } else {
                scene.getStylesheets().remove(StageManager.class.getResource(
                        "dark-theme.css").toExternalForm());
                scene.getStylesheets().add(StageManager.class.getResource(
                        "light-theme.css").toExternalForm());
            }
        }
        if (popup != null) {
            if (darkmode) {
                popup.getScene().getStylesheets().remove(StageManager.class.getResource(
                        "light-theme.css").toExternalForm());
                popup.getScene().getStylesheets().add(StageManager.class.getResource(
                        "dark-theme.css").toExternalForm());
            } else {
                popup.getScene().getStylesheets().remove(StageManager.class.getResource(
                        "dark-theme.css").toExternalForm());
                popup.getScene().getStylesheets().add(StageManager.class.getResource(
                        "light-theme.css").toExternalForm());
            }
        }
    }

    public static void updateDarkmode() {
        changeDarkmode(model.getOptions().isDarkmode());
    }

    public Editor getEditor() {
        return editor;
    }

    public Scene getScene() {
        return scene;
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        editor = new Editor();
        model = editor.haveLocalUser();
        model.setOptions(ResourceManager.loadOptions());
        restClient = new RestClient();

        showLoginScreen(restClient);
        stage.show();
    }

    @Override
    public void stop() {
        try {
            super.stop();
            String userKey = model.getUserKey();
            if (userKey != null && !userKey.isEmpty()) {
                restClient.logout(userKey, response -> {
                    Unirest.shutDown();
                    cleanup();
                });
            } else {
                Unirest.shutDown();
                cleanup();
            }
        } catch (Exception e) {
            System.err.println("Error while shutdown program");
            e.printStackTrace();
        }
    }
}

