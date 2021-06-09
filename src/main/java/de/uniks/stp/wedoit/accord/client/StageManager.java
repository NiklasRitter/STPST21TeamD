package de.uniks.stp.wedoit.accord.client;

import de.uniks.stp.wedoit.accord.client.controller.*;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.util.ResourceManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import kong.unirest.Unirest;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.*;

public class StageManager extends Application {

    private static final Map<String, Controller> controllerMap = new HashMap<>();
    private static SystemTrayController systemTrayController;
    private static Editor editor;
    private static AccordClient model;
    private static Stage stage;
    private static Scene scene;
    private static Stage popupStage;
    private static Scene popupScene;
    private static Stage emojiPickerStage;
    private static Scene emojiPickerScene;

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
            controllerMap.put(LOGIN_SCREEN_CONTROLLER, loginScreenController);

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
            controllerMap.put(MAIN_SCREEN_CONTROLLER, mainScreenController);

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
            controllerMap.put(CREATE_SERVER_SCREEN_CONTROLLER, createServerScreenController);

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

    public static void showJoinServerScreen() {
        try {
            //load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/JoinServerScreen.fxml")));
            popupScene = new Scene(root);

            updateDarkmode();

            //init controller
            JoinServerScreenController joinServerScreenController = new JoinServerScreenController(root, model.getLocalUser(), editor);
            joinServerScreenController.init();
            controllerMap.put("joinServerScreenController", joinServerScreenController);

            //display
            popupStage.setTitle("Join Server");
            popupStage.setScene(popupScene);
            popupStage.centerOnScreen();
            popupStage.setResizable(false);
            popupStage.show();
        } catch (Exception e) {
            System.err.println("Error on showing JoinServerScreen");
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
            controllerMap.put(PRIVATE_CHATS_SCREEN_CONTROLLER, privateChatsScreenController);

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
            controllerMap.put(SERVER_SCREEN_CONTROLLER, serverScreenController);

            //display
            stage.setTitle("Server");
            stage.setScene(scene);
            stage.setResizable(true);

        } catch (Exception e) {
            System.err.println("Error on showing ServerScreenController");
            e.printStackTrace();
        }

    }

    public static void showGameScreen(User opponent) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/GameScreen.fxml")));
            popupScene = new Scene(root);

            //updateDarkmode();

            //init controller
            GameScreenController gameScreenController = new GameScreenController(root, model.getLocalUser(), opponent, editor);
            gameScreenController.init();
            controllerMap.put("gameScreenController", gameScreenController);

            // display
            popupStage.setTitle("Rock - Paper - Scissors");
            if (popupStage.getStyle() != StageStyle.DECORATED) popupStage.initStyle(StageStyle.DECORATED);
            popupStage.setScene(popupScene);
            popupStage.centerOnScreen();
            popupStage.setResizable(true);
            popupStage.show();
        } catch (Exception e) {
            System.err.println("Error on showing GameScreen");
            e.printStackTrace();
        }
    }

    public static void showGameResultScreen(User opponent, Boolean isWinner) {
        if (popupStage.isShowing()) {
            popupStage.close();
            try {
                Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/GameResultScreen.fxml")));
                popupScene = new Scene(root);


                //init controller
                GameResultScreenController gameResultScreenController = new GameResultScreenController(root, model.getLocalUser(), opponent, isWinner, editor);
                gameResultScreenController.init();
                controllerMap.put("GameResultScreenController", gameResultScreenController);

                popupStage.setTitle("Result");
                if (popupStage.getStyle() != StageStyle.DECORATED) popupStage.initStyle(StageStyle.DECORATED);
                popupStage.setScene(popupScene);
                popupStage.setMinHeight(0);
                popupStage.setMinWidth(0);
                popupStage.centerOnScreen();
                popupStage.setResizable(false);
                popupStage.show();

            } catch (Exception e) {
                System.err.println("Error on loading GameResultScreen");
                e.printStackTrace();
            }
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
            controllerMap.put(OPTIONS_SCREEN_CONTROLLER, optionsScreenController);

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


    public static void showCreateCategoryScreen() {
        try {
            //load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/CreateCategoryScreen.fxml")));

            popupScene = new Scene(root);

            updateDarkmode();


            CreateCategoryScreenController createCategoryScreenController = new CreateCategoryScreenController(root, model.getLocalUser(), editor);
            createCategoryScreenController.init();
            controllerMap.put("createCategoryScreenController", createCategoryScreenController);

            //display
            popupStage.setTitle("Create Category");

            popupStage.setScene(popupScene);
            popupStage.centerOnScreen();
            popupStage.setResizable(false);
            popupStage.show();

        } catch (Exception e) {
            System.err.println("Error on showing CreateCategoryScreen");
            e.printStackTrace();
        }
    }

    public static void showEditCategoryScreen(Category category) {
        try {
            //load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/EditCategoryScreen.fxml")));
            popupScene = new Scene(root);

            updateDarkmode();

            //init controller
            EditCategoryScreenController editCategoryScreenController = new EditCategoryScreenController(root, model.getLocalUser(), editor, category);
            editCategoryScreenController.init();
            controllerMap.put("editCategoryScreenController", editCategoryScreenController);

            //display
            popupStage.setTitle("Edit Category");
            popupStage.setScene(popupScene);
            popupStage.centerOnScreen();
            popupStage.setResizable(false);
            popupStage.show();
        } catch (Exception e) {
            System.err.println("Error on showing EditCategoryScreen");
            e.printStackTrace();
        }
    }

    public static void showEmojiScreen(TextField tfForEmoji, Bounds pos) {
        try {
            //load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/EmojiScreen.fxml")));

            emojiPickerScene = new Scene(root);

            updateDarkmode();

            EmojiScreenController emojiScreenController = new EmojiScreenController(root, model.getLocalUser(), editor, tfForEmoji);
            emojiScreenController.init();
            controllerMap.put("emojiScreenController", emojiScreenController);

            //display
            emojiPickerStage.setX(pos.getMinX() - emojiPickerStage.getWidth());
            emojiPickerStage.setY(pos.getMinY() - emojiPickerStage.getHeight());
            emojiPickerStage.setTitle("Emoji Picker");
            emojiPickerStage.setScene(emojiPickerScene);
            emojiPickerStage.setResizable(false);
            emojiPickerStage.show();

        } catch (Exception e) {
            System.err.println("Error on showing Emoji Picker");
            e.printStackTrace();
        }
    }


    public static void showCreateChannelScreen(Category category) {
        try {
            //load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/CreateChannelScreen.fxml")));
            popupScene = new Scene(root);

            updateDarkmode();

            //init controller
            CreateChannelScreenController createChannelScreenController = new CreateChannelScreenController(root, model.getLocalUser(), editor, category);
            createChannelScreenController.init();
            controllerMap.put("createChannelScreenController", createChannelScreenController);

            //display
            popupStage.setTitle("Create Channel");
            popupStage.setScene(popupScene);
            popupStage.centerOnScreen();
            popupStage.setResizable(false);
            popupStage.show();
        } catch (Exception e) {
            System.err.println("Error on showing CreateChannelScreen");
            e.printStackTrace();
        }
    }

    public static void showEditChannelScreen(Channel channel) {
        try {
            //load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/EditChannelScreen.fxml")));
            popupScene = new Scene(root);

            updateDarkmode();

            //init controller
            EditChannelScreenController editChannelScreenController = new EditChannelScreenController(root, model.getLocalUser(), editor, channel);
            editChannelScreenController.init();
            controllerMap.put("editChannelScreenController", editChannelScreenController);

            //display
            popupStage.setTitle("Edit Channel");
            popupStage.setScene(popupScene);
            popupStage.centerOnScreen();
            popupStage.setResizable(false);
            popupStage.show();
        } catch (Exception e) {
            System.err.println("Error on showing EditChannelScreen");
            e.printStackTrace();
        }
    }

    public static void showEditServerScreen(Server server) {
        try {
            //load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/EditServerScreen.fxml")));
            popupScene = new Scene(root);

            updateDarkmode();

            //init controller
            EditServerScreenController editServerScreenController = new EditServerScreenController(root, model.getLocalUser(), editor, server, popupStage);
            editServerScreenController.init();
            controllerMap.put("editServerScreenController", editServerScreenController);

            //display
            popupStage.setTitle("Edit Server");
            popupStage.setScene(popupScene);
            popupStage.centerOnScreen();
            popupStage.setResizable(false);
            popupStage.show();

        } catch (Exception e) {
            System.err.println("Error on showing EditServerScreen");
            e.printStackTrace();
        }
    }

    public static void showAttentionScreen(Object objectToDelete) {
        try {
            //load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/AttentionScreen.fxml")));
            popupScene = new Scene(root);

            updateDarkmode();

            //init controller
            AttentionScreenController attentionScreenController = new AttentionScreenController(root, model.getLocalUser(), editor, objectToDelete);
            attentionScreenController.init();
            controllerMap.put("attentionScreenController", attentionScreenController);

            //display
            popupStage.setTitle("Attention");
            popupStage.setScene(popupScene);
            popupStage.centerOnScreen();
            popupStage.setResizable(false);
            popupStage.show();

        } catch (Exception e) {
            System.err.println("Error on showing EditServerScreen");
            e.printStackTrace();
        }
    }

    public static void showAttentionLeaveServerScreen(Server server) {
        try {
            //load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/AttentionLeaveServerScreen.fxml")));
            popupScene = new Scene(root);

            updateDarkmode();

            //init controller
            AttentionLeaveServerController attentionLeaveServerController = new AttentionLeaveServerController(root, editor, model.getLocalUser(), server);
            attentionLeaveServerController.init();
            controllerMap.put("attentionLeaveServerController", attentionLeaveServerController);

            //display
            popupStage.setTitle("Attention");
            popupStage.setScene(popupScene);
            popupStage.centerOnScreen();
            popupStage.setResizable(false);
            popupStage.show();

        } catch (Exception e) {
            System.err.println("Error on showing Leave Server Attention");
            e.printStackTrace();
        }
    }

    private static void cleanup() {
        stopController();

        if (popupStage != null) {
            popupStage.hide();
        }
        if (emojiPickerStage != null) {
            emojiPickerStage.hide();
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

    public static Map<String, Controller> getControllerMap() {
        return controllerMap;
    }

    public static SystemTrayController getSystemTrayController() {
        return systemTrayController;
    }

    public static Editor getEditor() {
        return editor;
    }

    public static Scene getScene() {
        return scene;
    }

    public static Stage getStage() {
        return stage;
    }

    public static Scene getPopupScene() {
        return popupScene;
    }

    public static Stage getPopupStage() {
        return popupStage;
    }

    public Stage getEmojiPickerStage() {
        return emojiPickerStage;
    }


    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("view/images/Logo.png"))));

        popupStage = new Stage();
        popupStage.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("view/images/Logo.png"))));
        popupStage.initOwner(stage);

        emojiPickerStage = new Stage();
        emojiPickerStage.initOwner(stage);

        //Removes title bar of emojiPickerStage including maximize, minimize and close icons.
        emojiPickerStage.initStyle(StageStyle.UNDECORATED);
        //Closes Emoji Picker when clicked outside.
        emojiPickerStage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                emojiPickerStage.close();
            }
        });

        editor = new Editor();
        model = editor.haveAccordClient();
        editor.haveLocalUser();
        model.setOptions(ResourceManager.loadOptions());
        if (!SystemTray.isSupported()) System.out.println("SystemTray not supported on the platform.");
        else {
            systemTrayController = new SystemTrayController(editor);
            systemTrayController.init();
        }

        stage.setHeight(400);
        stage.setWidth(600);
        showLoginScreen();
        stage.show();
    }

    @Override
    public void stop() {
        try {
            super.stop();
            if (systemTrayController != null) systemTrayController.stop();
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

