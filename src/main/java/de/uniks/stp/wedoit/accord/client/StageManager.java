package de.uniks.stp.wedoit.accord.client;

import de.uniks.stp.wedoit.accord.client.controller.*;
import de.uniks.stp.wedoit.accord.client.model.AccordClient;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.util.ResourceManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import kong.unirest.Unirest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class StageManager extends Application {

    private static Editor editor;
    private static AccordClient model;
    private static Stage stage;
    private static Stage popupStage;
    private static Scene scene;
    private static Scene popupScene;
    private static final Map<String, Controller> controllerMap = new HashMap<>();

    /**
     * load fxml of the LoginScreen and show the LoginScreen on the window
     */
    public static void showLoginScreen() {
        cleanup();

        try {
            //load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/LoginScreen.fxml")));

            if (scene != null) {
                scene.setRoot(root);
            } else {
                scene = new Scene(root);
            }

            model.setLocalUser(new LocalUser());

            updateDarkmode();

            LoginScreenController loginScreenController = new LoginScreenController(root, model.getLocalUser(), editor);
            loginScreenController.init();
            controllerMap.put("loginScreenController", loginScreenController);

            //display
            stage.setTitle("Login");
            stage.setScene(scene);
            stage.setHeight(450);
            stage.setWidth(600);
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
    public static void showMainScreen() {
        cleanup();

        try {
            //load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/MainScreen.fxml")));
            if (scene != null) {
                scene.setRoot(root);
            } else {
                scene = new Scene(root);
            }

            updateDarkmode();

            //init controller
            MainScreenController mainScreenController = new MainScreenController(root, model.getLocalUser(), editor);
            mainScreenController.init();
            controllerMap.put("mainScreenController", mainScreenController);

            // display
            stage.setTitle("Main");
            stage.setScene(scene);
            stage.setResizable(true);

        } catch (Exception e) {
            System.err.println("Error on showing MainScreen");
            e.printStackTrace();
        }
    }

    public static void showCreateServerScreen() {
        try {
            //load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/CreateServerScreen.fxml")));
            popupScene = new Scene(root);

            updateDarkmode();

            //init controller
            CreateServerScreenController createServerScreenController = new CreateServerScreenController(root, model.getLocalUser(), editor);
            createServerScreenController.init();
            controllerMap.put("createServerScreenController", createServerScreenController);

            //display
            popupStage.setTitle("Create Server");
            popupStage.setScene(popupScene);
            popupStage.centerOnScreen();
            popupStage.setResizable(false);
            popupStage.show();
        } catch (Exception e) {
            System.err.println("Error on showing CreateServerScreen");
            e.printStackTrace();
        }
    }

    public static void showPrivateChatsScreen() {
        cleanup();

        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/PrivateChatsScreen.fxml")));
            if (scene != null) {
                scene.setRoot(root);
            } else {
                scene = new Scene(root);
            }

            updateDarkmode();

            PrivateChatsScreenController privateChatsScreenController = new PrivateChatsScreenController(root, model.getLocalUser(), editor);
            privateChatsScreenController.init();
            controllerMap.put("privateChatsScreenController", privateChatsScreenController);

            //display
            stage.setTitle("Private Chats");
            stage.setScene(scene);
            stage.setResizable(true);

        } catch (Exception e) {
            System.err.println("Error on showing PrivateChatsScreen");
            e.printStackTrace();
        }
    }

    public static void showServerScreen(Server server) {
        cleanup();

        try {
            //load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/ServerScreen.fxml")));
            if (scene != null) {
                scene.setRoot(root);
            } else {
                scene = new Scene(root);
            }

            updateDarkmode();

            //init controller
            ServerScreenController serverScreenController = new ServerScreenController(root, model.getLocalUser(), editor, server);
            serverScreenController.init();
            controllerMap.put("serverScreenController", serverScreenController);

            //display
            stage.setTitle("Server");
            stage.setScene(scene);
            stage.setResizable(true);

        } catch (Exception e) {
            System.err.println("Error on showing ServerScreenController");
            e.printStackTrace();
        }

    }

    public static void showOptionsScreen() {
        try {
            //load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/OptionsScreen.fxml")));
            popupScene = new Scene(root);

            updateDarkmode();

            //init controller
            OptionsScreenController optionsScreenController = new OptionsScreenController(root, model.getOptions(), editor);
            optionsScreenController.init();
            controllerMap.put("optionsScreenController", optionsScreenController);

            //display
            popupStage.setTitle("Options");
            popupStage.setScene(popupScene);
            popupStage.centerOnScreen();
            popupStage.setResizable(false);
            popupStage.show();

        } catch (Exception e) {
            System.err.println("Error on showing OptionsScreen");
            e.printStackTrace();
        }
    }

    private static void cleanup() {
        stopController();

        if (popupStage != null) {
            popupStage.hide();
        }
    }

    private static void stopController() {
        Iterator<Map.Entry<String, Controller>> iterator = controllerMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Controller> entry = iterator.next();
            iterator.remove();
            entry.getValue().stop();
        }
    }

    public static void changeDarkmode(boolean darkmode) {
        if (darkmode) {
            if (scene != null) {
                scene.getStylesheets().remove(Objects.requireNonNull(StageManager.class.getResource(
                        "light-theme.css")).toExternalForm());
                scene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource(
                        "dark-theme.css")).toExternalForm());
            }
            if (popupScene != null) {
                popupScene.getStylesheets().remove(Objects.requireNonNull(StageManager.class.getResource(
                        "light-theme.css")).toExternalForm());
                popupScene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource(
                        "dark-theme.css")).toExternalForm());
            }
        } else {
            if (scene != null) {
                scene.getStylesheets().remove(Objects.requireNonNull(StageManager.class.getResource(
                        "dark-theme.css")).toExternalForm());
                scene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource(
                        "light-theme.css")).toExternalForm());
            }
            if (popupScene != null) {
                popupScene.getStylesheets().remove(Objects.requireNonNull(StageManager.class.getResource(
                        "dark-theme.css")).toExternalForm());
                popupScene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource(
                        "light-theme.css")).toExternalForm());
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

    public Scene getPopupScene() {
        return popupScene;
    }

    public Stage getPopupStage() {
        return popupStage;
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("view/images/Logo.png"))));
        popupStage = new Stage();
        popupStage.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("view/images/Logo.png"))));
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.initOwner(stage);
        editor = new Editor();
        model = editor.haveAccordClient();
        editor.haveLocalUser();
        model.setOptions(ResourceManager.loadOptions());

        stage.setMinHeight(450);
        stage.setMinWidth(600);
        showLoginScreen();
        stage.show();
    }

    @Override
    public void stop() {
        try {
            super.stop();
            editor.getNetworkController().stop();
            LocalUser localUser = model.getLocalUser();
            if (localUser != null) {
                String userKey = localUser.getUserKey();
                if (userKey != null && !userKey.isEmpty()) {
                    editor.getNetworkController().getRestClient().logout(userKey, response -> {
                        Unirest.shutDown();
                        cleanup();
                    });
                } else {
                    Unirest.shutDown();
                    cleanup();
                }
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

