package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.util.Duration;

import javax.json.JsonObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static de.uniks.stp.wedoit.accord.client.constants.Game.*;

public class GameScreenController implements Controller {

    private Parent view;
    private LocalUser localUser;
    private Editor editor;
    private User opponent;
    private Label lbOpponent;
    private ImageView imgYouPlayed, imgOppPlayed;
    private Button btnRock,btnPaper,btnScissors;
    private Text textYouPlayed,textOppPlayed;
    private String gameAction;
    private final PropertyChangeListener opponentGameMove = this::onOpponentGameMove;
    private final Image choosingIMG = new Image(getClass().getResource(CHOOSINGIMG).toString());

    private Timeline timeline;


    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     * @param opponent The Opponent who the localUser is playing against
     */
    public GameScreenController(Parent view, LocalUser model, User opponent, Editor editor){
        this.view = view;
        this.localUser = model;
        this.opponent = opponent;
        this.editor = editor;

    }

    /**
     * Called to start this controller
     * Only call after corresponding fxml is loaded
     * <p>
     * Load necessary GUI elements
     * Add action listeners
     */
    public void init() {

        this.lbOpponent = (Label) view.lookup("#lbOpponent");
        this.imgYouPlayed = (ImageView) view.lookup("#imgYouPlayed");
        this.imgOppPlayed = (ImageView) view.lookup("#imgOppPlayed");
        this.btnPaper = (Button) view.lookup("#btnPaper");
        this.btnRock = (Button) view.lookup("#btnRock");
        this.btnScissors = (Button) view.lookup("#btnScissors");
        this.textOppPlayed = (Text) view.lookup("#textYouPlayed");
        this.textOppPlayed = (Text) view.lookup("#textOppPlayed");

        this.lbOpponent.setText(opponent.getName());



        this.imgYouPlayed.setImage(choosingIMG);
        this.imgOppPlayed.setImage(choosingIMG);

        this.btnScissors.setOnAction(this::gameActionOnClick);
        this.btnRock.setOnAction(this::gameActionOnClick);
        this.btnPaper.setOnAction(this::gameActionOnClick);

        this.opponent.listeners().addPropertyChangeListener(User.PROPERTY_GAME_MOVE, this.opponentGameMove);

        timeline = new Timeline(new KeyFrame(Duration.millis(WAITING_TIME), e -> {
                    this.imgYouPlayed.setImage(choosingIMG);
                    this.imgOppPlayed.setImage(choosingIMG);

                }));


    }

    private void gameActionOnClick(ActionEvent actionEvent) {
        gameAction = ((Button) actionEvent.getSource()).getText();

        JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(opponent.getName(), PREFIX + gameAction);
        editor.getNetworkController().sendPrivateChatMessage(jsonMsg.toString());

        imgYouPlayed.setImage(new Image(getClass().getResource(IMGURL + gameAction + ".png").toString()));

        if(opponent.getGameMove() != null){
            imgOppPlayed.setImage(new Image(getClass().getResource(IMGURL + opponent.getGameMove() + ".png").toString()));

            timeline.play();

            opponent.setGameMove(null);
            gameAction = null;
        }
    }

    private void onOpponentGameMove(PropertyChangeEvent event) {
        if(event.getNewValue() != null && gameAction != null) {
            imgYouPlayed.setImage(new Image(getClass().getResource(IMGURL + gameAction + ".png").toString()));
            imgOppPlayed.setImage(new Image(getClass().getResource(IMGURL + opponent.getGameMove() + ".png").toString()));

            timeline.play();

            opponent.setGameMove(null);
            gameAction = null;
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

    }
}
