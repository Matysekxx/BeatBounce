package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.util.Time;

import cz.matysekxx.beatbounce.util.UIScale;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;

/**
 * A panel used for the intro screen, featuring animated particles and a stylized background.
 * It manages its own animation thread.
 */
public class IntroPanel extends BasePanel implements Runnable {
    /**
     * Particle array for the ambient background animation.
     */
    private final Particle[] particles;

    /**
     * The number of particles to update and render based on graphics settings.
     */
    private int count;

    /**
     * Internal animation timer in seconds.
     */
    private float time = 0;

    /**
     * Flag indicating if the animation thread is active.
     */
    private boolean running = false;

    /**
     * The thread responsible for driving the animation and repaints.
     */
    private Thread animatorThread;

    /**
     * Constructs a new IntroPanel.
     */
    public IntroPanel() {
        super();
        particles = new Particle[30];
        for (int i = 0; i < particles.length; i++)
            particles[i] = new Particle(1920, 540);
        updateParticleCount();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                UIScale.update(e.getComponent().getWidth(), e.getComponent().getHeight());
            }
        });
    }

    /**
     * Updates the particle count based on the current graphics quality settings.
     */
    private void updateParticleCount() {
        this.count = switch (Settings.graphicsQuality) {
            case "LOW" -> 0;
            case "MEDIUM" -> 15;
            default -> 30;
        };
    }

    /**
     * Starts the animation thread for the intro panel.
     * If the animation is already running, this method does nothing.
     */
    public void startAnimation() {
        if (!running) {
            running = true;
            if (cachedW == -1) cachedW = getWidth();
            if (cachedH == -1) cachedH = getHeight();
            if (cachedW > 0 && cachedH > 0) UIScale.update(cachedW, cachedH);
            animatorThread = new Thread(this);
            animatorThread.start();
        }
    }

    /**
     * Stops the animation thread.
     * If the animation is not running, this method does nothing.
     */
    public void stopAnimation() {
        running = false;
        if (animatorThread != null) {
            animatorThread.interrupt();
            animatorThread = null;
        }
    }

    /**
     * The main loop for the intro animation, handling particle updates and repaints.
     * This method is executed in a separate thread.
     */
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

            Time.delay(optimalTimeNanos, loopStartTime);
        }
    }

    /**
     * Draws the background elements including the primary background and the floor.
     *
     * @param g2d the graphics context
     * @param w   the width of the panel
     * @param h   the height of the panel
     */
    @Override
    protected void drawBackground(Graphics2D g2d, int w, int h) {
        final int horizonY = (h >> 1) + UIScale.scale(100);
        RenderUtils.drawBackground(g2d, w, h);
        RenderUtils.drawFloor(g2d, w, h, horizonY);
    }

    /**
     * Paints the component and its animated elements.
     *
     * @param g the graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        final int w = getWidth();
        final int h = getHeight();
        if (w <= 0 || h <= 0) return;

        final Graphics2D g2d = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2d);

        final int horizonY = (h >> 1) + UIScale.scale(100);
        final float globalHue = (time * 0.05f) % 1.0f;

        drawAudioBars(g2d, w, horizonY, globalHue);

        if (Settings.particlesEnabled) {
            Particle.drawAll(g2d, particles, count);
        }

        drawIntroGrid(g2d, w, h, horizonY, globalHue);
        drawShapes(g2d, w, horizonY, globalHue);

        final Color horizonColor = Color.getHSBColor(globalHue, 0.6f, 1.0f);
        g2d.setColor(RenderCache.customColorWithAlpha(horizonColor, 180));
        g2d.setStroke(RenderCache.STROKE_3);
        g2d.drawLine(0, horizonY, w, horizonY);
        g2d.setStroke(RenderCache.STROKE_1);
        g2d.setColor(RenderCache.whiteWithAlpha(200));
        g2d.drawLine(0, horizonY, w, horizonY);

        drawTitle(g2d, w, h, globalHue);
        drawVignette(g2d, w, h);
        g2d.dispose();
        if (Settings.vsync) {
            Toolkit.getDefaultToolkit().sync();
        }
    }

    /**
     * Renders a vignette effect over the entire panel.
     */
    private void drawVignette(Graphics2D g2d, int w, int h) {
        final float[] dist = {0.0f, 1.0f};
        final Color[] colors = {new Color(0, 0, 0, 0), new Color(0, 0, 0, 160)};
        final RadialGradientPaint p = new RadialGradientPaint(w / 2f, h / 2f, (float) Math.hypot(w / 2.0, h / 2.0), dist, colors);
        g2d.setPaint(p);
        g2d.fillRect(0, 0, w, h);
    }

    /**
     * Renders floating geometric shapes in the background.
     */
    private void drawShapes(Graphics2D g2d, int w, int horizonY, float globalHue) {
        final int shapes = 12;
        for (int i = 0; i < shapes; i++) {
            final float phase = time * 0.35f + i * 1.6f;
            final float x = w * 0.2f + (w * 0.7f) * ((float) i / (shapes - 1));
            final float y = horizonY - UIScale.scale(130) - (float) Math.sin(phase) * UIScale.scale(50) - i * UIScale.scale(12);
            final float size = UIScale.scale(24) + (float) Math.sin(phase * 0.7f) * UIScale.scale(8);
            final float rotation = time * 0.4f + i;
            final float alpha = 0.22f + (float) Math.sin(phase) * 0.1f;

            final AffineTransform old = g2d.getTransform();
            g2d.translate(x, y);
            g2d.rotate(rotation);
            g2d.scale(size, size);

            final Color shapeColor = Color.getHSBColor((globalHue + i * 0.06f) % 1.0f, 0.65f, 1.0f);
            final Shape shape = getShape(i);

            if (Settings.bloomEnabled) {
                g2d.setColor(RenderCache.customColorWithAlpha(shapeColor, (int) (255 * alpha * 0.3f)));
                g2d.setStroke(new BasicStroke(3f / size));
                g2d.draw(shape);
            }

            g2d.setColor(RenderCache.customColorWithAlpha(shapeColor, (int) (255 * alpha)));
            g2d.setStroke(new BasicStroke(2.0f / size));
            g2d.draw(shape);

            final Color fillColor = Color.getHSBColor((globalHue + 0.5f) % 1.0f, 0.8f, 1.0f);
            g2d.setColor(RenderCache.customColorWithAlpha(fillColor, (int) (255 * alpha * 0.2f)));
            g2d.fill(shape);

            g2d.setTransform(old);
        }
    }

    /**
     * Returns a predefined shape based on index.
     */
    private Shape getShape(int index) {
        return switch (index % 5) {
            case 0 -> RenderCache.SHAPE_TRIANGLE;
            case 1 -> RenderCache.SHAPE_DIAMOND;
            case 3 -> RenderCache.SHAPE_PENTAGON;
            case 4 -> RenderCache.SHAPE_SQUARE;
            default -> RenderCache.SHAPE_HEXAGON;
        };
    }

    /**
     * Renders simulated audio frequency bars.
     */
    private void drawAudioBars(Graphics2D g2d, int w, int horizonY, float globalHue) {
        final int barCount = 120;
        final float barWidth = (float) w / barCount;
        final int maxHeight = UIScale.scale(160);

        for (int i = 0; i < barCount; i++) {
            final float height = getHeight(i);
            final float x = i * barWidth;
            final float y = horizonY - height;
            final float alpha = 0.08f + (height / maxHeight) * 0.2f;

            final float distFromCenter = Math.abs((i - barCount / 2.0f) / (barCount / 2.0f));
            final Color barColor = Color.getHSBColor((globalHue + distFromCenter * 0.2f) % 1.0f, 0.7f, 1.0f);

            g2d.setColor(RenderCache.customColorWithAlpha(barColor, (int) (255 * alpha)));
            g2d.fillRect((int) x, (int) y, (int) (barWidth - 2), (int) height);

            g2d.setColor(RenderCache.whiteWithAlpha((int) (255 * Math.min(1.0f, alpha * 2.5f))));
            g2d.fillRect((int) x, (int) y, (int) (barWidth - 2), 4);

            final float reflectionHeight = height * 0.4f;
            final Color reflectionColor = Color.getHSBColor((globalHue + 0.5f + distFromCenter * 0.2f) % 1.0f, 0.7f, 1.0f);
            g2d.setColor(RenderCache.customColorWithAlpha(reflectionColor, (int) (255 * alpha * 0.4f)));
            g2d.fillRect((int) x, horizonY, (int) (barWidth - 2), (int) reflectionHeight);
        }
    }

    /**
     * Calculates the animated height of an audio bar.
     */
    private float getHeight(int i) {
        final float freq1 = (float) (Math.sin(time * 2 + i * 0.15) * 0.5 + 0.5);
        final float freq2 = (float) (Math.sin(time * 3.5 + i * 0.4) * 0.3 + 0.3);
        final float freq3 = (float) (Math.sin(time * 1.2 + i * 0.05) * 0.2 + 0.2);
        final float freq4 = (float) (Math.cos(time * 5.0 - i * 0.2) * 0.1 + 0.1);

        final float globalBounce = (float) (Math.sin(time * 4) * 0.5 + 0.5) * UIScale.scale(15);
        return (freq1 * 0.4f + freq2 * 0.3f + freq3 * 0.2f + freq4 * 0.1f) * UIScale.scale(160) + globalBounce;
    }

    /**
     * Renders the animated main title.
     */
    private void drawTitle(Graphics2D g2d, int w, int h, float globalHue) {
        final String text = "BEAT BOUNCE";
        final Font font = UIScale.scaleFont(RenderCache.MONO_ITALIC_BOLD_150);
        g2d.setFont(font);

        final FontMetrics fm = g2d.getFontMetrics();
        final int textWidth = fm.stringWidth(text);
        final int drawX = (w - textWidth) / 2;

        final float floatingOffset = (float) (Math.sin(time * 0.8) * UIScale.scale(15.0f));
        final int drawY = (int) (h / 4.f + UIScale.scale(40) + floatingOffset);

        final double pulse = (Math.sin(System.currentTimeMillis() / 400.0) + 1.0) / 2.0;

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final Color bloomColor = Color.getHSBColor(globalHue, 0.85f, 1.0f);
        final Color faceColor = Color.getHSBColor(globalHue, 0.25f, 1.0f);

        final GlyphVector gv = font.createGlyphVector(g2d.getFontRenderContext(), text);
        final Shape textShape = gv.getOutline(drawX, drawY);

        if (Settings.bloomEnabled) {
            g2d.setColor(RenderCache.customColorWithAlpha(bloomColor, (int) (80 * pulse)));
            g2d.setStroke(new BasicStroke(UIScale.scale(10.0f)));
            g2d.draw(textShape);

            g2d.setColor(RenderCache.customColorWithAlpha(bloomColor, (int) (140 * pulse)));
            g2d.setStroke(new BasicStroke(UIScale.scale(5.0f)));
            g2d.draw(textShape);
        }

        g2d.setColor(bloomColor);
        g2d.setStroke(new BasicStroke(UIScale.scale(2.0f)));
        g2d.draw(textShape);

        g2d.setColor(RenderCache.blackWithAlpha(180));
        g2d.drawString(text, drawX + UIScale.scale(3), drawY + UIScale.scale(3));

        g2d.setColor(RenderCache.customColorWithAlpha(faceColor, 230));
        g2d.drawString(text, drawX, drawY);
    }


    /**
     * Renders the animated futuristic grid on the floor.
     */
    private void drawIntroGrid(Graphics2D g2d, int w, int h, int horizonY, float globalHue) {
        final int vanishingPointX = w >> 1;

        final Color vertGridColor = Color.getHSBColor((globalHue + 0.1f) % 1.0f, 0.7f, 1.0f);
        g2d.setStroke(RenderCache.STROKE_2);
        g2d.setColor(RenderCache.customColorWithAlpha(vertGridColor, 120));
        for (int i = -50; i <= 50; i++) {
            final int bottomX = vanishingPointX + i * UIScale.scale(180);
            g2d.drawLine(vanishingPointX, horizonY, bottomX, h);
        }
        g2d.setStroke(RenderCache.STROKE_1);

        final float speed = 1.5f;
        final double angularFreq = Math.PI / 2.0;
        final double pos = speed * (time - (0.1 / angularFreq) * Math.cos(angularFreq * time));
        final float gridOffset = (float) (pos - Math.floor(pos));

        for (int z = 0; z <= 35; z++) {
            final double depth = Math.pow((z + gridOffset) / 35.0, 3.2);
            final int lineY = horizonY + (int) ((h - horizonY) * depth);

            if (lineY > horizonY && lineY <= h) {
                final int alpha = (int) (140 * depth);
                final Color horizGridColor = Color.getHSBColor((globalHue + (z % 5) * 0.05f) % 1.0f, 0.8f, 1.0f);

                g2d.setColor(RenderCache.customColorWithAlpha(horizGridColor, Math.clamp(alpha, 0, 255)));
                g2d.drawLine(0, lineY, w, lineY);
            }
        }
    }
}
