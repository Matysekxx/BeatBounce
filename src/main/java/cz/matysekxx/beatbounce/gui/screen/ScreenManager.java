package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.DIContainer;

public class ScreenManager {
    static {
        DIContainer.register(ScreenManager.class, new ScreenManager());
    }

    private ScreenManager() {}
}
