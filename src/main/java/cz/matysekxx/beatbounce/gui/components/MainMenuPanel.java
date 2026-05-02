package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.util.Time;

import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel implements Runnable {
    private boolean running = false;
    private Thread animatorThread;


    public MainMenuPanel() {
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
            Time.sleep(16);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        RenderUtils.drawBackground(g2d, getWidth(), getHeight());
        g2d.dispose();
    }
}