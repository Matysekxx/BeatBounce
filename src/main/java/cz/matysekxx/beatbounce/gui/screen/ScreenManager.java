package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.DIContainer;

public class ScreenManager {
    static {
        DIContainer.register(ScreenManager.class, new ScreenManager());
    }

    private ScreenManager() {}

    public MainMenuScreen showMainMenuScreen() {
        return new MainMenuScreen();
    }

    public GameScreen showGameScreen() {
        return new GameScreen();
    }

    public IntroScreen showIntroScreen() {
        return new IntroScreen();
    }
}
