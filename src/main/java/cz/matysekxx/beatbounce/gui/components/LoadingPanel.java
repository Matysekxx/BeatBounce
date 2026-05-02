package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.util.Time;

import javax.swing.*;
import java.awt.*;

public class LoadingPanel extends JPanel implements Runnable {
    private static final Font TITLE_FONT = new Font("Monospaced", Font.BOLD | Font.ITALIC, 72);
    private static final Font PERCENT_FONT = new Font("Monospaced", Font.BOLD, 16);
    private static final Font FOOTER_FONT = new Font("SansSerif", Font.PLAIN, 20);
    private static final Stroke BAR_STROKE = new BasicStroke(1.5f);
    private static final int BAR_HEIGHT = 20;
    private static final Color FILL_GLOW_COLOR = new Color(
            RenderUtils.cyan.getRed(), RenderUtils.cyan.getGreen(), RenderUtils.cyan.getBlue(), 40
    );
    private static final Color BORDER_GLOW_COLOR = new Color(0, 255, 220, 60);
    private static final Color PERCENT_SHADOW_COLOR = new Color(0, 255, 220, 30);
    private static final Color PERCENT_TEXT_COLOR = new Color(255, 255, 255, 200);
    private static final Color FOOTER_COLOR = new Color(180, 180, 200, 120);
    private float time = 0.f;
    private float progress = 0.f;
    private Thread animationThread;
    private volatile boolean running = false;
    private String text = "Loading Level...";

    public LoadingPanel() {
        setOpaque(true);
        setBackground(Color.BLACK);
    }

    public void setText(String text) {
        this.text = text;
    }

    public void startAnimation() {
        if (!running) {
            running = true;
            progress = 0f;
            time = 0f;
            animationThread = new Thread(this);
            animationThread.start();
        }
    }

    public void stopAnimation() {
        running = false;
        if (animationThread != null) {
            animationThread.interrupt();
            try {
                animationThread.join(200);
            } catch (InterruptedException ignored) {
            }
            animationThread = null;
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            final float dt = (now - lastTime) / 1_000_000_000f;
            lastTime = now;

            updateLogic(dt);
            repaint();
            Time.sleep(16);
        }
    }

    private void updateLogic(float dt) {
        time += dt;
        if (progress < 0.99f) {
            final float remaining = 1.0f - progress;
            float step = remaining * dt * 0.8f;
            if (Math.random() < 0.05) {
                step += (float) (Math.random() * 0.04f);
            }
            progress += step;
            if (progress > 0.99f) progress = 0.99f;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g.create();
        final int w = getWidth(), h = getHeight();
        final float pulse = (float) ((Math.sin(time * 2.0) + 1.0) / 2.0);

        RenderUtils.initGraphics2D(g2);
        RenderUtils.drawBackground(g2, w, h);

        drawTitle(g2, w, h, pulse);
        drawProgressBar(g2, w, h, pulse);
        drawFooter(g2, w, h);

        RenderUtils.applyNoiseOverlay(g2, 0, 0, w, h);
        g2.dispose();
    }

    private void drawTitle(Graphics2D g2, int w, int h, float pulse) {
        final int dotsCount = (int) (time * 2.5f) % 4;
        String drawText = text == null ? "" : text;
        if (drawText.endsWith("...")) {
            drawText = drawText.substring(0, drawText.length() - 3) + ".".repeat(dotsCount);
        }

        g2.setFont(TITLE_FONT);
        final FontMetrics fm = g2.getFontMetrics();
        final int x = (w - fm.stringWidth(drawText)) / 2;
        final int y = h / 3;

        for (int i = 4; i >= 1; i--) {
            final float alpha = 0.04f + pulse * 0.03f;
            g2.setColor(new Color(0, 255, 220, (int) (alpha * 255 / i)));
            g2.drawString(drawText, x - i, y);
            g2.drawString(drawText, x + i, y);
        }

        g2.setColor(RenderUtils.cyan);
        g2.drawString(drawText, x, y);
        g2.setColor(Color.WHITE);
        g2.drawString(drawText, x, y - 1);
    }

    private void drawProgressBar(Graphics2D g2, int w, int h, float pulse) {
        final int barWidth = Math.min(600, (int) (w * 0.45));
        final int barX = (w - barWidth) >> 1;
        final int barY = (int) (h * 0.62);
        final int arcSize = BAR_HEIGHT;

        final int glowAlpha = (int) (15 + pulse * 10);
        g2.setColor(new Color(0, 255, 220, glowAlpha));
        g2.fillRoundRect(barX - 8, barY - 8, barWidth + 16, BAR_HEIGHT + 16, arcSize + 16, arcSize + 16);

        g2.setColor(BORDER_GLOW_COLOR);
        g2.setStroke(BAR_STROKE);
        g2.drawRoundRect(barX, barY, barWidth, BAR_HEIGHT, arcSize, arcSize);

        final int fillWidth = (int) (barWidth * progress);
        if (fillWidth > 6) {
            drawProgressFill(g2, barX, barY, fillWidth, pulse, barWidth);
        }

        drawPercentLabel(g2, barX, barY, barWidth);
    }

    private void drawProgressFill(Graphics2D g2, int x, int y, int width, float pulse, int maxBarWidth) {
        final int arcSize = BAR_HEIGHT;
        g2.setColor(FILL_GLOW_COLOR);
        g2.fillRoundRect(x + 2, y - 2, width - 4, BAR_HEIGHT + 4, arcSize, arcSize);

        final GradientPaint fillGrad = new GradientPaint(x, y, RenderUtils.cyan, x + width, y, RenderUtils.purple);
        g2.setPaint(fillGrad);
        g2.fillRoundRect(x + 2, y + 2, width - 4, BAR_HEIGHT - 4, arcSize - 4, arcSize - 4);

        final GradientPaint shine = new GradientPaint(x, y + 2, new Color(255, 255, 255, 120), x, y + BAR_HEIGHT / 2f, new Color(255, 255, 255, 0));
        g2.setPaint(shine);
        g2.fillRoundRect(x + 2, y + 2, width - 4, (BAR_HEIGHT - 4) / 2, arcSize - 4, arcSize - 4);

        if (width < maxBarWidth - 6) {
            g2.setColor(new Color(255, 255, 255, (int) (100 + 80 * pulse)));
            g2.fillOval(x + width - 6, y + (BAR_HEIGHT >> 1) - 6, 12, 12);
        }
    }

    private void drawPercentLabel(Graphics2D g2, int barX, int barY, int barWidth) {
        g2.setFont(PERCENT_FONT);
        final String percentText = (int) (progress * 100) + "%";
        final FontMetrics fm = g2.getFontMetrics();
        final int pctX = barX + (barWidth >> 1) - (fm.stringWidth(percentText) >> 1);
        final int pctY = barY + BAR_HEIGHT + 30;

        g2.setColor(PERCENT_SHADOW_COLOR);
        g2.drawString(percentText, pctX - 1, pctY);
        g2.drawString(percentText, pctX + 1, pctY);

        g2.setColor(PERCENT_TEXT_COLOR);
        g2.drawString(percentText, pctX, pctY);
    }

    private void drawFooter(Graphics2D g2, int w, int h) {
        g2.setFont(FOOTER_FONT);
        g2.setColor(FOOTER_COLOR);
        final String hint = "Press ESC to cancel";
        final FontMetrics hintFm = g2.getFontMetrics();
        g2.drawString(hint, (w - hintFm.stringWidth(hint)) >> 1, h - 40);
    }
}