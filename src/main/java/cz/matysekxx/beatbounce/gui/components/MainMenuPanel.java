package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.util.Time;

import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel implements Runnable {
    private boolean running = false;
    private Thread animatorThread;
    private float time = 0f;
    private int currentFps = 0;
    private int frameCount = 0;
    private long lastFpsTime = 0;


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
        lastFpsTime = System.currentTimeMillis();
        while (running) {
            time += 0.01f;
            repaint();

            frameCount++;
            if (System.currentTimeMillis() - lastFpsTime >= 1000) {
                currentFps = frameCount;
                frameCount = 0;
                lastFpsTime = System.currentTimeMillis();
            }

            Time.sleep(16);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        RenderUtils.drawAuroraBackground(g2d, getWidth(), getHeight(), time);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("FPS: " + currentFps, 10, 20);

        g2d.dispose();
    }
}