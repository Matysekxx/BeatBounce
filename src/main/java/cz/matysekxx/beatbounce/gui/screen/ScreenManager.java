package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.util.Lazy;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ScreenManager {

    private final Map<Class<? extends Screen>, Lazy<Screen>> windows = new HashMap<>();
    private Screen activeWindow;

    public ScreenManager() {
        registerScreen(IntroScreen.class, () -> new IntroScreen(this));
        registerScreen(MainMenuScreen.class, () -> new MainMenuScreen(this));
        registerScreen(GameScreen.class, GameScreen::new);
    }

    public <T extends Screen> void registerScreen(Class<T> screenClass, Supplier<T> constructor) {
        if (windows.containsKey(screenClass)) {
            return;
        }
        @SuppressWarnings("unchecked") final Lazy<Screen> lazyScreen = (Lazy<Screen>) Lazy.of(constructor);
        windows.put(screenClass, lazyScreen);
    }

    public <T extends Screen> void initScreen(Class<T> screenClass) {
        final Lazy<Screen> lazyScreen = windows.get(screenClass);
        if (!lazyScreen.wasInitialized()) {
            lazyScreen.initialize();
        }
    }

    public <T extends Screen> void showScreen(Class<T> screenClass) {
        final Screen nextScreen = windows.get(screenClass).get();
        if (nextScreen != null) {
            nextScreen.setExtendedState(JFrame.MAXIMIZED_BOTH);
            nextScreen.setVisible(true);
            nextScreen.toFront();
            nextScreen.start();

            if (activeWindow != null && activeWindow != nextScreen) {
                activeWindow.stop();
                activeWindow.setVisible(false);
            }
            activeWindow = nextScreen;
        }
    }
}
