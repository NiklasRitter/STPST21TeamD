package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class GameScreenController implements Controller {

    private Parent view;
    private LocalUser localUser;
    private Editor editor;
    private User opponent;
    private Label lbOpponent;
    private ImageView imgYouPlayed,imgOppPlayed;
    private Button btnRock,btnPaper,btnScissors;


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
        localUser.withoutGameInvites(opponent);
        localUser.withoutGameRequests(opponent);

        this.lbOpponent = (Label) view.lookup("#lbOpponent");
        this.imgYouPlayed = (ImageView) view.lookup("#imgYouPlayed");
        this.imgOppPlayed = (ImageView) view.lookup("#imgOppPlayed");
        this.btnPaper = (Button) view.lookup("#btnPaper");
        this.btnRock = (Button) view.lookup("#btnRock");
        this.btnScissors = (Button) view.lookup("#btnScissors");
        this.lbOpponent.setText(opponent.getName());

        this.btnScissors.setOnAction(this::gameActionOnClick);
        this.btnRock.setOnAction(this::gameActionOnClick);
        this.btnPaper.setOnAction(this::gameActionOnClick);

    }

    private void gameActionOnClick(ActionEvent actionEvent) {
        String gameAction = ((Button) actionEvent.getSource()).getText();

        System.out.println(gameAction);

    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {

    }
}
