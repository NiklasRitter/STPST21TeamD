package de.uniks.stp.wedoit.accord.client.controller;

import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.Icons;
import de.uniks.stp.wedoit.accord.client.view.EmojiButton;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class EmojiScreenController implements Controller {

    private final Parent view;
    private GridPane pane;
    private final TextField tfForEmoji;
    private final Bounds pos;

    private EmojiButton emoji;
    private final HashMap<EmojiButton, String> hashMapForEmojiButtons = new HashMap<>();
    private final List<Icons> iconsUnicodeList = Arrays.asList(Icons.values());

    public EmojiScreenController(Parent view, TextField tfForEmoji, Bounds pos) {
        this.view = view;
        this.tfForEmoji = tfForEmoji;
        this.pos = pos;
    }

    /**
     * Initializes GridPane, the use it as emoji picker
     * call the emoji picker creator
     */
    public void init() {

        this.pane = (GridPane) this.view.lookup("#panelForEmojis");

        this.pane.setAlignment(Pos.CENTER);
        this.pane.setHgap(5);
        this.pane.setVgap(5);

        createEmojiPicker();
        Stage stage = (Stage) view.getScene().getWindow();
        stage.show();
        stage.setX(pos.getMinX() - stage.getWidth());
        stage.setY(pos.getMinY() - stage.getHeight());
    }

    /**
     * create dynamic buttons to show emojis
     * add emoji buttons to a hashMap
     * add created emoji buttons to the GridPane
     * call the action listener of each created emoji button
     */
    private void createEmojiPicker() {
        //how many buttons are to fit in one row
        int gridWidth = 7;

        for (int i = 0; i < iconsUnicodeList.size(); i++) {
            String hexCode = iconsUnicodeList.get(i).toString();
            ImageView icon = new ImageView();
            icon.setFitWidth(40);
            icon.setFitHeight(40);
            System.out.println(StageManager.class.getResource("emoji_images/" + hexCode + ".png"));
            String url = StageManager.class.getResource("emoji_images/" + hexCode + ".png").toString();
            icon.setImage(new Image(url));
            emoji = new EmojiButton("");
            emoji.setGraphic(icon);
            hashMapForEmojiButtons.put(emoji, hexCode);
            emoji.setOnAction(this::btnEmojiOnClick);
            this.pane.add(emoji, i % gridWidth, i / gridWidth);
        }
    }

    /**
     * The text of the selected button from the hashMap is retrieved
     * added the emoji button text in the chat text field
     */
    private void btnEmojiOnClick(ActionEvent actionEvent) {
        if (this.tfForEmoji.isEditable()) {
            Platform.runLater(() -> this.tfForEmoji.setText(this.tfForEmoji.getText() + ":" + hashMapForEmojiButtons.get(actionEvent.getSource()) + ":"));
        }
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    @Override
    public void stop() {
        emoji.setOnAction(null);
    }
}
