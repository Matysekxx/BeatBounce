package cz.matysekxx.beatbounce.util;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.screen.Screen;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for screen-related operations.
 *
 * @author Matysekxx
 */
public class ScreenUtil {

    private static GraphicsDevice[] getDevices() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    }

    /**
     * Applies fullscreen mode to the given screen on the monitor specified in the settings.
     *
     * @param screen the screen to apply fullscreen to
     */
    public static void applyFullscreen(Screen screen) {
        final GraphicsDevice[] devices = getDevices();
        final GraphicsDevice device = (Settings.monitorIndex >= 0 && Settings.monitorIndex < devices.length) ? devices[Settings.monitorIndex] : devices[0];
        final Rectangle bounds = device.getDefaultConfiguration().getBounds();
        screen.setExtendedState(JFrame.NORMAL);
        screen.setBounds(bounds);
    }

    /**
     * Applies the default windowed size and position to the given screen on the monitor specified in the settings.
     *
     * @param screen the screen to apply the default size to
     */
    public static void applyDefaultSize(Screen screen) {
        screen.setExtendedState(JFrame.NORMAL);
        screen.setSize(1024, 768);
        screen.setMinimumSize(new Dimension(1024, 768));
        final GraphicsDevice[] devices = getDevices();
        final GraphicsDevice device = (Settings.monitorIndex >= 0 && Settings.monitorIndex < devices.length) ? devices[Settings.monitorIndex] : devices[0];
        final Rectangle bounds = device.getDefaultConfiguration().getBounds();
        screen.setLocation(bounds.x + (bounds.width - 1024) / 2, bounds.y + (bounds.height - 768) / 2);
    }
}
