package de.uniks.stp.wedoit.accord.client;

import de.uniks.stp.wedoit.accord.client.controller.*;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.util.PreferenceManager;
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

    private final Map<String, Controller> controllerMap = new HashMap<>();
    private SystemTrayController systemTrayController;
    private Editor editor;
    private AccordClient model;
    private PreferenceManager prefManager;
    private Stage stage;
    private Scene scene;
    private Stage popupStage;
    private Scene popupScene;
    private Stage emojiPickerStage;
    private Scene emojiPickerScene;
    private Stage gameStage;
    private Scene gameScene;
    private ResourceManager resourceManager;

    /**
     * load fxml of the LoginScreen and show the LoginScreen on the window
     */
    public void showLoginScreen() {
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
            stage.setMaximized(false);
            stage.setResizable(false);

        } catch (Exception e) {
            System.err.println("Error on showing start screen");
            e.printStackTrace();
        }
    }

    /**
     * load fxml of the MainScreen and show the MainScreen on the window
     */
    public void showMainScreen() {
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

    public void showCreateServerScreen() {
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

    public void showJoinServerScreen() {
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

    public void showPrivateChatsScreen() {
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

    public void showServerScreen(Server server) {
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

    public void showGameScreen(User opponent) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/GameScreen.fxml")));
            gameScene = new Scene(root);

            updateDarkmode();

            //init controller
            GameScreenController gameScreenController = new GameScreenController(root, model.getLocalUser(), opponent, editor);
            gameScreenController.init();
            controllerMap.put("gameScreenController", gameScreenController);

            // display
            gameStage.setTitle("Rock - Paper - Scissors");
            if (gameStage.getStyle() != StageStyle.DECORATED) gameStage.initStyle(StageStyle.DECORATED);
            gameStage.setScene(gameScene);
            gameStage.centerOnScreen();
            gameStage.setResizable(true);
            gameStage.show();
        } catch (Exception e) {
            System.err.println("Error on showing GameScreen");
            e.printStackTrace();
        }
    }

    public void showGameResultScreen(User opponent, Boolean isWinner) {
        if (gameStage.isShowing()) {
            gameStage.close();
            try {
                Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/GameResultScreen.fxml")));
                gameScene = new Scene(root);

                updateDarkmode();

                //init controller
                GameResultScreenController gameResultScreenController = new GameResultScreenController(root, model.getLocalUser(), opponent, isWinner, editor);
                gameResultScreenController.init();
                controllerMap.put("GameResultScreenController", gameResultScreenController);

                gameStage.setTitle("Result");
                if (gameStage.getStyle() != StageStyle.DECORATED) gameStage.initStyle(StageStyle.DECORATED);
                gameStage.setScene(gameScene);
                gameStage.setMinHeight(0);
                gameStage.setMinWidth(0);
                gameStage.centerOnScreen();
                gameStage.setResizable(false);
                gameStage.show();

            } catch (Exception e) {
                System.err.println("Error on loading GameResultScreen");
                e.printStackTrace();
            }
        }

    }

    public void showOptionsScreen() {
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


    public void showCreateCategoryScreen() {
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

    public void showEditCategoryScreen(Category category) {
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

    public void showEmojiScreen(TextField tfForEmoji, Bounds pos) {
        try {
            //load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/EmojiScreen.fxml")));

            emojiPickerScene = new Scene(root);
            updateDarkmode();

            EmojiScreenController emojiScreenController = new EmojiScreenController(root, tfForEmoji);
            emojiScreenController.init();
            controllerMap.put("emojiScreenController", emojiScreenController);
            //display
            emojiPickerStage.setTitle("Emoji Picker");
            emojiPickerStage.setScene(emojiPickerScene);
            emojiPickerStage.setResizable(false);
            emojiPickerStage.show();
            emojiPickerStage.setX(pos.getMinX() - emojiPickerStage.getWidth());
            emojiPickerStage.setY(pos.getMinY() - emojiPickerStage.getHeight());

        } catch (Exception e) {
            System.err.println("Error on showing Emoji Picker");
            e.printStackTrace();
        }
    }


    public void showCreateChannelScreen(Category category) {
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
            popupStage.setResizable(true);
            popupStage.show();
        } catch (Exception e) {
            System.err.println("Error on showing CreateChannelScreen");
            e.printStackTrace();
        }
    }

    public void showEditChannelScreen(Channel channel) {
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
            popupStage.setResizable(true);
            popupStage.show();
        } catch (Exception e) {
            System.err.println("Error on showing EditChannelScreen");
            e.printStackTrace();
        }
    }

    public void showEditServerScreen(Server server) {
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

    public void showAttentionScreen(Object objectToDelete) {
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

    public void showAttentionLeaveServerScreen(Server server) {
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

    private void cleanup() {
        stopController();

        if (popupStage != null) {
            popupStage.hide();
        }
        if (emojiPickerStage != null) {
            emojiPickerStage.hide();
        }
        if (gameStage != null) {
            gameStage.hide();
        }
    }

    private void stopController() {
        Iterator<Map.Entry<String, Controller>> iterator = controllerMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Controller> entry = iterator.next();
            iterator.remove();
            entry.getValue().stop();
        }
    }

    public void changeDarkmode(boolean darkmode) {
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
            if (gameScene != null) {
                gameScene.getStylesheets().remove(Objects.requireNonNull(StageManager.class.getResource(
                        "light-theme.css")).toExternalForm());
                gameScene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource(
                        "dark-theme.css")).toExternalForm());
            }
            if (emojiPickerScene != null) {
                emojiPickerScene.getStylesheets().remove(Objects.requireNonNull(StageManager.class.getResource(
                        "light-theme.css")).toExternalForm());
                emojiPickerScene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource(
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
            if (gameScene != null) {
                gameScene.getStylesheets().remove(Objects.requireNonNull(StageManager.class.getResource(
                        "dark-theme.css")).toExternalForm());
                gameScene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource(
                        "light-theme.css")).toExternalForm());
            }
            if (emojiPickerScene != null) {
                emojiPickerScene.getStylesheets().remove(Objects.requireNonNull(StageManager.class.getResource(
                        "dark-theme.css")).toExternalForm());
                emojiPickerScene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource(
                        "light-theme.css")).toExternalForm());
            }
        }
    }

    public void updateDarkmode() {
        changeDarkmode(model.getOptions().isDarkmode());
    }

    public Map<String, Controller> getControllerMap() {
        return controllerMap;
    }

    public SystemTrayController getSystemTrayController() {
        return systemTrayController;
    }

    public Editor getEditor() {
        return editor;
    }

    public Scene getScene() {
        return scene;
    }

    public Stage getStage() {
        return stage;
    }

    public Scene getPopupScene() {
        return popupScene;
    }

    public Stage getPopupStage() {
        return popupStage;
    }

    public Stage getEmojiPickerStage() {
        return emojiPickerStage;
    }

    public Stage getGameStage() {
        return gameStage;
    }


    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("view/images/LogoAccord.png"))));

        popupStage = new Stage();
        popupStage.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("view/images/LogoAccord.png"))));
        popupStage.initOwner(stage);

        gameStage = new Stage();
        gameStage.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("view/images/LogoAccord.png"))));
        gameStage.initOwner(stage);

        emojiPickerStage = new Stage();
//        emojiPickerStage.setX(770.6000000238419);
//        emojiPickerStage.setY(296.60000002384186);
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
        editor.setStageManager(this);
        prefManager = new PreferenceManager();
        prefManager.setStageManager(this);
        resourceManager = new ResourceManager();
        resourceManager.setPreferenceManager(prefManager);
        model = editor.haveAccordClient();
        editor.haveLocalUser();
        model.setOptions(resourceManager.loadOptions());
        if (!SystemTray.isSupported()) System.out.println("SystemTray not supported on the platform.");
        else {
            systemTrayController = new SystemTrayController(editor);
            systemTrayController.init();
        }

        stage.setMinHeight(400);
        stage.setMinWidth(600);
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
        this.resourceManager = null;
        this.prefManager = null;
    }
}

