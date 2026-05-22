package cz.matysekxx.beatbounce.util;

import org.junit.jupiter.api.Test;
import java.awt.*;
import static org.junit.jupiter.api.Assertions.*;

class UIScaleTest {

    @Test
    void testUpdateAndGetScale() {
        UIScale.update(1920, 1080);
        assertEquals(1.0f, UIScale.getScale());

        UIScale.update(1920, 540);
        assertEquals(0.5f, UIScale.getScale());

        UIScale.update(1920, 100);
        assertEquals(0.5f, UIScale.getScale());
    }

    @Test
    void testScaleInt() {
        UIScale.update(1920, 1080);
        assertEquals(100, UIScale.scale(100));

        UIScale.update(1920, 540);
        assertEquals(50, UIScale.scale(100));
    }

    @Test
    void testScaleFloat() {
        UIScale.update(1920, 1080);
        assertEquals(100.0f, UIScale.scale(100.0f));

        UIScale.update(1920, 540);
        assertEquals(50.0f, UIScale.scale(100.0f));
    }

    @Test
    void testScaleFont() {
        UIScale.update(1920, 1080);
        Font font = new Font("Arial", Font.PLAIN, 20);
        Font scaledFont = UIScale.scaleFont(font);
        assertEquals(20.0f, scaledFont.getSize2D());

        UIScale.update(1920, 540);
        scaledFont = UIScale.scaleFont(font);
        assertEquals(10.0f, scaledFont.getSize2D());
    }

    @Test
    void testScaleStroke() {
        UIScale.update(1920, 1080);
        BasicStroke stroke = UIScale.scaleStroke(2.0f);
        assertEquals(2.0f, stroke.getLineWidth());

        UIScale.update(1920, 540);
        stroke = UIScale.scaleStroke(2.0f);
        assertEquals(1.0f, stroke.getLineWidth());
    }
}
