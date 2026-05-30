package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.util.Lazy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Manages the different screens in the application, handling registration, initialization, and navigation between them.
 *
 * @author Matysekxx
 */
public class ScreenManager {

    /**
     * Map of screen classes to their lazy-loaded instances.
     */
    private final Map<Class<? extends Screen>, Lazy<Screen>> windows = new HashMap<>();

    /**
     * The currently displayed screen.
     */
    private Screen activeWindow;

    /**
     * Constructs a new {@code ScreenManager} and registers the initial screens.
     */
    public ScreenManager() {
        registerScreen(IntroScreen.class, () -> new IntroScreen(this));
        registerScreen(MainMenuScreen.class, () -> new MainMenuScreen(this));
        registerScreen(GameScreen.class, () -> new GameScreen(this));
        registerScreen(CreditsScreen.class, () -> new CreditsScreen(this));
    }

    /**
     * Registers a screen class with its constructor supplier.
     *
     * @param screenClass the class of the screen to register
     * @param constructor the supplier that creates a new instance of the screen
     * @param <T>         the type of the screen
     */
    public <T extends Screen> void registerScreen(Class<T> screenClass, Supplier<T> constructor) {
        if (windows.containsKey(screenClass)) {
            return;
        }
        @SuppressWarnings("unchecked") final Lazy<Screen> lazyScreen = (Lazy<Screen>) Lazy.of(constructor);
        windows.put(screenClass, lazyScreen);
    }

    /**
     * Initializes a screen if it hasn't been initialized yet.
     *
     * @param screenClass the class of the screen to initialize
     * @param <T>         the type of the screen
     */
    public <T extends Screen> void initScreen(Class<T> screenClass) {
        final Lazy<Screen> lazyScreen = windows.get(screenClass);
        if (!lazyScreen.wasInitialized()) {
            lazyScreen.initialize();
        }
    }

    /**
     * Retrieves an instance of the specified screen class.
     *
     * @param screenClass the class of the screen to retrieve
     * @param <T>         the type of the screen
     * @return the screen instance
     */
    @SuppressWarnings("unchecked")
    public <T extends Screen> T getScreen(Class<T> screenClass) {
        return (T) windows.get(screenClass).get();
    }

    /**
     * Shows the specified screen and hides the current active screen.
     *
     * @param screenClass the class of the screen to show
     * @param <T>         the type of the screen
     */
    public <T extends Screen> void showScreen(Class<T> screenClass) {
        final Screen nextScreen = windows.get(screenClass).get();
        if (nextScreen != null) {
            nextScreen.revalidate();
            nextScreen.repaint();
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

    /**
     * Applies current {@link Settings} to all initialized screens.
     * This may involve disposing and recreating windows to change decoration or fullscreen state.
     */
    public void applySettings() {
        final Screen currentActive = activeWindow;
        Class<? extends Screen> activeClass = null;

        for (var entry : windows.entrySet()) {
            final Lazy<Screen> lazyScreen = entry.getValue();
            if (lazyScreen.wasInitialized()) {
                final Screen screen = lazyScreen.get();
                if (screen == currentActive) {
                    activeClass = entry.getKey();
                }
                screen.dispose();
                lazyScreen.reset();
            }
        }

        if (activeClass != null) {
            activeWindow = null;
            showScreen(activeClass);
            if (activeClass == MainMenuScreen.class) {
                getScreen(MainMenuScreen.class).openPanel("SETTINGS");
            }
        }
    }

    /**
     * Retrieves the currently active screen.
     *
     * @return the active screen instance
     */
    public Screen getActiveScreen() {
        return activeWindow;
    }
}