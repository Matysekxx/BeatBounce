package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.LockSupport;

public class IntroPanel extends JPanel implements Runnable {
    private final Particle[] particles;
    private int count;
    private float time = 0;
    private boolean running = false;
    private Thread animatorThread;

    private int cachedW = -1;
    private int cachedH = -1;
    private BufferedImage staticBackgroundCache;
    private int cachedBgW = -1;
    private int cachedBgH = -1;

    public IntroPanel() {
        super();
        this.setDoubleBuffered(true);
        this.setOpaque(true);
        particles = new Particle[30];
        for (int i = 0; i < particles.length; i++)
            particles[i] = new Particle(1920, 540);
        updateParticleCount();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                cachedW = e.getComponent().getWidth();
                cachedH = e.getComponent().getHeight();
            }
        });
    }

    private void updateParticleCount() {
        this.count = switch (Settings.graphicsQuality) {
            case "LOW" -> 0;
            case "MEDIUM" -> 15;
            default -> 30;
        };
    }

    public void startAnimation() {
        if (!running) {
            running = true;
            if (cachedW == -1) cachedW = getWidth();
            if (cachedH == -1) cachedH = getHeight();
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
        final long optimalTimeNanos = 1_000_000_000L / Settings.targetFps;
        long lastTime = System.nanoTime();

        while (running) {
            final long loopStartTime = System.nanoTime();
            updateParticleCount();

            final float dt = (loopStartTime - lastTime) / 1_000_000_000f;
            lastTime = loopStartTime;
            time += dt;

            final int w = (cachedW > 0) ? cachedW : (getWidth() > 0 ? getWidth() : 1920);
            final int h = (cachedH > 0) ? cachedH : (getHeight() > 0 ? getHeight() : 1080);

            if (Settings.particlesEnabled) {
                Particle.updateAll(particles, count, dt, w, h);
            }
            repaint();

            RenderUtils.delay(optimalTimeNanos, loopStartTime);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        final int w = getWidth();
        final int h = getHeight();
        if (w <= 0 || h <= 0) return;

        final Graphics2D g2d = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2d);

        final int horizonY = (h >> 1) + 100;

        if (staticBackgroundCache == null || cachedBgW != w || cachedBgH != h) {
            cachedBgW = w;
            cachedBgH = h;
            staticBackgroundCache = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration()
                    .createCompatibleImage(w, h, Transparency.OPAQUE);
            final Graphics2D bgG2d = staticBackgroundCache.createGraphics();
            RenderUtils.initGraphics2D(bgG2d);
            RenderUtils.drawBackground(bgG2d, w, h);
            RenderUtils.drawFloor(bgG2d, w, h, horizonY);
            bgG2d.dispose();
        }
        g2d.drawImage(staticBackgroundCache, 0, 0, null);

        if (Settings.particlesEnabled) {
            Particle.drawAll(g2d, particles, count);
        }

        drawIntroGrid(g2d, w, h, horizonY);
        RenderUtils.drawHorizonLine(g2d, w, horizonY);
        RenderUtils.drawTitle(g2d, w, h, "BEAT BOUNCE");

        g2d.dispose();

        if (Settings.vsync) {
            Toolkit.getDefaultToolkit().sync();
        }
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