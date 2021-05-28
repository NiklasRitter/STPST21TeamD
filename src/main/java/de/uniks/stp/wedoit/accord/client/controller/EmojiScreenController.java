package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.Icons;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.Arrays;
import java.util.List;

public class EmojiScreenController implements Controller {

    private final Parent view;
    private final LocalUser localUser;
    private final Editor editor;
    private GridPane pane;
    private TextField tfPrivateChat;
    private EmojiButton emojiButton;

    public EmojiScreenController(Parent view, LocalUser localUser, Editor editor) {
        this.view = view;
        this.localUser = localUser;
        this.editor = editor;
    }

    public void init() {
        tfPrivateChat = (TextField) view.lookup("#tfEnterPrivateChat");

        List<Icons> days = Arrays.asList(Icons.values());

        this.pane = (GridPane) this.view.lookup("#panelForEmojis");
//        pane.setHgap(5);
//        pane.setVgap(5);

        int GRIDWIDTH = 5;//how many buttons are to fit in one row
        for (int i = 0; i < days.size(); i++) {
            emojiButton = new EmojiButton(days.get(i).toString());
            emojiButton.setOnAction(this::btnEmojiOnClick);
            pane.add(emojiButton, i % GRIDWIDTH, i / GRIDWIDTH, 1, 1);
        }

    }

    private void btnEmojiOnClick(ActionEvent actionEvent) {
        System.out.println(emojiButton.getText());
    }

    @Override
    public void stop() {

    }
}
