package de.uniks.stp.wedoit.accord.client.util;

import com.pavlobu.emojitextflow.EmojiTextFlowParameters;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class EmojiTextFlowParameterHelper {

    private final int fontSize;
    private final double scaleFactor;
    private final TextAlignment textAlignment;
    private final String fontFamily;
    private final FontWeight fontWeight;
    private final Color color;

    public EmojiTextFlowParameterHelper(int fontSize) {
        this.fontSize = fontSize;
        this.scaleFactor = 1D;
        this.textAlignment = TextAlignment.LEFT;
        this.fontFamily = "System";
        this.fontWeight = FontWeight.NORMAL;
        this.color = Color.BLACK;
    }

    public EmojiTextFlowParameters createParameters() {
        EmojiTextFlowParameters parameters = new EmojiTextFlowParameters();
        parameters.setEmojiScaleFactor(scaleFactor);
        parameters.setTextAlignment(textAlignment);
        parameters.setFont(Font.font(fontFamily, fontWeight, fontSize));
        parameters.setTextColor(color);

        return parameters;
    }

}
