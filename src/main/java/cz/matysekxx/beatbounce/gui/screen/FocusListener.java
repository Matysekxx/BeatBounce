package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.configuration.Settings;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

/**
 * A listener that mutes or unmutes the application audio based on window focus.
 * It checks the {@link Settings#muteOnFocusLoss} setting to determine whether to act.
 */
public class FocusListener implements WindowFocusListener {
    /**
     * Called when the window gains focus. Unmutes the audio if {@link Settings#muteOnFocusLoss} is enabled.
     *
     * @param e the window event
     */
    @Override
    public void windowGainedFocus(WindowEvent e) {
        if (Settings.muteOnFocusLoss) {
            Settings.isMuted = false;
        }
    }

    /**
     * Called when the window loses focus. Mutes the audio if {@link Settings#muteOnFocusLoss} is enabled.
     *
     * @param e the window event
     */
    @Override
    public void windowLostFocus(WindowEvent e) {
        if (Settings.muteOnFocusLoss) {
            Settings.isMuted = true;
        }
    }
}
