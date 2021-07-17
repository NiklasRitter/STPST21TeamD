/*
 * Created 2014 by Tomas Mikula.
 *
 * The author dedicates this file to the public domain.
 */

package de.uniks.stp.wedoit.accord.client.richtext;

import com.pavlobu.emojitextflow.Emoji;
import com.pavlobu.emojitextflow.EmojiParser;
import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
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
import org.reactfx.collection.LiveList;
import org.reactfx.util.Either;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

public class RichText {

    HashMap<Integer, Emoji> emojiPositions = new HashMap<>();
    HashMap<String, Emoji> typedEmojis = new HashMap<>();
    Editor editor = new Editor();

    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    private final TextOps<String, TextStyle> styledTextOps = SegmentOps.styledTextOps();
    private final LinkedImageOps<TextStyle> linkedImageOps = new LinkedImageOps<>();

    private final GenericStyledArea<ParStyle, Either<String, LinkedImage>, TextStyle> area =
            new GenericStyledArea<>(
                    ParStyle.EMPTY,                                                 // default paragraph style
                    (paragraph, style) -> {
                        //paragraph.getStyleClass().add("");
                    },        // paragraph style setter

                    TextStyle.EMPTY.updateFontSize(12).updateFontFamily("Serif").updateTextColor(Color.BLACK),  // default segment style
                    styledTextOps._or(linkedImageOps, (s1, s2) -> Optional.empty()),                            // segment operations
                    seg -> createNode(seg, (text, style) -> text.getStyleClass()));                     // Node creator and segment style setter
    {
        area.setStyleCodecs(
                ParStyle.CODEC,
                Codec.styledSegmentCodec(Codec.eitherCodec(Codec.STRING_CODEC, LinkedImage.codec()), TextStyle.CODEC));
    }

    public GenericStyledArea<ParStyle, Either<String, LinkedImage>, TextStyle> getArea() {
        return  area;
    }

    public HashMap<Integer, Emoji> getEmojiPositions() {
        return emojiPositions;
    }

    private Node createNode(StyledSegment<Either<String, LinkedImage>, TextStyle> seg,
                            BiConsumer<? super TextExt, TextStyle> applyStyle) {
        return seg.getSegment().unify(
                text -> StyledTextArea.createStyledTextNode(text, seg.getStyle(), applyStyle),
                LinkedImage::createNode
        );
    }

    public void insertEmoji(Emoji emoji) {
        String hex = emoji.getHex();
        String imagePath = Objects.requireNonNull(StageManager.class.getResource("emoji_images/" + hex + ".png")).toString();
        ReadOnlyStyledDocument<ParStyle, Either<String, LinkedImage>, TextStyle> ros =
                ReadOnlyStyledDocument.fromSegment(Either.right(new RealLinkedImage(imagePath)),
                        ParStyle.EMPTY, TextStyle.EMPTY, area.getSegOps());
        area.insert(area.getCaretPosition(), ros);
        System.out.println(area.getContent().getText());
        typedEmojis.put(hex, emoji);
        emojiPositions.put(area.getCaretPosition(), emoji);
    }

    public String getConvertedText() {
        System.out.println(area.getParagraphs());
        StringBuilder buf = new StringBuilder();
        Boolean lastSegmentleft = false;
        LiveList<Paragraph<ParStyle, Either<String, LinkedImage>, TextStyle>> paragraphs = area.getParagraphs();
        for (Paragraph<ParStyle, Either<String, LinkedImage>, TextStyle> paragraph : paragraphs) {
            for (Either<String, LinkedImage> segment : paragraph.getSegments()) {
                if (segment.isLeft() && lastSegmentleft) {
                    buf.append(System.getProperty("line.separator"));
                    buf.append(segment.getLeft());
                } else if (segment.isLeft() && !lastSegmentleft) {
                    buf.append(segment.getLeft());
                    lastSegmentleft = true;
                } else if (segment.isRight()) {
                    String imagePath = segment.getRight().getImagePath();
                    Emoji emoji = getEmojiFromPath(imagePath);
                    buf.append(emoji.getShortname());
                    lastSegmentleft = false;
                }
            }
        }
        return buf.toString();
    }

    private Emoji getEmojiFromPath(String imagePath) {
        String[] split = imagePath.split("/");
        String temp = split[split.length-1];
        String emojiHex = temp.substring(0, temp.length() - 4);
        Emoji emoji = typedEmojis.get(emojiHex);
        return emoji;
    }

}
