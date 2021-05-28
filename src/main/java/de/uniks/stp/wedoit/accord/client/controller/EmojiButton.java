package de.uniks.stp.wedoit.accord.client.controller;

import javafx.scene.control.Button;
import javafx.scene.text.Font;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class EmojiButton extends Button {

    public EmojiButton(String text) {
        init(text);
    }

    private void init(String text) {
        this.setFont(Font.font("Segoe UI Symbol"));
        this.setStyle("-fx-background-radius: 50; -fx-background-color: #f3ff00; -fx-font-size: 18px");
        this.setText(text);

    }

}
