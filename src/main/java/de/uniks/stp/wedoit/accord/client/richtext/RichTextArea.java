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

public class RichTextArea extends GenericStyledArea<ParStyle, Either<String, LinkedImage>, TextStyle> {

    HashMap<String, Emoji> typedEmojis = new HashMap<>();

    private final static TextOps<String, TextStyle> styledTextOps = SegmentOps.styledTextOps();
    private final static LinkedImageOps<TextStyle> linkedImageOps = new LinkedImageOps<>();
    private boolean isDarkmode = false;

    public boolean isDarkmode() {
        return isDarkmode;
    }

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

    private static Node createNode(StyledSegment<Either<String, LinkedImage>, TextStyle> seg,
                                   BiConsumer<? super TextExt, TextStyle> applyStyle) {
        return seg.getSegment().unify(
                text -> StyledTextArea.createStyledTextNode(text, seg.getStyle(), applyStyle),
                LinkedImage::createNode
        );
    }

    public void setStyleCodecs() {
        this.setStyleCodecs(
                ParStyle.CODEC,
                Codec.styledSegmentCodec(Codec.eitherCodec(Codec.STRING_CODEC, LinkedImage.codec()), TextStyle.CODEC));
    }

    public void insertEmoji(Emoji emoji) {
        String hex = emoji.getHex();
        String imagePath = Objects.requireNonNull(StageManager.class.getResource("emoji_images/" + hex + ".png")).toString();
        ReadOnlyStyledDocument<ParStyle, Either<String, LinkedImage>, TextStyle> ros =
                ReadOnlyStyledDocument.fromSegment(Either.right(new RealLinkedImage(imagePath)),
                        ParStyle.EMPTY, TextStyle.EMPTY, this.getSegOps());
        this.insert(this.getCaretPosition(), ros);
        typedEmojis.put(hex, emoji);
        updateTextColor(isDarkmode);
    }

    @Override
    public String getText() {
        return this.getConvertedText();
    }

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
                    if (emoji.getShortname() != null) {
                        buf.append(emoji.getShortname());
                    }
                    lastSegmentLeft = false;
                }
            }
        }
        return buf.toString();
    }

    public void setText(String newText) {
        Platform.runLater(() -> this.replaceText(newText));
    }

    private Emoji getEmojiFromPath(String imagePath) {
        String[] split = imagePath.split("/");
        String temp = split[split.length - 1];
        String emojiHex = temp.substring(0, temp.length() - 4);
        return typedEmojis.get(emojiHex);
    }

    public void setPromptText(String promptText, boolean isDarkMode) {
        Label text = new Label(promptText);
        if (isDarkMode) {
            text.setStyle("-fx-text-fill: WHITE");

        } else {
            text.setStyle("-fx-text-fill: BLACK");

        }
        this.setPlaceholder(text);
    }

    public String getPromptText() {
        Node placeholder = this.getPlaceholder();
        if (placeholder instanceof Label) {
            return ((Label) placeholder).getText();
        }
        return "";
    }

}
