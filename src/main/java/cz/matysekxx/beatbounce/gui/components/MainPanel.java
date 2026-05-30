package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.util.Time;

import java.awt.*;

/**
 * A panel used as the background for the main menu.
 * It handles background rendering and animations.
 *
 * @author Matysekxx
 */
public class MainPanel extends BasePanel implements Runnable {
    /**
     * Flag indicating if the animation loop is active.
     */
    private boolean running = false;

    /**
     * The thread responsible for driving the menu background animation.
     */
    private Thread animatorThread;

    /**
     * Constructs a new MainMenuPanel.
     */
    public MainPanel() {
        super();
    }

    /**
     * Starts the animation thread for the background.
     */
    public void startAnimation() {
        if (!running) {
            running = true;
            animatorThread = new Thread(this);
            animatorThread.start();
        }
    }

    /**
     * Stops the animation thread.
     */
    public void stopAnimation() {
        running = false;
        if (animatorThread != null) {
            animatorThread.interrupt();
            animatorThread = null;
        }
    }

    /**
     * The main loop for the background animation, which handles repainting.
     */
    @Override
    public void run() {
        final long optimalTimeNanos = 1_000_000_000L / Settings.targetFps;
        long lastFpsTime = System.currentTimeMillis();
        while (running) {
            final long loopStartTime = System.nanoTime();
            repaint();
            if (System.currentTimeMillis() - lastFpsTime >= 1000) {
                lastFpsTime = System.currentTimeMillis();
            }
            if (Settings.vsync) Toolkit.getDefaultToolkit().sync();
            Time.delay(optimalTimeNanos, loopStartTime);
        }
    }

    @Override
    protected void drawBackground(Graphics2D g2d, int w, int h) {
        RenderUtils.drawBackground(g2d, w, h);
    }
}