package cz.matysekxx.beatbounce;

import cz.matysekxx.beatbounce.gui.screen.IntroScreen;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;

/// The main execution orchestrator for the BeatBounce application.
///
/// This class implements the [Runnable] interface and follows the **Singleton**
/// design pattern to ensure a single point of control for application lifecycle
/// events, such as starting the UI and managing screens.
///
/// ### Example Usage:
/// ```java
/// // Starting the application
/// Execute app = Execute.getSingleton();
/// app.run();
/// ```
public class Execute implements Runnable {

    /// The single static instance of the Execute class.
    private static final Execute singleton = new Execute();

    /// Manager responsible for handling UI screen transitions.
    private final ScreenManager screenManager;

    /// Private constructor to enforce the Singleton pattern.
    ///
    /// Initializes the [ScreenManager] which will be used throughout
    /// the application's lifecycle.
    private Execute() {
        this.screenManager = new ScreenManager();
    }

    /// Returns the global instance of the application executor.
    ///
    /// @return The [Execute] singleton instance.
    public static Execute getSingleton() {
        return singleton;
    }

    /// Starts the application's primary execution flow.
    ///
    /// This method is responsible for bootstrapping the initial user interface
    /// by instructing the [ScreenManager] to display the [IntroScreen].
    @Override
    public void run() {
        screenManager.showScreen(IntroScreen.class);
    }
}