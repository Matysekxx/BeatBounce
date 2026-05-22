package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.gui.components.CreditsPanel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;

public class CreditsPanelTest {

    @Test
    void testCreditEntryColorDecoding() {
        final CreditsPanel.CreditEntry entry = new CreditsPanel.CreditEntry();

        entry.color = "#FF0000";
        assertEquals(Color.RED, entry.getAwtColor());

        entry.color = "invalid";
        assertEquals(Color.WHITE, entry.getAwtColor());
        
        entry.color = null;
        assertEquals(Color.WHITE, entry.getAwtColor());
    }
}
