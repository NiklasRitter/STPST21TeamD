package de.uniks.stp.wedoit.accord.client;

import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.controller.*;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.richtext.RichTextArea;
import de.uniks.stp.wedoit.accord.client.util.PreferenceManager;
import de.uniks.stp.wedoit.accord.client.util.ResourceManager;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import kong.unirest.Unirest;

import java.awt.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.*;
import static de.uniks.stp.wedoit.accord.client.constants.StageEnum.POPUP_STAGE;

public class StageManager extends Application {

    private final Map<String, Controller> controllerMap = new HashMap<>();
    private final Editor editor = new Editor();
    private final Image logoImage = new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("view/images/LogoAccord.png")));
    private final Map<StageEnum, Scene> sceneMap = new HashMap<>();
    private final Map<StageEnum, Stage> stageMap = new HashMap<>();
    private ResourceManager resourceManager = new ResourceManager();
    private PreferenceManager prefManager = new PreferenceManager();
    private SystemTrayController systemTrayController;
    private AccordClient model;
    private ControllerEnum currentController;


    {
        resourceManager.setPreferenceManager(prefManager);
    }

    /**
     * inits the view using the ControllerEnum
     * set up in a strategy design Pattern
     *
     * @param controller   Enum with information which stage should be loaded
     * @param parameter    for controller
     * @param parameterTwo for controller
     */
    public void initView(ControllerEnum controller, Object parameter, Object parameterTwo) {
        try {

            Parent root = controller.loadScreen();
            if(currentController != null) cleanup(controller);
            currentController = controller;

            Scene currentScene = sceneMap.get(controller.stage);

            Stage currentStage = stageMap.get(controller.stage);

            if (currentScene != null) currentScene.setRoot(root);
            else sceneMap.put(controller.stage, new Scene(root));

            if (controller.stage.equals(POPUP_STAGE)) currentStage.sizeToScene();

            controller.setUpStage(currentStage);

            currentStage.setScene(sceneMap.get(controller.stage));
            if (controller.stage != StageEnum.EMOJI_PICKER_STAGE) currentStage.show();

            updateLanguage();
            updateDarkmode();
            openController(root, controller.controllerName, parameter, parameterTwo);

        } catch (Exception e) {
            System.err.println("Error on showing " + controller.controllerName);
            e.printStackTrace();
        }
    }

    /**
     * loads the right controller when changing the scene
     *
     * @param root           fxml root object
     * @param controllerName for switch case to load right controller
     * @param parameter      for controller
     * @param parameterTwo   for controller
     */
    private void openController(Parent root, String controllerName, Object parameter, Object parameterTwo) {
        Controller controller = null;
        switch (controllerName) {
            case LOGIN_SCREEN_CONTROLLER:
                editor.haveLocalUser();
                controller = new LoginScreenController(root, model, editor, (boolean) parameter);
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
                controller = new GameResultScreenController(root, model.getLocalUser(), (User) parameter, (Boolean) parameterTwo, editor);
                break;
            case OPTIONS_SCREEN_CONTROLLER:
                controller = new OptionsScreenController(root, model.getOptions(), editor);
                break;
            case CONNECT_TO_STEAM_SCREEN_CONTROLLER:
                controller = new ConnectToSteamScreenController(root, model.getLocalUser(), editor);
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
                controller = new EmojiScreenController(root, (RichTextArea) parameter, (Bounds) parameterTwo);
                break;
            case ATTENTION_SCREEN_CONTROLLER:
                controller = new AttentionScreenController(root, model.getLocalUser(), editor, parameter);
                break;
            case ATTENTION_LEAVE_SERVER_SCREEN_CONTROLLER:
                controller = new AttentionLeaveServerController(root, editor, (Server) parameter);
                break;
            case ATTENTION_LEAVE_SERVER_AS_OWNER_SCREEN_CONTROLLER:
                controller = new AttentionLeaveServerAsOwnerController(root, editor);
                break;
            case EDIT_SERVER_SCREEN_CONTROLLER:
                controller = new EditServerScreenController(root, model.getLocalUser(), editor, (Server) parameter, stageMap.get(StageEnum.POPUP_STAGE));
                break;
            case UPDATE_MESSAGE_SCREEN_CONTROLLER:
                controller = new UpdateMessageScreenController(root, editor, (Message) parameter, stageMap.get(StageEnum.POPUP_STAGE));
                break;
            case PRIVATE_MESSAGE_SERVER_SCREEN_CONTROLLER:
                controller = new PrivateMessageServerScreenController(root, editor, (Server) parameter, (User) parameterTwo);
                break;
        }
        if (controller != null) {
            controller.init();
            controllerMap.put(controllerName, controller);
        }
    }

    /**
     * clean up the last opened controller
     */
    private void cleanup(ControllerEnum e) {

        if(currentController.stage == e.stage) controllerMap.get(currentController.controllerName).stop();

        if (stageMap.get(StageEnum.EMOJI_PICKER_STAGE) != null) {
            stageMap.get(StageEnum.EMOJI_PICKER_STAGE).hide();
        }
    }

    /**
     * clean up all controller
     */
    private void cleanup() {
        stageMap.forEach((k, v) -> v.hide());
        controllerMap.get(currentController.controllerName).stop();
    }

    /**
     * changes the view to light or darkmode
     *
     * @param darkmode boolean weather darkmode is enabled or not
     */
    public void changeDarkmode(boolean darkmode) {
        Scene scene = sceneMap.get(StageEnum.STAGE), popup = sceneMap.get(StageEnum.POPUP_STAGE), game = sceneMap.get(StageEnum.POPUP_STAGE);
        if (darkmode) {
            if (scene != null) {
                scene.getStylesheets().remove(Objects.requireNonNull(StageManager.class.getResource(
                        "light-theme.css")).toExternalForm());
                scene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource(
                        "dark-theme.css")).toExternalForm());
            }
            if (popup != null) {
                popup.getStylesheets().remove(Objects.requireNonNull(StageManager.class.getResource(
                        "light-theme.css")).toExternalForm());
                popup.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource(
                        "dark-theme.css")).toExternalForm());
            }
            if (game != null) {
                game.getStylesheets().remove(Objects.requireNonNull(StageManager.class.getResource(
                        "light-theme.css")).toExternalForm());
                game.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource(
                        "dark-theme.css")).toExternalForm());
            }
        } else {
            if (scene != null) {
                scene.getStylesheets().remove(Objects.requireNonNull(StageManager.class.getResource(
                        "dark-theme.css")).toExternalForm());
                scene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource(
                        "light-theme.css")).toExternalForm());
            }
            if (popup != null) {
                popup.getStylesheets().remove(Objects.requireNonNull(StageManager.class.getResource(
                        "dark-theme.css")).toExternalForm());
                popup.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource(
                        "light-theme.css")).toExternalForm());
            }
            if (game != null) {
                game.getStylesheets().remove(Objects.requireNonNull(StageManager.class.getResource(
                        "dark-theme.css")).toExternalForm());
                game.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource(
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

    private void updateOutputInputDevices() {
        this.model.getOptions().setOutputDevice(prefManager.loadOutputDevice());
        this.model.getOptions().setInputDevice(prefManager.loadInputDevice());
    }

    public void changeLanguage(String language) {
        Locale.setDefault(new Locale(Objects.requireNonNullElse(language, "en_GB")));
    }

    public SystemTrayController getSystemTrayController() {
        return systemTrayController;
    }

    public Editor getEditor() {
        return editor;
    }

    public Scene getScene(StageEnum type) {
        return sceneMap.get(type);
    }

    public Stage getStage(StageEnum type) {
        return stageMap.get(type);
    }

    public AccordClient getModel() {
        return model;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public Map<String, Controller> getControllerMap() {
        return controllerMap;
    }

    public PreferenceManager getPrefManager() {
        return prefManager;
    }

    /**
     * start message for the initial screen,
     * overrides the start method from javafx.application </p>
     * sets up the stageMap with all needed stages
     *
     * @param primaryStage from javafx
     */
    @Override
    public void start(Stage primaryStage) {
        stageMap.put(StageEnum.STAGE, primaryStage);

        stageMap.get(StageEnum.STAGE).getIcons().add(logoImage);

        stageMap.put(StageEnum.POPUP_STAGE, new Stage());
        stageMap.get(StageEnum.POPUP_STAGE).getIcons().add(logoImage);
        stageMap.get(StageEnum.POPUP_STAGE).initOwner(stageMap.get(StageEnum.STAGE));
        stageMap.get(StageEnum.POPUP_STAGE).initModality(Modality.APPLICATION_MODAL);

        stageMap.put(StageEnum.GAME_STAGE, new Stage());
        stageMap.get(StageEnum.GAME_STAGE).getIcons().add(logoImage);
        stageMap.get(StageEnum.GAME_STAGE).initOwner(stageMap.get(StageEnum.STAGE));

        stageMap.put(StageEnum.EMOJI_PICKER_STAGE, new Stage());
        stageMap.get(StageEnum.EMOJI_PICKER_STAGE).initOwner(stageMap.get(StageEnum.STAGE));
        stageMap.get(StageEnum.EMOJI_PICKER_STAGE).initStyle(StageStyle.UNDECORATED);
        stageMap.get(StageEnum.EMOJI_PICKER_STAGE).focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                stageMap.get(StageEnum.EMOJI_PICKER_STAGE).close();
            }
        });

        editor.setStageManager(this);
        prefManager.setStageManager(this);
        model = editor.haveAccordClient();
        model.setOptions(new Options());
        editor.haveLocalUser();
        resourceManager.start(model);
        updateOutputInputDevices();
        updateLanguage();
        if (!SystemTray.isSupported()) System.err.println("SystemTray not supported on the platform.");
        else {
            systemTrayController = new SystemTrayController(editor);
            systemTrayController.init();
        }
        stageMap.get(StageEnum.STAGE).setMinHeight(499);
        stageMap.get(StageEnum.STAGE).setMinWidth(655);
        editor.automaticLogin(model);
    }

    /**
     * stop all controller, cleanup and stop rest of application
     */
    @Override
    public void stop() {
        try {
            super.stop();
            stageMap.forEach((k, v) -> v.getIcons().remove(logoImage));
            this.editor.getAudioManager().closeAudioConnection();
            this.editor.getSteamManager().terminateSteamTimer();
            if (systemTrayController != null) systemTrayController.stop();
            editor.getWebSocketManager().stop();
            if (this.model != null) {
                LocalUser localUser = model.getLocalUser();
                resourceManager.stop(model);
                if (localUser != null) {
                    String userKey = localUser.getUserKey();
                    if (userKey != null && !userKey.isEmpty()) {
                        editor.getRestManager().getRestClient().logout(userKey, response -> {
                            Unirest.shutDown();
                            cleanup();
                        });
                    }
                }
            }
            Unirest.shutDown();
            cleanup();
        } catch (Exception e) {
            System.err.println("Error while shutdown program");
            e.printStackTrace();
        }
        this.resourceManager = null;
        this.prefManager = null;
        this.model = null;
    }

}