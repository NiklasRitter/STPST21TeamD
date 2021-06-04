package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javax.json.JsonObject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static de.uniks.stp.wedoit.accord.client.constants.Game.*;

public class GameScreenController implements Controller {

    private Parent view;
    private LocalUser localUser;
    private Editor editor;
    private User opponent;
    private Label lbOpponent;
    private ImageView imgYouPlayed, imgOppPlayed;
    private Button btnRock,btnPaper,btnScissors;
    private Text textScore;
    private String gameAction;
    private final PropertyChangeListener opponentGameMove = this::onOpponentGameMove;
    private final Image choosingIMG = new Image(getClass().getResource(CHOOSINGIMG).toString());

    private final SimpleIntegerProperty ownScore = new SimpleIntegerProperty(0), oppScore = new SimpleIntegerProperty(0);

    //private Timeline timeline;


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
     * Load necessary GUI elements,
     * Add action listeners,
     * Init timeline for game flow
     */
    public void init(){

        this.lbOpponent = (Label) view.lookup("#lbOpponent");
        this.imgYouPlayed = (ImageView) view.lookup("#imgYouPlayed");
        this.imgOppPlayed = (ImageView) view.lookup("#imgOppPlayed");
        this.btnPaper = (Button) view.lookup("#btnPaper");
        this.btnRock = (Button) view.lookup("#btnRock");
        this.btnScissors = (Button) view.lookup("#btnScissors");
        this.textScore = (Text) view.lookup("#textScore");

        this.lbOpponent.setText(opponent.getName());



        this.imgYouPlayed.setImage(choosingIMG);
        this.imgOppPlayed.setImage(choosingIMG);

        this.btnScissors.setOnAction(this::gameActionOnClick);
        this.btnRock.setOnAction(this::gameActionOnClick);
        this.btnPaper.setOnAction(this::gameActionOnClick);

        this.opponent.listeners().addPropertyChangeListener(User.PROPERTY_GAME_MOVE, this.opponentGameMove);

        this.textScore.textProperty().bind(Bindings.createStringBinding(()-> (ownScore.get() + ":" + oppScore.get()), oppScore,ownScore));

        //timeline = new Timeline(new KeyFrame(Duration.millis(WAITING_TIME), e -> {
        //            if(gameAction == null) this.imgYouPlayed.setImage(choosingIMG);
        //            this.imgOppPlayed.setImage(choosingIMG);
        //        }));

    }

    private void gameActionOnClick(ActionEvent actionEvent) {
        gameAction = ((Button) actionEvent.getSource()).getText();

        JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(opponent.getName(), PREFIX + gameAction);
        editor.getNetworkController().sendPrivateChatMessage(jsonMsg.toString());

        imgYouPlayed.setImage(new Image(getClass().getResource(IMGURL + gameAction + ".png").toString()));

        if(opponent.getGameMove() != null){
            imgOppPlayed.setImage(new Image(getClass().getResource(IMGURL + opponent.getGameMove() + ".png").toString()));

            resolveGameOutcome();

            opponent.setGameMove(null);
            gameAction = null;
        }else{
            imgOppPlayed.setImage(new Image(getClass().getResource(CHOOSINGIMG).toString()));
        }
    }

    private void onOpponentGameMove(PropertyChangeEvent event) {
        if(event.getNewValue() != null && gameAction != null) {
            imgYouPlayed.setImage(new Image(getClass().getResource(IMGURL + gameAction + ".png").toString()));
            imgOppPlayed.setImage(new Image(getClass().getResource(IMGURL + opponent.getGameMove() + ".png").toString()));

            resolveGameOutcome();

            opponent.setGameMove(null);
            gameAction = null;
        }
    }

    private void resolveGameOutcome(){
        Boolean outCome = editor.resultOfGame(gameAction, opponent.getGameMove());
        if(outCome != null && outCome) ownScore.set(ownScore.get() + 1);
        else if(outCome != null) oppScore.set(oppScore.get() + 1);

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
        this.textScore.textProperty().unbind();

    }
}
