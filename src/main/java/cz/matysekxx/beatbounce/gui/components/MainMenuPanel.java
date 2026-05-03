package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.util.Time;

import javax.swing.*;
import java.awt.*;

import cz.matysekxx.beatbounce.configuration.Settings;
import java.awt.image.BufferedImage;

public class MainMenuPanel extends JPanel implements Runnable {
    private boolean running = false;
    private Thread animatorThread;
    private BufferedImage bgCache;
    private int cachedW = -1;
    private int cachedH = -1;

    public MainMenuPanel() {
        this.setDoubleBuffered(true);
        this.setOpaque(true);
    }

    public void startAnimation() {
        if (!running) {
            running = true;
            animatorThread = new Thread(this);
            animatorThread.start();
        }
    }

    public void stopAnimation() {
        running = false;
        if (animatorThread != null) {
            animatorThread.interrupt();
            animatorThread = null;
        }
    }

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