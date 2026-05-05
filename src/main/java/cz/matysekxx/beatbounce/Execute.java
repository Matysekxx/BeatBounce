package cz.matysekxx.beatbounce;

import cz.matysekxx.beatbounce.gui.screen.IntroScreen;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;

/**
 * The main execution orchestrator for the BeatBounce application.
 * <p>
 * This class implements the {@link Runnable} interface and follows the **Singleton**
 * design pattern to ensure a single point of control for application lifecycle
 * events, such as starting the UI and managing screens.
 * <p>
 * ### Example Usage:
 * <pre>{@code
 * // Starting the application
 * Execute app = Execute.getSingleton();
 * app.run();
 * }</pre>
 */
public class Execute implements Runnable {

    /**
     * The single static instance of the Execute class.
     */
    private static final Execute singleton = new Execute();

    /**
     * Manager responsible for handling UI screen transitions.
     */
    private final ScreenManager screenManager;

    /**
     * Private constructor to enforce the Singleton pattern.
     * <p>
     * Initializes the {@link ScreenManager} which will be used throughout
     * the application's lifecycle.
     */
    private Execute() {
        this.screenManager = new ScreenManager();
    }

    /**
     * Returns the global instance of the application executor.
     *
     * @return The {@link Execute} singleton instance.
     */
    public static Execute getSingleton() {
        return singleton;
    }

    /**
     * Starts the application's primary execution flow.
     * <p>
     * This method is responsible for bootstrapping the initial user interface
     * by instructing the {@link ScreenManager} to display the {@link IntroScreen}.
     */
    @Override
    public void run() {
        screenManager.showScreen(IntroScreen.class);
    }
}