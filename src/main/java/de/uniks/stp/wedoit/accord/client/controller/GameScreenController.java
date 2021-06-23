package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.json.JsonObject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static de.uniks.stp.wedoit.accord.client.constants.Game.*;

public class GameScreenController implements Controller {

    private final Parent view;
    private final Editor editor;
    private final User opponent;
    private Label lbScore, lblOpponentPlayed, lblYouPlayed, lblYou;
    private ImageView imgYouPlayed, imgOppPlayed;
    private Button btnRock, btnPaper, btnScissors;
    private String gameAction;
    private final PropertyChangeListener opponentGameMove = this::onOpponentGameMove;
    private final Image choosingIMG = new Image(String.valueOf(getClass().getResource(GAME_CHOOSINGIMG)));

    private final IntegerProperty ownScore = new SimpleIntegerProperty(0), oppScore = new SimpleIntegerProperty(0);


    /**
     * Create a new Controller
     *
     * @param view     The view this Controller belongs to
     * @param editor   The editor of the Application
     * @param opponent The Opponent who the localUser is playing against
     */
    public GameScreenController(Parent view, User opponent, Editor editor) {
        this.view = view;
        this.opponent = opponent;
        this.editor = editor;
    }


    /**
     * Called to start this controller
     * Only call after corresponding fxml is loaded
     * <p>
     * Load necessary GUI elements,
     * Add action listeners,
     * setup score listener
     */
    public void init() {
        editor.getStageManager().getGameStage().setOnCloseRequest((e) -> {
            stop();
            JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(opponent.getName(), GAME_CLOSE);
            editor.getWebSocketManager().sendPrivateChatMessage(jsonMsg.toString());
        });

        opponent.setGameMove(null);

        Label lbOpponent = (Label) view.lookup("#lbOpponent");
        this.imgYouPlayed = (ImageView) view.lookup("#imgYouPlayed");
        this.imgOppPlayed = (ImageView) view.lookup("#imgOppPlayed");
        this.btnPaper = (Button) view.lookup("#btnPaper");
        this.btnRock = (Button) view.lookup("#btnRock");
        this.btnScissors = (Button) view.lookup("#btnScissors");
        this.lbScore = (Label) view.lookup("#lbScore");
        this.lblOpponentPlayed = (Label) view.lookup("#lblOpponentPlayed");
        this.lblYouPlayed = (Label) view.lookup("#lblYouPlayed");
        this.lblYou = (Label) view.lookup("#lblYou");

        this.setComponentsText();

        lbOpponent.setText(opponent.getName());

        this.imgYouPlayed.setImage(choosingIMG);
        this.imgOppPlayed.setImage(choosingIMG);

        this.btnScissors.setOnAction(this::gameActionOnClick);
        this.btnRock.setOnAction(this::gameActionOnClick);
        this.btnPaper.setOnAction(this::gameActionOnClick);

        this.opponent.listeners().addPropertyChangeListener(User.PROPERTY_GAME_MOVE, this.opponentGameMove);

        this.lbScore.textProperty().bind(Bindings.createStringBinding(() -> (ownScore.get() + ":" + oppScore.get()), oppScore, ownScore));
    }

    private void setComponentsText() {
        this.lblYou.setText(LanguageResolver.getString("YOU"));
        this.lblYouPlayed.setText(LanguageResolver.getString("YOU_PLAYED"));
        this.lblOpponentPlayed.setText(LanguageResolver.getString("OPPONENT_PLAYED"));
        this.btnRock.setText(LanguageResolver.getString("ROCK"));
        this.btnPaper.setText(LanguageResolver.getString("PAPER"));
        this.btnScissors.setText(LanguageResolver.getString("SCISSORS"));
    }

    /**
     * send message off corresponding action <br>
     * resolves game if opponent already choose a action
     *
     * @param actionEvent occurs when one of the action button is pressed
     */
    private void gameActionOnClick(ActionEvent actionEvent) {
        if ((Button) actionEvent.getSource() == btnRock) {
            gameAction = "Rock";
        } else if ((Button) actionEvent.getSource() == btnPaper) {
            gameAction = "Paper";
        } else if ((Button) actionEvent.getSource() == btnScissors) {
            gameAction = "Scissors";
        }

        JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(opponent.getName(), GAME_PREFIX + gameAction);
        editor.getWebSocketManager().sendPrivateChatMessage(jsonMsg.toString());

        StringBuilder buf = new StringBuilder().append(GAME_IMGURL).append(gameAction).append(".png");
        imgYouPlayed.setImage(new Image(String.valueOf(getClass().getResource(buf.toString()))));

        if (opponent.getGameMove() != null) {
            buf = new StringBuilder().append(GAME_IMGURL).append(opponent.getGameMove()).append(".png");
            imgOppPlayed.setImage(new Image(String.valueOf(getClass().getResource(buf.toString()))));

            resolveGameOutcome();

            opponent.setGameMove(null);
            gameAction = null;
        } else {
            imgOppPlayed.setImage(new Image(String.valueOf(getClass().getResource(GAME_CHOOSINGIMG))));
        }
    }

    /**
     * displays actions if both users choose a action and resolves game
     *
     * @param propertyChangeEvent event occurs when the opponent choose a action
     */
    private void onOpponentGameMove(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != null && gameAction != null) {
            StringBuilder buf = new StringBuilder().append(GAME_IMGURL).append(gameAction).append(".png");
            imgYouPlayed.setImage(new Image(String.valueOf(getClass().getResource(buf.toString()))));
            buf = new StringBuilder().append(GAME_IMGURL).append(opponent.getGameMove()).append(".png");
            imgOppPlayed.setImage(new Image(String.valueOf(getClass().getResource(buf.toString()))));

            resolveGameOutcome();

            opponent.setGameMove(null);
            gameAction = null;
        }
    }

    /**
     * Called to resolve the game moves
     * <p>
     * resolve the game by the action of both players and update score
     */
    private void resolveGameOutcome() {
        Boolean outCome = editor.resultOfGame(gameAction, opponent.getGameMove());
        Platform.runLater(() -> {

            if (outCome != null && outCome) ownScore.set(ownScore.get() + 1);
            else if (outCome != null) oppScore.set(oppScore.get() + 1);
            handleGameDone();
        });
    }

    /**
     * Called when both players have choosen a action
     * <p>
     * checks weather one of the player has won,
     * in that case they get redirected to the result screen
     */
    private void handleGameDone() {
        if (oppScore.get() == 3 || ownScore.get() == 3) {
            this.editor.getStageManager().showGameResultScreen(opponent, ownScore.get() == 3);
            stop();
        }
    }


    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        this.btnScissors.setOnAction(null);
        this.btnRock.setOnAction(null);
        this.btnPaper.setOnAction(null);
        this.opponent.listeners().removePropertyChangeListener(User.PROPERTY_GAME_MOVE, this.opponentGameMove);
        this.lbScore.textProperty().unbind();

    }
}
