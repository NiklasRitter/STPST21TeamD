package de.uniks.stp.wedoit.accord.client;

import de.uniks.stp.wedoit.accord.client.controller.*;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
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
import java.util.*;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.*;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.*;

public class StageManager extends Application {

    private final Map<String, Controller> controllerMap = new HashMap<>();
    private ResourceManager resourceManager = new ResourceManager();
    private final Editor editor = new Editor();
    private PreferenceManager prefManager = new PreferenceManager();
    private SystemTrayController systemTrayController;
    private AccordClient model;
    private Stage stage;
    private Scene scene;
    private Stage popupStage;
    private Scene popupScene;
    private Stage emojiPickerStage;
    private Stage gameStage;
    private Scene gameScene;

    {
        resourceManager.setPreferenceManager(prefManager);
    }

    public void initView(String scene, String title, String fxmlName, String controllerName, boolean resizable, Object parameter, Object parameterTwo) {
        try {
            String fxmlSource = "view/" + fxmlName + ".fxml";
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource(fxmlSource)));
            switch (scene) {
                case STAGE:
                    cleanup();
                    initStage(root, title, resizable);
                    break;
                case POPUPSTAGE:
                    initPopupStage(root, title, resizable);
                    break;
                case GAMESTAGE:
                    initGameStage(root, title, resizable, controllerName);
                    break;
                case EMOJIPICKERSTAGE:
                    initEmojiPickerStage(root, title, resizable);
                    break;
            }
            updateLanguage();
            updateDarkmode();
            openController(root, controllerName, parameter, parameterTwo);
        } catch (Exception e) {
            System.err.println("Error on showing " + controllerName);
            e.printStackTrace();
        }
    }

    private void initStage(Parent root, String title, boolean resizable) {
        if (scene != null) {
            scene.setRoot(root);
        } else {
            scene = new Scene(root);
        }
        stage.setTitle(title);
        stage.setScene(scene);
        stage.setResizable(resizable);
        stage.show();
    }

    private void initPopupStage(Parent root, String title, boolean resizable) {
        popupScene = new Scene(root);
        popupStage.setTitle(title);
        popupStage.setScene(popupScene);
        popupStage.centerOnScreen();
        popupStage.setResizable(resizable);
        popupStage.show();
    }

    private void initGameStage(Parent root, String title, boolean resizable, String controllerName) {
        gameScene = new Scene(root);
        gameStage.setTitle(title);
        if (gameStage.getStyle() != StageStyle.DECORATED) gameStage.initStyle(StageStyle.DECORATED);
        gameStage.setScene(gameScene);
        gameStage.centerOnScreen();
        gameStage.setResizable(resizable);
        if (controllerName.equals(GAME_SCREEN_CONTROLLER)) {
            gameStage.setHeight(450);
            gameStage.setWidth(600);
        } else if (controllerName.equals(GAME_RESULT_SCREEN_CONTROLLER)) {
            gameStage.setMinHeight(0);
            gameStage.setMinWidth(0);
            gameStage.setHeight(170);
            gameStage.setWidth(370);
        }
        gameStage.show();
    }

    private void initEmojiPickerStage(Parent root, String title, boolean resizable) {
        Scene emojiPickerScene = new Scene(root);
        emojiPickerStage.setTitle(title);
        emojiPickerStage.setScene(emojiPickerScene);
        emojiPickerStage.setResizable(resizable);
        emojiPickerStage.sizeToScene();
    }

    private void openController(Parent root, String controllerName, Object parameter, Object parameterTwo) {
        Controller controller = null;
        switch (controllerName) {
            case LOGIN_SCREEN_CONTROLLER:
                editor.haveLocalUser();
                controller = new LoginScreenController(root, model, editor);
                break;
            case MAIN_SCREEN_CONTROLLER:
                controller = new MainScreenController(root, model.getLocalUser(), editor);
                break;
            case CREATE_SERVER_SCREEN_CONTROLLER:
                controller = new CreateServerScreenController(root, editor);
                break;
            case JOIN_SERVER_SCREEN_CONTROLLER:
                controller = new JoinServerScreenController(root, model.getLocalUser(), editor);
                break;
            case PRIVATE_CHATS_SCREEN_CONTROLLER:
                controller = new PrivateChatsScreenController(root, model.getLocalUser(), editor);
                break;
            case SERVER_SCREEN_CONTROLLER:
                controller = new ServerScreenController(root, model.getLocalUser(), editor, (Server) parameter);
                break;
            case GAME_SCREEN_CONTROLLER:
                controller = new GameScreenController(root, (User) parameter, editor);
                break;
            case GAME_RESULT_SCREEN_CONTROLLER:
                controller = new GameResultScreenController(root, model.getLocalUser(), (User) parameter, (boolean) parameterTwo, editor);
                break;
            case OPTIONS_SCREEN_CONTROLLER:
                controller = new OptionsScreenController(root, model.getOptions(), editor);
                break;
            case CREATE_CATEGORY_SCREEN_CONTROLLER:
                controller = new CreateCategoryScreenController(root, editor);
                break;
            case EDIT_CATEGORY_SCREEN_CONTROLLER:
                controller = new EditCategoryScreenController(root, editor, (Category) parameter);
                break;
            case CREATE_CHANNEL_SCREEN_CONTROLLER:
                controller = new CreateChannelScreenController(root, model.getLocalUser(), editor, (Category) parameter);
                break;
            case EDIT_CHANNEL_SCREEN_CONTROLLER:
                controller = new EditChannelScreenController(root, model.getLocalUser(), editor, (Channel) parameter);
                break;
            case EMOJI_SCREEN_CONTROLLER:
                controller = new EmojiScreenController(root, (TextField) parameter, (Bounds) parameterTwo);
                break;
            case ATTENTION_SCREEN_CONTROLLER:
                controller = new AttentionScreenController(root, model.getLocalUser(), editor, parameter);
                break;
            case ATTENTION_LEAVE_SERVER_SCREEN_CONTROLLER:
                controller = new AttentionLeaveServerController(root, editor, (Server) parameter);
                break;
            case EDIT_SERVER_SCREEN_CONTROLLER:
                controller = new EditServerScreenController(root, model.getLocalUser(), editor, (Server) parameter, popupStage);
                break;
            case UPDATE_MESSAGE_SCREEN_CONTROLLER:
                controller = new UpdateMessageScreenController(root, editor, (Message) parameter, popupStage);
                break;
            case PRIVATE_MESSAGE_SERVER_SCREEN_CONTROLLER:
                controller = new PrivateMessageServerScreenController(root, editor, (Server) parameter, (User) parameterTwo); break;
        }
        if (controller != null) {
            controller.init();
            controllerMap.put(controllerName, controller);
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

    /**
     * changes the view to light or darkmode
     *
     * @param darkmode boolean weather darkmode is enabled or not
     */
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
        }
    }

    public void updateDarkmode() {
        changeDarkmode(model.getOptions().isDarkmode());
    }

    public void updateLanguage() {
        changeLanguage(model.getOptions().getLanguage());
    }

    public void changeLanguage(String language) {
        if (language != null) {
            Locale.setDefault(new Locale(language));
        } else {
            Locale.setDefault(new Locale("en_GB"));
        }
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

    public AccordClient getModel() {
        return model;
    }

    public Stage getPopupStage() {
        return popupStage;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public Stage getGameStage() {
        return gameStage;
    }

    public Stage getEmojiPickerStage() {
        return emojiPickerStage;
    }

    public Map<String, Controller> getControllerMap() {
        return controllerMap;
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
        emojiPickerStage.initOwner(stage);
        //Removes title bar of emojiPickerStage including maximize, minimize and close icons.
        emojiPickerStage.initStyle(StageStyle.UNDECORATED);
        //Closes Emoji Picker when clicked outside.
        emojiPickerStage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                emojiPickerStage.close();
            }
        });
        editor.setStageManager(this);
        prefManager.setStageManager(this);
        model = editor.haveAccordClient();
        model.setOptions(new Options());
        editor.haveLocalUser();
        resourceManager.start(model);
        updateLanguage();
        if (!SystemTray.isSupported()) System.out.println("SystemTray not supported on the platform.");
        else {
            systemTrayController = new SystemTrayController(editor);
            systemTrayController.init();
        }
        stage.setMinHeight(400);
        stage.setMinWidth(600);
        editor.automaticLogin(model);
    }

    @Override
    public void stop() {
        try {
            super.stop();
            if (systemTrayController != null) systemTrayController.stop();
            editor.getWebSocketManager().stop();
            LocalUser localUser = model.getLocalUser();
            resourceManager.stop(model);
            if (localUser != null) {
                String userKey = localUser.getUserKey();
                if (userKey != null && !userKey.isEmpty()) {
                    editor.getRestManager().getRestClient().logout(userKey, response -> {
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