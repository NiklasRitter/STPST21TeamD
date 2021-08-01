package de.uniks.stp.wedoit.accord.client.richtext;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class RichTextTest {

    @Test
    public void EmptyLinkedImageTest() {
        EmptyLinkedImage emptyLinkedImage = new EmptyLinkedImage();
        Assert.assertFalse(emptyLinkedImage.isReal());
        Assert.assertEquals("", emptyLinkedImage.getImagePath());
    }

    @Test
    public void TextStyleTest() {
        TextStyle textStyle = new TextStyle();
        Assert.assertEquals(textStyle.getCodec().getName(), "text-style");
    }

    @Test
    public void ParStyleTest() {
        ParStyle parStyle = new ParStyle();

        Assert.assertEquals(parStyle.toCss(), "");
        Assert.assertEquals(parStyle.toString(), "");
        Assert.assertEquals(parStyle.equals(ParStyle.EMPTY), false);

    }


}
