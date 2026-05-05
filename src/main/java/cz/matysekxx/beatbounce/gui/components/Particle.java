package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import java.awt.*;


public class Particle {
    float x, y, speed, opacity;
    Color color;
    Color renderedColor;

    public Particle(int w, int h) {
        reset(w, h, true);
    }

    public static void drawAll(Graphics2D g2d, Particle[] particles) {
        for (Particle p : particles) {
            if (p.renderedColor != null) {
                g2d.setColor(p.renderedColor);
                g2d.fillOval((int) p.x, (int) p.y, 3, 3);
            }
        }
    }

    public static void drawAll(Graphics2D g2d, Particle[] particles, int len) {
        for (int i = 0; i < len; i++) {
            final Particle p = particles[i];
            if (p.renderedColor != null) {
                g2d.setColor(p.renderedColor);
                g2d.fillOval((int) p.x, (int) p.y, 3, 3);
            }
        }
    }

    public static void updateAll(Particle[] particles, float dt, int w, int h) {
        for (Particle p : particles) {
            p.update(dt, w, h);
        }
    }

    public static void updateAll(Particle[] particles, int len,float dt, int w, int h) {
        for (int i = 0; i < len; i++) {
            final Particle p = particles[i];
            p.update(dt, w, h);
        }
    }
    public void reset(int w, int h, boolean randomY) {
        x = (float) (Math.random() * w);
        y = randomY ? (float) (Math.random() * h) : h;
        speed = 10f + (float) (Math.random() * 20f);
        opacity = 0.3f + (float) (Math.random() * 0.5f);
        color = Math.random() > 0.5 ? RenderUtils.cyan : new Color(255, 0, 255);
        renderedColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (opacity * 255));
    }

    public void update(float dt, int w, int h) {
        y -= speed * dt;
        if (y < 0) reset(w, h, false);
    }
}