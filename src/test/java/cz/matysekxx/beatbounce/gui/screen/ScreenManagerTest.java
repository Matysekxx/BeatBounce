package cz.matysekxx.beatbounce.gui.screen;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ScreenManagerTest {

    @Test
    void testScreenRegistration() {
        ScreenManager manager = new ScreenManager();

        assertNotNull(manager.getScreen(IntroScreen.class));
        assertNotNull(manager.getScreen(MainMenuScreen.class));
    }
}
