package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.util.Time;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A panel used as the background for the main menu.
 * It handles background rendering and animations.
 */
public class MainMenuPanel extends JPanel implements Runnable {
    private boolean running = false;
    private Thread animatorThread;
    private BufferedImage bgCache;
    private int cachedW = -1;
    private int cachedH = -1;

    /**
     * Constructs a new MainMenuPanel.
     */
    public MainMenuPanel() {
        this.setDoubleBuffered(true);
        this.setOpaque(true);
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
        long lastFpsTime = System.currentTimeMillis();
        while (running) {
            repaint();
            if (System.currentTimeMillis() - lastFpsTime >= 1000) {
                lastFpsTime = System.currentTimeMillis();
            }
            long frameTimeMs = (long) (1000.0 / Settings.targetFps);
            Time.sleep(frameTimeMs);
        }
    }

    /**
     * Paints the background component, using a cache to improve performance.
     *
     * @param g the graphics context to paint on
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final int w = getWidth();
        final int h = getHeight();

        if (bgCache == null || cachedW != w || cachedH != h) {
            cachedW = w;
            cachedH = h;
            bgCache = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_RGB);
            final Graphics2D cg = bgCache.createGraphics();
            RenderUtils.initGraphics2D(cg);
            RenderUtils.drawBackground(cg, w, h);
            cg.dispose();
        }
        g.drawImage(bgCache, 0, 0, null);
    }
}