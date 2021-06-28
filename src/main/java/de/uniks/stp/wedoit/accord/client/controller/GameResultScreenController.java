package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javax.json.JsonObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.PRIVATE_CHATS_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.Game.*;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.STAGE;

public class GameResultScreenController implements Controller{

    private Button btnQuit, btnPlayAgain;
    private Label lbOutcome;
    private final Parent view;
    private final LocalUser localUser;
    private final Editor editor;
    private final User opponent;
    private final Boolean isWinner;
    private final PropertyChangeListener chatListener = this::newMessage;

    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param opponent The Opponent who the localUser is playing against
     * @param isWinner indicates if the LocalUser is the winner
     * @param editor The editor of the Application
     */
    public GameResultScreenController(Parent view, LocalUser model, User opponent, Boolean isWinner, Editor editor){
        this.view = view;
        this.localUser = model;
        this.opponent = opponent;
        this.editor = editor;
        this.isWinner = isWinner;
    }

    /**
     * Called to start this controller
     * Only call after corresponding fxml is loaded
     * <p>
     * Load necessary GUI elements,
     * Add action listeners
     */
    public void init() {
        lbOutcome = (Label) view.lookup("#lbOutcome");
        btnPlayAgain = (Button) view.lookup("#btnPlayAgain");
        btnQuit = (Button) view.lookup("#btnQuit");

        this.setComponentsText();

        if(isWinner == null){
            lbOutcome.setText(LanguageResolver.getString("YOU_WON"));
        }else if(!isWinner){
            lbOutcome.setText(LanguageResolver.getString("SECOND_PLACE"));
        }

        btnQuit.setOnAction(this::redirectToPrivateChats);
        btnPlayAgain.setOnAction(this::playAgainOnClick);
    }

    private void setComponentsText() {
        this.btnPlayAgain.setText(LanguageResolver.getString("PLAY_AGAIN"));
        this.btnQuit.setText(LanguageResolver.getString("QUIT"));
    }


    /**
     * sends either a game game request or accepts a request if the opponent already requested a game
     *
     * @param actionEvent occurs when the Play Again button is pressed
     */
    private void playAgainOnClick(ActionEvent actionEvent) {
        if(this.localUser.getGameInvites().contains(opponent)){
            JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(opponent.getName(), GAME_ACCEPTS);
            editor.getWebSocketManager().sendPrivateChatMessage(JsonUtil.stringify(jsonMsg));
            stop();
        }else{
            JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(opponent.getName(), GAME_INVITE);
            editor.getWebSocketManager().sendPrivateChatMessage(JsonUtil.stringify(jsonMsg));
        }
    }

    /**
     * redirects the user to the PrivateChatScreen
     *
     * @param actionEvent occurs when the Quit button ist pressed
     */
    private void redirectToPrivateChats(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(STAGE, LanguageResolver.getString("PRIVATE_CHATS"), "PrivateChatsScreen", PRIVATE_CHATS_SCREEN_CONTROLLER, true, null, null);
    }


    private void newMessage(PropertyChangeEvent event) {
        if (event.getNewValue() != null) {
            JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(opponent.getName(), GAME_CLOSE);
            editor.getWebSocketManager().sendPrivateChatMessage(JsonUtil.stringify(jsonMsg));
        }
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    @Override
    public void stop() {
        btnPlayAgain.setOnAction(null);
        btnQuit.setOnAction(null);
    }

}
