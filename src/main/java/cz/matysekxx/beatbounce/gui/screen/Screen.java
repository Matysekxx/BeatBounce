package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.util.ScreenUtil;
import cz.matysekxx.beatbounce.util.UIScale;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;

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
            ScreenUtil.applyFullscreen(this);
        } else {
            ScreenUtil.applyDefaultSize(this);
        }
        this.addWindowFocusListener(new FocusListener());

        try {
            final URL url = this.getClass().getResource("/icon.png");
            if (url != null) {
                final Image icon = ImageIO.read(url);
                this.setIconImage(icon);
            }
        } catch (Exception _) {
        }

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                UIScale.update(getWidth(), getHeight());
            }
        });
        UIScale.update(getWidth(), getHeight());
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