package de.uniks.stp.wedoit.accord.client.constants;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.*;


public enum ControllerEnum {
    LOGIN_SCREEN(StageEnum.STAGE, LanguageResolver.getString("LOGIN"), "LoginRegisterScreen", LOGIN_SCREEN_CONTROLLER, true),
    PRIVATE_CHAT_SCREEN(StageEnum.STAGE, LanguageResolver.getString("PRIVATE_CHATS"), "PrivateChatsScreen", PRIVATE_CHATS_SCREEN_CONTROLLER, true),
    OPTION_SCREEN(StageEnum.STAGE, LanguageResolver.getString("OPTIONS"), "OptionScreen", OPTIONS_SCREEN_CONTROLLER, true),
    CONNECT_TO_STEAM_SCREEN(StageEnum.POPUP_STAGE, LanguageResolver.getString("CONNECT_TO_STEAM"), "SteamConnectionScreen", CONNECT_TO_STEAM_SCREEN_CONTROLLER, false),
    GAME_SCREEN_INGAME(StageEnum.GAME_STAGE, LanguageResolver.getString("ROCK_PAPER_SCISSORS"), "GameScreen", GAME_SCREEN_CONTROLLER, true),
    GAME_SCREEN_RESULT(StageEnum.GAME_STAGE, LanguageResolver.getString("RESULT"), "GameResultScreen", GAME_RESULT_SCREEN_CONTROLLER, false),
    SERVER_SCREEN(StageEnum.STAGE, LanguageResolver.getString("SERVER"), "ServerScreen", SERVER_SCREEN_CONTROLLER, true),

    EDIT_SERVER_SCREEN(StageEnum.POPUP_STAGE, LanguageResolver.getString("EDIT_SERVER"), "EditServerScreen", EDIT_SERVER_SCREEN_CONTROLLER, false),
    EDIT_CHANNEL_SCREEN(StageEnum.POPUP_STAGE, LanguageResolver.getString("EDIT_CHANNEL"), "EditChannelScreen", EDIT_CHANNEL_SCREEN_CONTROLLER, true),
    EDIT_CATEGORY_SCREEN(StageEnum.POPUP_STAGE, LanguageResolver.getString("EDIT_CATEGORY"), "EditCategoryScreen", EDIT_CATEGORY_SCREEN_CONTROLLER, false),

    ATTENTION_SCREEN(StageEnum.POPUP_STAGE, LanguageResolver.getString("ATTENTION"), "AttentionScreen", ATTENTION_SCREEN_CONTROLLER, false),
    ATTENTION_LEAVE_SERVER_SCREEN(StageEnum.POPUP_STAGE, LanguageResolver.getString("ATTENTION"), "AttentionLeaveServerScreen", ATTENTION_LEAVE_SERVER_SCREEN_CONTROLLER, false),
    CREATE_SERVER_SCREEN(StageEnum.POPUP_STAGE, LanguageResolver.getString("CREATE_SERVER"), "CreateServerScreen", CREATE_SERVER_SCREEN_CONTROLLER, false),
    JOIN_SERVER_SCREEN(StageEnum.POPUP_STAGE, LanguageResolver.getString("JOIN_SERVER"), "JoinServerScreen", JOIN_SERVER_SCREEN_CONTROLLER, false),
    CREATE_CATEGORY_SCREEN(StageEnum.POPUP_STAGE, LanguageResolver.getString("ADD_CATEGORY"), "CreateCategoryScreen", CREATE_CATEGORY_SCREEN_CONTROLLER, false),
    UPDATE_MESSAGE_SCREEN(StageEnum.POPUP_STAGE, LanguageResolver.getString("UPDATE_MESSAGE"), "UpdateMessageScreen", UPDATE_MESSAGE_SCREEN_CONTROLLER, false),
    LEAVE_SERVER_AS_OWNER_SCREEN(StageEnum.POPUP_STAGE, LanguageResolver.getString("ATTENTION"), "AttentionLeaveServerAsOwnerScreen", ATTENTION_LEAVE_SERVER_AS_OWNER_SCREEN_CONTROLLER, false),
    PRIVATE_MESSAGE_SERVER_SCREEN(StageEnum.POPUP_STAGE, "PRIVATE MESSAGE", "PrivateMessageServerScreen", PRIVATE_MESSAGE_SERVER_SCREEN_CONTROLLER, false),
    CREATE_CHANNEL_SCREEN(StageEnum.POPUP_STAGE, LanguageResolver.getString("CREATE_CHANNEL"), "EditChannelScreen", CREATE_CHANNEL_SCREEN_CONTROLLER, true),


    EMOJI_PICKER_SCREEN(StageEnum.EMOJI_PICKER_STAGE, "Emoji Picker", "EmojiScreen", EMOJI_SCREEN_CONTROLLER, false),

    APPEARANCE_OPTIONS_SCREEN("AppearanceScreen"),
    LANGUAGE_OPTIONS_SCREEN("LanguageScreen"),
    CONNECTIONS_OPTIONS_SCREEN("ConnectionsScreen"),
    VOICE_OPTIONS_SCREEN("VoiceScreen"),
    DESCRIPTION_OPTION_SCREEN("DescriptionScreen");


    private final String fxmlName;
    public StageEnum stage;
    public String controllerName;
    private String title;
    private boolean resizable;


    ControllerEnum(StageEnum stage, String title, String fxmlName, String controllerName, boolean resizable) {
        this.stage = stage;
        this.title = title;
        this.fxmlName = fxmlName;
        this.controllerName = controllerName;
        this.resizable = resizable;
    }

    ControllerEnum(String fxmlName) {
        this.fxmlName = fxmlName;
    }

    public Parent loadScreen() {
        try {
            return FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/" + this.fxmlName + ".fxml")), LanguageResolver.getLanguage());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Parent loadSubOptionScreen() {
        try {
            return FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/subview/options/" + this.fxmlName + ".fxml")), LanguageResolver.getLanguage());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setUpStage(Stage currentStage) {
        currentStage.setTitle(this.title);
        currentStage.setResizable(this.resizable);
        stage.setUpStage(currentStage);
    }


}
