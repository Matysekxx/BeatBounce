package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.configuration.Settings;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

public class FocusListener implements WindowFocusListener {
    @Override
    public void windowGainedFocus(WindowEvent e) {
        if (Settings.muteOnFocusLoss) {
            Settings.isMuted = false;
        }
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        if (Settings.muteOnFocusLoss) {
            Settings.isMuted = true;
        }
    }
}
