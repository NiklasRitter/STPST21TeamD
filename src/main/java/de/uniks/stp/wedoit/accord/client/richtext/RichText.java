/*
 * Created 2014 by Tomas Mikula.
 *
 * The author dedicates this file to the public domain.
 */

package de.uniks.stp.wedoit.accord.client.richtext;

import de.uniks.stp.wedoit.accord.client.Editor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.*;
import org.reactfx.util.Either;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.BiConsumer;

public class RichText {

    HashMap<Integer, String> emojiPositions = new HashMap<>();
    Editor editor = new Editor();

    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    private final TextOps<String, TextStyle> styledTextOps = SegmentOps.styledTextOps();
    private final LinkedImageOps<TextStyle> linkedImageOps = new LinkedImageOps<>();

    private final GenericStyledArea<ParStyle, Either<String, LinkedImage>, TextStyle> area =
            new GenericStyledArea<>(
                    ParStyle.EMPTY,                                                 // default paragraph style
                    (paragraph, style) -> paragraph.setStyle(style.toCss()),        // paragraph style setter

                    TextStyle.EMPTY.updateFontSize(12).updateFontFamily("Serif").updateTextColor(Color.BLACK),  // default segment style
                    styledTextOps._or(linkedImageOps, (s1, s2) -> Optional.empty()),                            // segment operations
                    seg -> createNode(seg, (text, style) -> text.setStyle(style.toCss())));                     // Node creator and segment style setter
    {
        area.setWrapText(true);
        area.setStyleCodecs(
                ParStyle.CODEC,
                Codec.styledSegmentCodec(Codec.eitherCodec(Codec.STRING_CODEC, LinkedImage.codec()), TextStyle.CODEC));

        area.setOnKeyPressed(this::keyPressed);

    }

    private void keyPressed(KeyEvent keyEvent) {
        if (area.getText().contains(":emoji")){
            int i = area.getText().indexOf(":emoji");
            System.out.println(i);
            area.getText().replaceAll(":emoji", "bla");
        }
    }

    public GenericStyledArea<ParStyle, Either<String, LinkedImage>, TextStyle> getArea() {
        return  area;
    }

    public HashMap<Integer, String> getEmojiPositions() {
        return emojiPositions;
    }

    private Stage mainStage;

    private Node createNode(StyledSegment<Either<String, LinkedImage>, TextStyle> seg,
                            BiConsumer<? super TextExt, TextStyle> applyStyle) {
        return seg.getSegment().unify(
                text -> StyledTextArea.createStyledTextNode(text, seg.getStyle(), applyStyle),
                LinkedImage::createNode
        );
    }

    public Button createButton(String styleClass, String toolTip) {
        Runnable action = this::insertImage;
        Button button = new Button();
        button.getStyleClass().add(styleClass);
        button.setOnAction(evt -> {
            action.run();
            area.requestFocus();
        });
        button.setPrefWidth(25);
        button.setPrefHeight(25);
        if (toolTip != null) {
            button.setTooltip(new Tooltip(toolTip));
        }
        return button;
    }


    /**
     * Action listener which inserts a new image at the current caret position.
     */
    public void insertImage() {
        String initialDir = System.getProperty("user.dir");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Insert image");
        fileChooser.setInitialDirectory(new File(initialDir));
        File selectedFile = fileChooser.showOpenDialog(mainStage);
        if (selectedFile != null) {
            String imagePath = selectedFile.getAbsolutePath();
            imagePath = imagePath.replace('\\',  '/');
            System.out.println(imagePath);
            insertEmoji(imagePath);

        }
    }



    public void insertEmoji(String imagePath) {
        ReadOnlyStyledDocument<ParStyle, Either<String, LinkedImage>, TextStyle> ros =
                ReadOnlyStyledDocument.fromSegment(Either.right(new RealLinkedImage(imagePath)),
                        ParStyle.EMPTY, TextStyle.EMPTY, area.getSegOps());
        area.insert(0, ros);
        System.out.println(area.getSegOps());
        System.out.println(area.getContent().getText());
        //emojiPositions.put(area.getCaretPosition(), imagePath.substring(imagePath.length()-5));
    }


    public void insertEmoji(GenericStyledArea otherArea, String imagePath) {
        ReadOnlyStyledDocument<ParStyle, Either<String, LinkedImage>, TextStyle> ros =
                ReadOnlyStyledDocument.fromSegment(Either.right(new RealLinkedImage(imagePath)),
                        ParStyle.EMPTY, TextStyle.EMPTY, otherArea.getSegOps());
        otherArea.replaceSelection(ros);
        System.out.println(otherArea.getSegOps());
        System.out.println(otherArea.getContent().getText());
        emojiPositions.put(otherArea.getCaretPosition(), imagePath.substring(imagePath.length()-5));
    }

    public String getCleanMessage() {
        StringBuilder text = new StringBuilder();
        System.out.println("getAreaText" + area.getText());
        byte[] bytes = area.getText().getBytes(StandardCharsets.UTF_8);
        System.out.println(bytes.length);


        for (int i = 0; i < bytes.length ; i++) {
            System.out.println(((char) bytes[i]));
            if (((char) bytes[i]) == '?' && emojiPositions.containsKey(i)) {
                text.append(emojiPositions.get(i));
            } else

                text.append((char) bytes[i]);
        }
        System.out.println(text.toString());
        return text.toString();
    }


}
