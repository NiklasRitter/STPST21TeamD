package de.uniks.stp.wedoit.accord.client.controller;

import javafx.scene.control.Button;
import javafx.scene.text.Font;

public class EmojiButton extends Button {

    public EmojiButton(String text) {
        init(text);
    }

    private void init(String text) {

//        this.setStyle("-fx-background-radius: 50; -fx-background-color: #ffcc00; -fx-font-size: 18px");
        this.setText(text);

    }

}
