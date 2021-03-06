/*
 * This code relates to the following GitHub repository.
 * https://github.com/FXMisc/RichTextFX
 * https://github.com/FXMisc/RichTextFX/tree/master/richtextfx-demos
 * The code is part from the demo for the RichTextFx library.
 * Some source code are created 2014 by Tomas Mikula.
 * The author dedicates this file to the public domain.
 */
package de.uniks.stp.wedoit.accord.client.richtext;

import com.pavlobu.emojitextflow.Emoji;
import de.uniks.stp.wedoit.accord.client.StageManager;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.*;
import org.reactfx.collection.LiveList;
import org.reactfx.util.Either;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * RichTextArea is text area in which images and text can be inserted.
 */
public class RichTextArea extends GenericStyledArea<ParStyle, Either<String, LinkedImage>, TextStyle> {

    private final static TextOps<String, TextStyle> styledTextOps = SegmentOps.styledTextOps();
    private final static LinkedImageOps<TextStyle> linkedImageOps = new LinkedImageOps<>();
    HashMap<String, Emoji> typedEmojis = new HashMap<>();
    private boolean isDarkmode = false;

    public RichTextArea() {
        super(
                ParStyle.EMPTY,
                (paragraph, style) -> paragraph.setStyle(style.toCss()),
                TextStyle.EMPTY.updateFontSize(12).updateFontFamily("Serif").updateTextColor(Color.BLACK),
                styledTextOps._or(linkedImageOps, (s1, s2) -> Optional.empty()),
                seg -> createNode(seg, (text, style) -> text.setStyle(style.toCss())));

        this.setStyleCodecs();
        this.setPrefWidth(2000);
        this.setMaxHeight(75);
        this.setWrapText(true);
    }

    private static Node createNode(StyledSegment<Either<String, LinkedImage>, TextStyle> seg,
                                   BiConsumer<? super TextExt, TextStyle> applyStyle) {
        return seg.getSegment().unify(
                text -> StyledTextArea.createStyledTextNode(text, seg.getStyle(), applyStyle),
                LinkedImage::createNode
        );
    }

    public boolean isDarkmode() {
        return isDarkmode;
    }

    /**
     * updates the text color of the text of a area and the text color of a prompt text of this area
     * True -> color white, false -> color black
     *
     * @param isDarkMode boolean which shows whether the dark mode is switched on
     */
    public void updateTextColor(boolean isDarkMode) {
        TextStyle mixin;
        this.isDarkmode = isDarkMode;
        if (isDarkMode) {
            mixin = TextStyle.textColor(Color.WHITE);
        } else {
            mixin = TextStyle.textColor(Color.BLACK);
        }
        boolean init = false;
        if (this.getConvertedText() == null || this.getConvertedText().equals("")) {
            this.replaceText("init");
            init = true;
        }

        this.selectAll();
        IndexRange selection = this.getSelection();
        if (selection.getLength() != 0) {
            StyleSpans<TextStyle> styles = this.getStyleSpans(selection);
            StyleSpans<TextStyle> newStyles = styles.mapStyles(style -> style.updateWith(mixin));
            this.setStyleSpans(selection.getStart(), newStyles);
        }
        if (init) {
            this.replaceText("");
        }
        this.deselect();

        if (this.getPlaceholder() instanceof Label) {
            Label placeholder = (Label) this.getPlaceholder();
            if (placeholder != null) {
                if (isDarkMode) {
                    placeholder.setStyle("-fx-text-fill: WHITE");
                } else {
                    placeholder.setStyle("-fx-text-fill: BLACK");
                }
            }
            this.setPlaceholder(placeholder);
        }

    }

    public void setStyleCodecs() {
        this.setStyleCodecs(
                ParStyle.CODEC,
                Codec.styledSegmentCodec(Codec.eitherCodec(Codec.STRING_CODEC, LinkedImage.codec()), TextStyle.CODEC));
    }

    /**
     * inserts a emoji at the caret position
     *
     * @param emoji
     */
    public void insertEmoji(Emoji emoji) {
        String hex = emoji.getHex();
        String imagePath = Objects.requireNonNull(StageManager.class.getResource("/emoji_images/" + hex + ".png")).toString();
        ReadOnlyStyledDocument<ParStyle, Either<String, LinkedImage>, TextStyle> ros =
                ReadOnlyStyledDocument.fromSegment(Either.right(new RealLinkedImage(imagePath)),
                        ParStyle.EMPTY, TextStyle.EMPTY, this.getSegOps());
        this.insert(this.getCaretPosition(), ros);
        typedEmojis.put(hex, emoji);
        updateTextColor(isDarkmode);
    }

    /**
     * @return the converted text
     */
    @Override
    public String getText() {
        return this.getConvertedText();
    }

    public RichTextArea setText(String newText) {
        Platform.runLater(() -> this.replaceText(newText));
        return this;
    }

    /**
     * converts the text in a text area.
     * included emojis will be transformed to their shortnames
     *
     * @return converted text
     */
    public String getConvertedText() {
        StringBuilder buf = new StringBuilder();
        boolean lastSegmentLeft = false;
        LiveList<Paragraph<ParStyle, Either<String, LinkedImage>, TextStyle>> paragraphs = this.getParagraphs();
        for (Paragraph<ParStyle, Either<String, LinkedImage>, TextStyle> paragraph : paragraphs) {
            for (Either<String, LinkedImage> segment : paragraph.getSegments()) {
                if (segment.isLeft() && lastSegmentLeft) {
                    buf.append(System.getProperty("line.separator"));
                    buf.append(segment.getLeft());
                } else if (segment.isLeft() && !lastSegmentLeft) {
                    buf.append(segment.getLeft());
                    lastSegmentLeft = true;
                } else if (segment.isRight()) {
                    String imagePath = segment.getRight().getImagePath();
                    Emoji emoji = getEmojiFromPath(imagePath);
                    if (emoji.getUnicode() != null) {
                        buf.append(emoji.getUnicode());
                    }
                    lastSegmentLeft = false;
                }
            }
        }
        return buf.toString();
    }

    private Emoji getEmojiFromPath(String imagePath) {
        String[] split = imagePath.split("/");
        String temp = split[split.length - 1];
        String emojiHex = temp.substring(0, temp.length() - 4);
        return typedEmojis.get(emojiHex);
    }

    /**
     * similar to setPlaceholder. The given text is set as javafx Label as placeholder. The text color depends on the isDarkMode parameter.
     * true -> white, false -> black
     *
     * @param promptText text which should set as prompt text
     * @param isDarkMode boolean which shows whether the dark mode is switched on
     */
    public void setPromptText(String promptText, boolean isDarkMode) {
        Label text = new Label(promptText);
        if (isDarkMode) {
            text.setStyle("-fx-text-fill: WHITE");

        } else {
            text.setStyle("-fx-text-fill: BLACK");

        }
        this.setPlaceholder(text);
    }


    /**
     * returns a prompt text, set with setPromptText
     *
     * @return text of the prompt text
     */
    public String getPromptText() {
        Node placeholder = this.getPlaceholder();
        if (placeholder instanceof Label) {
            return ((Label) placeholder).getText();
        }
        return "";
    }

}
