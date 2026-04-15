package cz.matysekxx.beatbounce;

import cz.matysekxx.beatbounce.gui.screen.IntroScreen;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;

public class Execute implements Runnable {
    private static final Execute singleton = new Execute();

    private final ScreenManager screenManager;

    private Execute() {
        this.screenManager = new ScreenManager();
    }

    public static Execute getSingleton() {
        return singleton;
    }

    @Override
    public void run() {
        screenManager.showScreen(IntroScreen.class);
    }
}
