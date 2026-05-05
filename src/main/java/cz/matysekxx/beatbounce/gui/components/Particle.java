package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import java.awt.*;


/**
 * Represents a visual particle in the background animation.
 * Particles move and fade to create a dynamic visual effect.
 */
public class Particle {
    float x, y, speed, opacity;
    Color color;
    Color renderedColor;

    /**
     * Constructs a new Particle with a random position.
     *
     * @param w the width of the bounds
     * @param h the height of the bounds
     */
    public Particle(int w, int h) {
        reset(w, h, true);
    }

    /**
     * Draws all particles in the given array.
     *
     * @param g2d       the graphics context to draw on
     * @param particles the array of particles to draw
     * @param len       the number of particles to draw from the array
     */
    public static void drawAll(Graphics2D g2d, Particle[] particles, int len) {
        for (int i = 0; i < len; i++) {
            final Particle p = particles[i];
            if (p.renderedColor != null) {
                g2d.setColor(p.renderedColor);
                g2d.fillOval((int) p.x, (int) p.y, 3, 3);
            }
        }
    }

    /**
     * Updates the position of all particles in the given array.
     *
     * @param particles the array of particles to update
     * @param len       the number of particles to update from the array
     * @param dt        the time elapsed since the last update in seconds
     * @param w         the width of the bounds
     * @param h         the height of the bounds
     */
    public static void updateAll(Particle[] particles, int len, float dt, int w, int h) {
        for (int i = 0; i < len; i++) {
            final Particle p = particles[i];
            p.update(dt, w, h);
        }
    }

    /**
     * Resets the particle to a new position and random properties.
     *
     * @param w       the width of the bounds
     * @param h       the height of the bounds
     * @param randomY whether to pick a random Y coordinate or start from the bottom
     */
    public void reset(int w, int h, boolean randomY) {
        x = (float) (Math.random() * w);
        y = randomY ? (float) (Math.random() * h) : h;
        speed = 10f + (float) (Math.random() * 20f);
        opacity = 0.3f + (float) (Math.random() * 0.5f);
        color = Math.random() > 0.5 ? RenderUtils.cyan : new Color(255, 0, 255);
        renderedColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (opacity * 255));
    }

    /**
     * Updates the particle's position based on its speed and the elapsed time.
     *
     * @param dt the time elapsed since the last update in seconds
     * @param w  the width of the bounds
     * @param h  the height of the bounds
     */
    public void update(float dt, int w, int h) {
        y -= speed * dt;
        if (y < 0) reset(w, h, false);
    }
}