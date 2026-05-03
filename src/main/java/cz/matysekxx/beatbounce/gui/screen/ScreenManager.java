package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.util.Lazy;
import cz.matysekxx.beatbounce.configuration.Settings;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ScreenManager {

    private final Map<Class<? extends Screen>, Lazy<Screen>> windows = new HashMap<>();
    private Screen activeWindow;

    public ScreenManager() {
        registerScreen(IntroScreen.class, () -> new IntroScreen(this));
        registerScreen(MainMenuScreen.class, () -> new MainMenuScreen(this));
        registerScreen(GameScreen.class, () -> new GameScreen(this));
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

    @SuppressWarnings("unchecked")
    public <T extends Screen> T getScreen(Class<T> screenClass) {
        return (T) windows.get(screenClass).get();
    }

    public <T extends Screen> void showScreen(Class<T> screenClass) {
        final Screen nextScreen = windows.get(screenClass).get();
        if (nextScreen != null) {
            if (Settings.fullscreen) {
                applyFullscreen(nextScreen);
            }
            nextScreen.setVisible(true);
            nextScreen.toFront();

            if (activeWindow != null && activeWindow != nextScreen) {
                activeWindow.stop();
                activeWindow.setVisible(false);
            }
            activeWindow = nextScreen;
            nextScreen.start();
        }
    }

    public void applySettings() {
        for (Lazy<Screen> lazyScreen : windows.values()) {
            if (lazyScreen.wasInitialized()) {
                final Screen screen = lazyScreen.get();
                final boolean isActive = (screen == activeWindow);
                screen.dispose();
                screen.setUndecorated(Settings.fullscreen);
                if (Settings.fullscreen) {
                    applyFullscreen(screen);
                } else {
                    screen.setExtendedState(JFrame.NORMAL);
                    screen.setSize(1024, 768);
                    final GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
                    final GraphicsDevice device = (Settings.monitorIndex >= 0 && Settings.monitorIndex < devices.length) ? devices[Settings.monitorIndex] : devices[0];
                    final Rectangle bounds = device.getDefaultConfiguration().getBounds();
                    screen.setLocation(bounds.x + (bounds.width - 1024) / 2, bounds.y + (bounds.height - 768) / 2);
                }
                if (isActive) {
                    screen.setVisible(true);
                }
            }
        }
    }

    private void applyFullscreen(Screen screen) {
        final GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        final GraphicsDevice device = (Settings.monitorIndex >= 0 && Settings.monitorIndex < devices.length) ? devices[Settings.monitorIndex] : devices[0];
        final Rectangle bounds = device.getDefaultConfiguration().getBounds();
        bounds.height += 1;
        screen.setBounds(bounds);
    }
}