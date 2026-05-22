package cz.matysekxx.beatbounce.gui.components;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.util.Time;
import cz.matysekxx.beatbounce.util.UIScale;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel that displays game credits with a scrolling animation and a rich animated background.
 */
public class CreditsPanel extends BasePanel implements Runnable {
    /**
     * Array of background particles.
     */
    private final Particle[] particles;
    /**
     * Current vertical scroll position of the credits list.
     */
    private float scrollY = 0;
    /**
     * Flag indicating whether the animation loop is running.
     */
    private boolean running = false;
    /**
     * Thread responsible for running the animation loop.
     */
    private Thread animatorThread;
    /**
     * List of credit entries to be displayed.
     */
    private List<CreditEntry> credits = new ArrayList<>();
    /**
     * Elapsed time for animation calculation.
     */
    private float time = 0;
    /**
     * Number of particles currently active based on graphics settings.
     */
    private int particleCount;

    public CreditsPanel() {
        super();
        particles = new Particle[40];
        for (int i = 0; i < particles.length; i++)
            particles[i] = new Particle(1920, 540);
        updateParticleCount();
        loadCredits();
    }

    /**
     * Updates the particle count based on the current graphics quality settings.
     */
    private void updateParticleCount() {
        this.particleCount = switch (Settings.graphicsQuality) {
            case "LOW" -> 0;
            case "MEDIUM" -> 20;
            default -> 40;
        };
    }

    /**
     * Loads the credit entries from the JSON resource file.
     */
    private void loadCredits() {
        try (InputStream is = getClass().getResourceAsStream("/credits.json")) {
            if (is != null) {
                ObjectMapper mapper = new ObjectMapper();
                credits = mapper.readValue(is, new TypeReference<>() {
                });
            }
        } catch (Exception _) {
        }
    }

    /**
     * Starts the animation thread for the credits screen.
     */
    public void startAnimation() {
        if (!running) {
            running = true;
            scrollY = -UIScale.scale(100);
            animatorThread = new Thread(this);
            animatorThread.start();
        }
    }

    /**
     * Stops the animation thread for the credits screen.
     */
    public void stopAnimation() {
        running = false;
        if (animatorThread != null) {
            animatorThread.interrupt();
            animatorThread = null;
        }
    }

    /**
     * The main loop for the credits animation, handling updates and repaints.
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

            final int w;
            if (cachedW > 0) w = cachedW;
            else w = getWidth() > 0 ? getWidth() : 1920;

            final int h;
            if (cachedH > 0) h = cachedH;
            else h = getHeight() > 0 ? getHeight() : 1080;

            if (Settings.particlesEnabled) Particle.updateAll(particles, particleCount, dt, w, h);

            scrollY += dt * UIScale.scale(50);

            float totalHeight = credits.size() * UIScale.scale(60) + h;
            if (scrollY > totalHeight) {
                scrollY = -UIScale.scale(200);
            }

            repaint();
            Time.delay(optimalTimeNanos, loopStartTime);
        }
    }

    /**
     * Paints the credit components and background effects.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2d);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        final float globalHue = (time * 0.05f) % 1.0f;

        RenderUtils.drawBackground(g2d, w, h);

        if (Settings.particlesEnabled) {
            Particle.drawAll(g2d, particles, particleCount);
        }

        drawFloatingShapes(g2d, w, h, globalHue);

        drawCredits(g2d, w, h);

        drawVignette(g2d, w, h);

        g2d.dispose();
    }

    /**
     * Renders floating background shapes.
     *
     * @param g2d       graphics context
     * @param w         panel width
     * @param h         panel height
     * @param globalHue global hue for color cycling
     */
    private void drawFloatingShapes(Graphics2D g2d, int w, int h, float globalHue) {
        final int shapesPerSide = 12;
        for (int i = 0; i < shapesPerSide * 2; i++) {
            boolean isLeft = i < shapesPerSide;
            int sideIndex = i % shapesPerSide;

            final float phase = time * 0.25f + i * 1.2f;

            float x;
            if (isLeft) {
                x = w * 0.05f + (w * 0.2f) * ((float) sideIndex / (shapesPerSide - 1));
            } else {
                x = w * 0.75f + (w * 0.2f) * ((float) sideIndex / (shapesPerSide - 1));
            }

            final float y = h * 0.1f + (h * 0.8f) * ((float) sideIndex / (shapesPerSide - 1)) + (float) Math.sin(phase) * UIScale.scale(50);
            final float size = UIScale.scale(35) + (float) Math.sin(phase * 0.8f) * UIScale.scale(15);
            final float rotation = time * 0.3f + i;
            final float alpha = 0.12f + (float) Math.sin(phase) * 0.06f;

            final AffineTransform old = g2d.getTransform();
            g2d.translate(x, y);
            g2d.rotate(rotation);
            g2d.scale(size, size);

            final Color shapeColor = Color.getHSBColor((globalHue + i * 0.07f) % 1.0f, 0.6f, 1.0f);
            final Shape shape = getShape(i);

            g2d.setColor(RenderCache.customColorWithAlpha(shapeColor, (int) (255 * alpha)));
            g2d.setStroke(new BasicStroke(2.0f / size));
            g2d.draw(shape);

            final Color fillColor = Color.getHSBColor((globalHue + 0.5f) % 1.0f, 0.7f, 1.0f);
            g2d.setColor(RenderCache.customColorWithAlpha(fillColor, (int) (255 * alpha * 0.12f)));
            g2d.fill(shape);

            g2d.setTransform(old);
        }
    }

    /**
     * Gets a shape based on the index.
     *
     * @param index the shape index
     * @return the shape
     */
    private Shape getShape(int index) {
        return switch (index % 4) {
            case 0 -> RenderCache.SHAPE_TRIANGLE;
            case 1 -> RenderCache.SHAPE_DIAMOND;
            case 2 -> RenderCache.SHAPE_HEXAGON;
            case 3 -> RenderCache.SHAPE_PENTAGON;
            default -> RenderCache.SHAPE_SQUARE;
        };
    }

    /**
     * Draws the credits text.
     *
     * @param g2d graphics context
     * @param w   panel width
     * @param h   panel height
     */
    private void drawCredits(Graphics2D g2d, int w, int h) {
        float currentY = h - scrollY;
        float lineHeight = UIScale.scale(60);

        for (CreditEntry entry : credits) {
            if (currentY > -100 && currentY < h + 100) {
                if (!entry.text.isEmpty()) {
                    g2d.setFont(entry.isTitle ?
                            UIScale.scaleFont(RenderCache.MONO_ITALIC_BOLD_48) :
                            UIScale.scaleFont(RenderCache.MONO_ITALIC_BOLD_24));

                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (w - fm.stringWidth(entry.text)) / 2;

                    Color textColor = entry.getAwtColor();
                    if (entry.isTitle) {
                        double pulse = (Math.sin(time * 3) + 1.0) / 2.0;
                        RenderUtils.drawBloom(g2d, entry.text, x, (int) currentY, pulse, textColor);
                        g2d.setColor(Color.WHITE);
                    } else {
                        g2d.setColor(textColor);
                    }

                    g2d.drawString(entry.text, x, currentY);
                }
            }
            currentY += lineHeight;
        }
    }

    /**
     * Renders a vignette overlay.
     *
     * @param g2d graphics context
     * @param w   panel width
     * @param h   panel height
     */
    private void drawVignette(Graphics2D g2d, int w, int h) {
        final float[] dist = {0.0f, 0.8f, 1.0f};
        final Color[] colors = {new Color(0, 0, 0, 0), new Color(0, 0, 0, 40), new Color(0, 0, 0, 220)};
        final RadialGradientPaint p = new RadialGradientPaint(w / 2f, h / 2f, (float) Math.hypot(w / 2.0, h / 2.0), dist, colors);
        g2d.setPaint(p);
        g2d.fillRect(0, 0, w, h);
    }

    @Override
    protected void drawBackground(Graphics2D g2d, int w, int h) {
    }

    /**
     * Represents a single entry in the credits list.
     */
    public static class CreditEntry {
        /**
         * The credit text.
         */
        public String text;
        /**
         * The hex color for the text.
         */
        public String color;
        /**
         * Whether this entry is a title/header.
         */
        public boolean isTitle;

        /**
         * Decodes the hex color string to an AWT Color object.
         *
         * @return the color object
         */
        public Color getAwtColor() {
            if (color == null || color.isEmpty()) return Color.WHITE;
            try {
                return Color.decode(color);
            } catch (Exception e) {
                return Color.WHITE;
            }
        }
    }
}
