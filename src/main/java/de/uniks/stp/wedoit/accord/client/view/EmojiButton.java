package de.uniks.stp.wedoit.accord.client.view;

import javafx.scene.control.Button;

public class EmojiButton extends Button {

    public EmojiButton(String text) {
        init(text);
    }

    private void init(String text) {
        this.setStyle("-fx-background-radius: 50;-fx-text-fill: black; -fx-font-size: 18px;-fx-background-color: transparent;");
        this.setText(text);
    }

}
