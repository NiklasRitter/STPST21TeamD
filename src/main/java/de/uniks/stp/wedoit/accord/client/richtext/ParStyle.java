/**
 * This code is from the following GitHub repository.
 * https://github.com/FXMisc/RichTextFX
 * https://github.com/FXMisc/RichTextFX/tree/master/richtextfx-demos
 * The code is part from the demo for the RichTextFx library.
 * Some source code may have been changed.
 */
package de.uniks.stp.wedoit.accord.client.richtext;

import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.fxmisc.richtext.model.Codec;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds information about the style of a paragraph.
 */
public class ParStyle {

    public static final ParStyle EMPTY = new ParStyle();

    public static final Codec<ParStyle> CODEC = new Codec<ParStyle>() {

        private final Codec<Optional<TextAlignment>> OPT_ALIGNMENT_CODEC =
                Codec.optionalCodec(Codec.enumCodec(TextAlignment.class));
        private final Codec<Optional<Color>> OPT_COLOR_CODEC =
                Codec.optionalCodec(Codec.COLOR_CODEC);

        @Override
        public String getName() {
            return "par-style";
        }

        @Override
        public void encode(DataOutputStream os, ParStyle t) throws IOException {
        }

        @Override
        public ParStyle decode(DataInputStream is) throws IOException {
            return new ParStyle();
        }

    };


    public ParStyle() {
    }


    @Override
    public int hashCode() {
        return Objects.hash();
    }

    @Override
    public boolean equals(Object other) {
        return false;
    }

    @Override
    public String toString() {
        return toCss();
    }

    public String toCss() {
        return "";
    }


}
