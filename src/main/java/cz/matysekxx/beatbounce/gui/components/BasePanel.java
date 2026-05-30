package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

/**
 * Abstract base class for UI panels providing an off-screen cache for static backgrounds.
 * It manages resizing and reduces rendering overhead by caching the background image,
 * which prevents visual artifacts and performance drops when transitioning between monitors.
 *
 * @author Matysekxx
 */
public abstract class BasePanel extends JPanel {

    /**
     * Off-screen buffer for the static background elements.
     */
    protected BufferedImage bgCache;

    /**
     * Cached width of the panel.
     */
    protected int cachedW = -1;

    /**
     * Cached height of the panel.
     */
    protected int cachedH = -1;

    /**
     * Constructs a new BaseMenuPanel and sets up resize listeners to invalidate the cache.
     */
    public BasePanel() {
        super();
        this.setDoubleBuffered(true);
        this.setOpaque(true);
        this.setBackground(Color.BLACK);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                cachedW = -1;
                cachedH = -1;
            }
        });
    }

    /**
     * Handles the drawing of the cached background. Subclasses should call super.paintComponent(g)
     * if they override this method to draw animated elements on top.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        final int w = getWidth();
        final int h = getHeight();

        if (w <= 0 || h <= 0) return;

        if (bgCache == null || cachedW != w || cachedH != h) {
            cachedW = w;
            cachedH = h;
            bgCache = ((Graphics2D) g).getDeviceConfiguration()
                    .createCompatibleImage(w, h, Transparency.OPAQUE);
            final Graphics2D cg = bgCache.createGraphics();
            RenderUtils.initGraphics2D(cg);
            drawBackground(cg, w, h);
            cg.dispose();
        }
        g.drawImage(bgCache, 0, 0, null);
    }

    /**
     * Defines how the static background should be drawn. This is called only when the panel
     * is resized or initialized, and the result is cached.
     *
     * @param g2d The graphics context of the cached image buffer.
     * @param w   The width of the panel.
     * @param h   The height of the panel.
     */
    protected abstract void drawBackground(Graphics2D g2d, int w, int h);
}
