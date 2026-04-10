package cz.matysekxx.beatbounce.core;

import cz.matysekxx.beatbounce.core.gui.screen.IntroScreen;

public class Execute implements Runnable {
    @Override
    public void run() {
        final IntroScreen introScreen = new IntroScreen();
    }
}
