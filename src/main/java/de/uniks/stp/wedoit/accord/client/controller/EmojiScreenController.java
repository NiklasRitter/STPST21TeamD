package de.uniks.stp.wedoit.accord.client.controller;

import com.pavlobu.emojitextflow.Emoji;
import com.pavlobu.emojitextflow.EmojiParser;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.Icons;
import de.uniks.stp.wedoit.accord.client.richtext.RichText;
import de.uniks.stp.wedoit.accord.client.view.EmojiButton;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.fxmisc.richtext.GenericStyledArea;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class EmojiScreenController implements Controller {

    private final Parent view;
    private final RichText richText;
    private GridPane pane;
    private final GenericStyledArea txtAreaForEmoji;
    private final Bounds pos;

    private EmojiButton emojiButton;
    private final HashMap<EmojiButton, Emoji> hashMapForEmojiButtons = new HashMap<>();
    private final List<Icons> iconsUnicodeList = Arrays.asList(Icons.values());

    public EmojiScreenController(Parent view, RichText richText, Bounds pos) {
        this.view = view;
        this.richText = richText;
        this.txtAreaForEmoji = richText.getArea();
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
            String unicode = iconsUnicodeList.get(i).toString();
            ImageView icon = new ImageView();
            icon.setFitWidth(30);
            icon.setFitHeight(30);
            String shortname = EmojiParser.getInstance().unicodeToShortname(unicode);
            Emoji emoji = EmojiParser.getInstance().getEmoji(shortname);
            String imagePath = StageManager.class.getResource("emoji_images/" + emoji.getHex() + ".png").toString();
            icon.setImage(new Image(imagePath));
            this.emojiButton = new EmojiButton("");
            this.emojiButton.setGraphic(icon);
            hashMapForEmojiButtons.put(this.emojiButton, emoji);
            this.emojiButton.setOnAction(this::btnEmojiOnClick);
            this.pane.add(this.emojiButton, i % gridWidth, i / gridWidth);
        }
    }

    /**
     * The text of the selected button from the hashMap is retrieved
     * added the emoji button text in the chat text field
     */
    private void btnEmojiOnClick(ActionEvent actionEvent) {
        if (this.txtAreaForEmoji.isEditable()) {
            Emoji selectedEmoji = hashMapForEmojiButtons.get(actionEvent.getSource());
            String imagePath = Objects.requireNonNull(StageManager.class.getResource("emoji_images/" + selectedEmoji.getHex() + ".png").toString());
            System.out.println(imagePath);
            //RichText richText = new RichText();
            //Platform.runLater(() -> richText.insertImage());
            Platform.runLater(() -> richText.insertEmoji(selectedEmoji));
            //Platform.runLater(() -> this.txtAreaForEmoji.setText(this.txtAreaForEmoji.getText() + selectedEmoji.getShortname()));
        }
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    @Override
    public void stop() {
        emojiButton.setOnAction(null);
    }
}
