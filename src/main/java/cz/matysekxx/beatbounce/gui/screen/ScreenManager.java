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
        if (activeWindow != null) {
            activeWindow.setVisible(false);
        }
        final Screen nextScreen = windows.get(screenClass);
        if (nextScreen != null) {
            activeWindow = nextScreen;
            activeWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
            activeWindow.setVisible(true);
            activeWindow.toFront();
            activeWindow.start();
        }
    }
}
