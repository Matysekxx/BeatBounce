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
        while (running) {
            repaint();
            Time.sleep(16);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        RenderUtils.initGraphic2D(g2d);
        final GradientPaint bg = new GradientPaint(0, 0, new Color(22, 22, 26), getWidth(), getHeight(), new Color(8, 8, 10));
        g2d.setPaint(bg);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }
}