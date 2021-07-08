package de.uniks.stp.wedoit.accord.client.view;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class EmojiButton extends Button {

    public EmojiButton(ImageView text) {
        init(text);
    }

    private void init(ImageView text) {
//        stage.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("view/images/LogoAccord.png"))));
        this.setStyle("-fx-background-radius: 50;-fx-text-fill: black; -fx-font-size: 18px;-fx-background-color: transparent;");
        this.setGraphic(text);
//        this.setText(text);

    }

}
