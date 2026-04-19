package cz.matysekxx.beatbounce.gui.screen;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class ScreenManager {

    private final Map<Class<? extends Screen>, Screen> windows = new HashMap<>();
    private Screen activeWindow;

    public ScreenManager() {
        registerScreen(new IntroScreen(this));
        registerScreen(new MainMenuScreen());
        registerScreen(new GameScreen());
    }

    public void registerScreen(Screen screen) {
        windows.put(screen.getClass(), screen);
        screen.setVisible(false);
    }

    public <T extends Screen> void showScreen(Class<T> screenClass) {
        final Screen nextScreen = windows.get(screenClass);
        if (nextScreen != null) {
            nextScreen.setExtendedState(JFrame.MAXIMIZED_BOTH);
            nextScreen.setVisible(true);
            nextScreen.toFront();
            nextScreen.start();

            if (activeWindow != null && activeWindow != nextScreen) {
                activeWindow.setVisible(false);
            }
            activeWindow = nextScreen;
        }
    }
}
