package cz.matysekxx.beatbounce.util;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.screen.Screen;

import javax.swing.*;
import java.awt.*;

public class ScreenUtil {
    public static void applyFullscreen(Screen screen) {
        final GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        final GraphicsDevice device = (Settings.monitorIndex >= 0 && Settings.monitorIndex < devices.length) ? devices[Settings.monitorIndex] : devices[0];
        final Rectangle bounds = device.getDefaultConfiguration().getBounds();
        bounds.height += 1;
        screen.setBounds(bounds);
    }

    public static void applyDefaultSize(Screen screen) {
        screen.setExtendedState(JFrame.NORMAL);
        screen.setSize(1024, 768);
        screen.setMinimumSize(new Dimension(1024, 768));
        final GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        final GraphicsDevice device = (Settings.monitorIndex >= 0 && Settings.monitorIndex < devices.length) ? devices[Settings.monitorIndex] : devices[0];
        final Rectangle bounds = device.getDefaultConfiguration().getBounds();
        screen.setLocation(bounds.x + (bounds.width - 1024) / 2, bounds.y + (bounds.height - 768) / 2);
    }
}
