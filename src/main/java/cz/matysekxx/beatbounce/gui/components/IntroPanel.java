package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.util.Time;

import javax.swing.*;
import java.awt.*;


public class IntroPanel extends JPanel implements Runnable {
    private Particle[] particles = new Particle[0];
    private float time = 0;
    private boolean running = false;
    private Thread animatorThread;

    public IntroPanel() {
        super();
        this.setDoubleBuffered(true);
        this.setOpaque(true);
        updateParticleCount();
    }

    private void updateParticleCount() {
        final int count = switch (Settings.graphicsQuality) {
            case "LOW" -> 0;
            case "MEDIUM" -> 15;
            default -> 30;
        };
        if (particles.length != count) {
            particles = new Particle[count];
            for (int i = 0; i < count; i++) {
                particles[i] = new Particle(1920, 540);
            }
        }
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
        long lastTime = System.nanoTime();
        while (running) {
            updateParticleCount();
            final long now = System.nanoTime();
            final float dt = (now - lastTime) / 1_000_000_000f;
            lastTime = now;

            time += dt;

            final int w = getWidth() > 0 ? getWidth() : 1920;
            final int h = getHeight() > 0 ? (getHeight() / 2 + 100) : 540;
            if (Settings.particlesEnabled) {
                Particle.updateAll(particles, dt, w, h);
            }

            repaint();

            if (System.currentTimeMillis() - lastFpsTime >= 1000) {
                lastFpsTime = System.currentTimeMillis();
            }
            final long frameTimeMs = (long) (1000.0 / Settings.targetFps);
            Time.sleep(frameTimeMs);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2d);

        final int w = getWidth();
        final int h = getHeight();
        
        final Rectangle clipBounds = g2d.getClipBounds();
        if (clipBounds != null && (clipBounds.width == 0 || clipBounds.height == 0)) {
             g2d.dispose();
             return;
        }
        final int horizonY = (h >> 1) + 100;

        RenderUtils.drawBackground(g2d, w, h);
        if (Settings.particlesEnabled) {
            Particle.drawAll(g2d, particles);
        }
        RenderUtils.drawFloor(g2d, w, h, horizonY);

        drawIntroGrid(g2d, w, h, horizonY);
        RenderUtils.drawHorizonLine(g2d, w, horizonY);
        RenderUtils.drawTitle(g2d, w, h, "BEAT BOUNCE");

        g2d.dispose();
    }

    private void drawIntroGrid(Graphics2D g2d, int w, int h, int horizonY) {
        final int vanishingPointX = w >> 1;

        g2d.setColor(RenderCache.cyanWithAlpha(100));
        for (int i = -40; i <= 40; i++) {
            final int bottomX = vanishingPointX + i * 200;
            g2d.drawLine(vanishingPointX, horizonY, bottomX, h);
        }

        final float speed = 1.2f;
        final double angularFreq = Math.PI / 2.0;
        final double pos = speed * (time - (0.1 / angularFreq) * Math.cos(angularFreq * time));
        final float gridOffset = (float) (pos - Math.floor(pos));

        for (int z = 0; z <= 25; z++) {
            final double depth = Math.pow((z + gridOffset) / 25.0, 2.8);
            final int lineY = horizonY + (int) ((h - horizonY) * depth);

            if (lineY > horizonY && lineY <= h) {
                final int alpha = (int) (100 * depth);
                g2d.setColor(RenderCache.cyanWithAlpha(alpha));
                g2d.drawLine(0, lineY, w, lineY);
            }
        }
    }
}