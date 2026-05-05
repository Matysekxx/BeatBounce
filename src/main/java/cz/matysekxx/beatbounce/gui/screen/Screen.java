package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.configuration.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

/**
 * Abstract base class for all game screens.
 * Extends {@link JFrame} and provides basic window configuration based on {@link Settings}.
 */
public abstract class Screen extends JFrame {

    /**
     * Constructs a new {@code Screen} and configures its properties such as title,
     * background color, and bounds based on fullscreen settings and monitor index.
     */
    public Screen() {
        this.setUndecorated(Settings.fullscreen);
        this.setTitle("BeatBounce");
        this.getContentPane().setBackground(Color.BLACK);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (Settings.fullscreen) {
            GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            GraphicsDevice device = (Settings.monitorIndex >= 0 && Settings.monitorIndex < devices.length) ? devices[Settings.monitorIndex] : devices[0];
            final Rectangle bounds = device.getDefaultConfiguration().getBounds();
            bounds.height += 1;
            this.setBounds(bounds);
        } else {
            this.setSize(1024, 768);
            this.setMinimumSize(new Dimension(1024, 768));
            final GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            final GraphicsDevice device = (Settings.monitorIndex >= 0 && Settings.monitorIndex < devices.length) ? devices[Settings.monitorIndex] : devices[0];
            final Rectangle bounds = device.getDefaultConfiguration().getBounds();
            this.setLocation(bounds.x + (bounds.width - 1024) / 2, bounds.y + (bounds.height - 768) / 2);
        }

        this.addWindowFocusListener(new WindowFocusListener() {
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
        });
    }

    /**
     * Called when the screen becomes active. Subclasses can override this to start animations or background tasks.
     */
    public void start() {

    }

    /**
     * Called when the screen becomes inactive. Subclasses can override this to stop animations or background tasks.
     */
    public void stop() {

    }
}