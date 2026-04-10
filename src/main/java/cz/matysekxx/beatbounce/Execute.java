package cz.matysekxx.beatbounce;

import cz.matysekxx.beatbounce.gui.screen.IntroScreen;

public class Execute implements Runnable {
    @Override
    public void run() {
        final IntroScreen introScreen = DIContainer.getComponent(IntroScreen.class);
    }
}
