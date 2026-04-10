package cz.matysekxx.beatbounce;

import cz.matysekxx.beatbounce.gui.screen.IntroScreen;

public class Execute implements Runnable {
    private static final Execute singleton = new Execute();
    public static Execute getSingleton() {
        return singleton;
    }
    @Override
    public void run() {
        final IntroScreen introScreen = DIContainer.getComponent(IntroScreen.class);
    }
}
