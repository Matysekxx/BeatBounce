package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.Star;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import cz.matysekxx.beatbounce.util.Time;

public class IntroPanel extends JPanel implements Runnable {
    private float time = 0;
    private boolean running = false;
    private Thread animatorThread;
    private final Collection<Star> stars = new ArrayList<>(STARS_COUNT);
    private static final int STARS_COUNT = 400;
    private int currentFps = 0;
    private int frameCount = 0;
    private long lastFpsTime = 0;

    public IntroPanel() {
        super();
        initStars();
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
        lastFpsTime = System.currentTimeMillis();
        while (running) {
            time += 0.04f;
            if (!stars.isEmpty()) {
                for (Star s : stars) s.update();
            }
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

    private void initStars() {
        for (int i = 0; i < STARS_COUNT; i++) stars.add(new Star());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g.create();
        RenderUtils.initGraphic2D(g2d);

        final int w = getWidth();
        final int h = getHeight();
        final int horizonY = (h >> 1) + 50;

        RenderUtils.drawBackground(g2d, w, h);
        RenderUtils.drawStars(g2d, stars, w, horizonY);
        RenderUtils.drawFloor(g2d, w, h, horizonY);

        drawIntroGrid(g2d, w, h, horizonY);

        RenderUtils.drawHorizonLine(g2d, w, horizonY);

        drawTitle(g2d, w, h);

        // RenderUtils.drawCRTScanlines(g2d, w, h);
        // RenderUtils.drawVignette(g2d, w, h);
        drawFPS(g2d);
        g2d.dispose();
    }

    private void drawFPS(Graphics2D g2d) {
        g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("FPS: " + currentFps, 10, 20);
    }

    private void drawIntroGrid(Graphics2D g2d, int w, int h, int horizonY) {
        g2d.setColor(new Color(0, 255, 255, 140));
        final int vanishingPointX = w >> 1;
        for (int i = -30; i <= 30; i++) {
            final int bottomX = vanishingPointX + i * 150;
            g2d.drawLine(vanishingPointX, horizonY, bottomX, h);
        }

        final float gridOffset = time % 1.0f;
        for (int z = 1; z <= 20; z++) {
            final double depth = Math.pow((z + gridOffset) / 20.0, 2.5);
            final int lineY = horizonY + (int) ((h - horizonY) * depth);
            if (lineY > horizonY && lineY <= h) {
                g2d.drawLine(0, lineY, w, lineY);
            }
        }
    }

    private void drawTitle(Graphics2D g2d, int w, int h) {
        final String text = "BEAT BOUNCE";
        g2d.setFont(new Font("Monospaced", Font.BOLD | Font.ITALIC, 115));
        final FontMetrics fm = g2d.getFontMetrics();
        final int x = (w - fm.stringWidth(text)) >> 1;
        final int y = (h / 3);

        RenderUtils.drawText(g2d, text, x, y, new Color(0, 255, 255));
    }
}