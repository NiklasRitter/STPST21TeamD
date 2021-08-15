/**
 * This code is from the following GitHub repository.
 * https://github.com/FXMisc/RichTextFX
 * https://github.com/FXMisc/RichTextFX/tree/master/richtextfx-demos
 * The code is part from the demo for the RichTextFx library.
 * Some source code may have been changed.
 */
package de.uniks.stp.wedoit.accord.client.richtext;

import javafx.scene.paint.Color;
import org.fxmisc.richtext.model.Codec;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds information about the style of a text fragment.
 */
public class TextStyle {

    public static final TextStyle EMPTY = new TextStyle();
    public static final Codec<TextStyle> CODEC = new Codec<TextStyle>() {

        private final Codec<Optional<String>> OPT_STRING_CODEC =
                Codec.optionalCodec(Codec.STRING_CODEC);
        private final Codec<Optional<Color>> OPT_COLOR_CODEC =
                Codec.optionalCodec(Codec.COLOR_CODEC);

        @Override
        public String getName() {
            return "text-style";
        }

        @Override
        public void encode(DataOutputStream os, TextStyle s)
                throws IOException {
            os.writeInt(encodeOptionalUint(s.fontSize));
            OPT_STRING_CODEC.encode(os, s.fontFamily);
            OPT_COLOR_CODEC.encode(os, s.textColor);
        }

        @Override
        public TextStyle decode(DataInputStream is) throws IOException {
            Optional<Integer> fontSize = decodeOptionalUint(is.readInt());
            Optional<String> fontFamily = OPT_STRING_CODEC.decode(is);
            Optional<Color> textColor = OPT_COLOR_CODEC.decode(is);
            return new TextStyle(
                    fontSize, fontFamily, textColor);
        }

        private int encodeOptionalUint(Optional<Integer> oi) {
            return oi.orElse(-1);
        }

        private Optional<Integer> decodeOptionalUint(int i) {
            return (i < 0) ? Optional.empty() : Optional.of(i);
        }
    };
    final Optional<Integer> fontSize;
    final Optional<String> fontFamily;
    final Optional<Color> textColor;

    public TextStyle() {
        this(
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    public TextStyle(
            Optional<Integer> fontSize,
            Optional<String> fontFamily,
            Optional<Color> textColor) {
        this.fontSize = fontSize;
        this.fontFamily = fontFamily;
        this.textColor = textColor;
    }

    public static TextStyle fontSize(int fontSize) {
        return EMPTY.updateFontSize(fontSize);
    }

    public static TextStyle fontFamily(String family) {
        return EMPTY.updateFontFamily(family);
    }

    public static TextStyle textColor(Color color) {
        return EMPTY.updateTextColor(color);
    }

    static String cssColor(Color color) {
        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);
        return "rgb(" + red + ", " + green + ", " + blue + ")";
    }

    public Codec<TextStyle> getCodec() {
        return CODEC;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fontSize, fontFamily, textColor);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TextStyle) {
            TextStyle that = (TextStyle) other;
            return Objects.equals(this.fontSize, that.fontSize) &&
                    Objects.equals(this.fontFamily, that.fontFamily) &&
                    Objects.equals(this.textColor, that.textColor);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        List<String> styles = new ArrayList<>();
        fontSize.ifPresent(s -> styles.add(s.toString()));
        fontFamily.ifPresent(f -> styles.add(f));
        textColor.ifPresent(c -> styles.add(c.toString()));

        return String.join(",", styles);
    }

    public String toCss() {
        StringBuilder sb = new StringBuilder();

        if (fontSize.isPresent()) {
            sb.append("-fx-font-size: " + fontSize.get() + "pt;");
        }

        if (fontFamily.isPresent()) {
            sb.append("-fx-font-family: " + fontFamily.get() + ";");
        }

        if (textColor.isPresent()) {
            Color color = textColor.get();
            sb.append("-fx-fill: " + cssColor(color) + ";");
        }
        return sb.toString();
    }

    public TextStyle updateWith(TextStyle mixin) {
        return new TextStyle(
                mixin.fontSize.isPresent() ? mixin.fontSize : fontSize,
                mixin.fontFamily.isPresent() ? mixin.fontFamily : fontFamily,
                mixin.textColor.isPresent() ? mixin.textColor : textColor);
    }

    public TextStyle updateFontSize(int fontSize) {
        return new TextStyle(Optional.of(fontSize), fontFamily, textColor);
    }

    public TextStyle updateFontFamily(String fontFamily) {
        return new TextStyle(fontSize, Optional.of(fontFamily), textColor);
    }

    public TextStyle updateTextColor(Color textColor) {
        return new TextStyle(fontSize, fontFamily, Optional.of(textColor));
    }

}