package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.EMOJI_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.EMOJIPICKERSTAGE;

public class TestScreenController {


    private final Parent view;
    private final Editor editor;

    private TextArea textArea;
    private TextField textField;
    private Button btnEmoji;

    public TestScreenController(Parent view, Editor editor) {
        this.view = view;
        this.editor = editor;
    }

    public void init() {
        textArea = (TextArea) this.view.lookup("#textArea");
        textField = (TextField) this.view.lookup("#textField");
        btnEmoji = (Button) this.view.lookup("#btnEmoji");

        btnEmoji.setOnAction(this::btnEmojiOnClick);
    }

    private void btnEmojiOnClick(ActionEvent actionEvent) {
        //get the position of Emoji Button and pass it to showEmojiScreen
            Bounds pos = btnEmoji.localToScreen(btnEmoji.getBoundsInLocal());
            this.editor.getStageManager().initView(EMOJIPICKERSTAGE, LanguageResolver.getString("EMOJI_PICKER"), "EmojiScreen", EMOJI_SCREEN_CONTROLLER, false, textField, pos);
    }



}
