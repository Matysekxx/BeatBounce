package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.util.Time;

import javax.swing.*;
import java.awt.*;

public class LoadingPanel extends JPanel implements Runnable {
    private float time = 0.f;
    private float progress = 0.f;
    private Thread animationThread;
    private boolean running = false;
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
            } catch (InterruptedException ignored) {}
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
            float remaining = 1.0f - progress;
            float step = remaining * dt * 0.6f + (float) (Math.random() * 0.001f);
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
        int dotsCount = (int) (time * 2.5f) % 4;
        String drawText = text;
        if (text != null && text.contains("...")) {
            drawText = text.substring(0, text.indexOf("...")) + ".".repeat(dotsCount);
        }

        g2.setFont(new Font("Monospaced", Font.BOLD | Font.ITALIC, 72));
        FontMetrics fm = g2.getFontMetrics();
        int x = (w - fm.stringWidth(drawText)) / 2;
        int y = h / 3;

        for (int i = 4; i >= 1; i--) {
            float alpha = 0.04f + pulse * 0.03f;
            g2.setColor(new Color(0, 255, 220, (int) (alpha * 255 / i)));
            g2.drawString(drawText, x - i, y);
            g2.drawString(drawText, x + i, y);
        }

        g2.setColor(new Color(0, 255, 220));
        g2.drawString(drawText, x, y);
        g2.setColor(Color.WHITE);
        g2.drawString(drawText, x, y - 1);
    }

    private void drawProgressBar(Graphics2D g2, int w, int h, float pulse) {
        final int barWidth = Math.min(600, (int) (w * 0.45));
        final int barHeight = 20;
        final int barX = (w - barWidth) / 2;
        final int barY = (int) (h * 0.62);

        int glowAlpha = (int) (15 + pulse * 10);
        g2.setColor(new Color(0, 255, 220, glowAlpha));
        g2.fillRoundRect(barX - 8, barY - 8, barWidth + 16, barHeight + 16, barHeight + 16, barHeight + 16);

        g2.setColor(new Color(0, 255, 220, 60));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(barX, barY, barWidth, barHeight, barHeight, barHeight);

        final int fillWidth = (int) (barWidth * progress);
        if (fillWidth > 6) {
            drawProgressFill(g2, barX, barY, fillWidth, barHeight, pulse);
        }

        drawPercentLabel(g2, barX, barY, barWidth, barHeight);
    }

    private void drawProgressFill(Graphics2D g2, int x, int y, int width, int height, float pulse) {
        g2.setColor(new Color(RenderUtils.cyan.getRed(), RenderUtils.cyan.getGreen(), RenderUtils.cyan.getBlue(), 40));
        g2.fillRoundRect(x + 2, y - 2, width - 4, height + 4, height, height);

        GradientPaint fillGrad = new GradientPaint(x, y, RenderUtils.cyan, x + width, y, RenderUtils.purple);
        g2.setPaint(fillGrad);
        g2.fillRoundRect(x + 2, y + 2, width - 4, height - 4, height - 4, height - 4);

        GradientPaint shine = new GradientPaint(x, y + 2, new Color(255, 255, 255, 120), x, y + height / 2f, new Color(255, 255, 255, 0));
        g2.setPaint(shine);
        g2.fillRoundRect(x + 2, y + 2, width - 4, (height - 4) / 2, height - 4, height - 4);

        if (width < (int)(getWidth() * 0.45)) {
            g2.setColor(new Color(255, 255, 255, (int) (100 + 80 * pulse)));
            g2.fillOval(x + width - 6, y + height / 2 - 6, 12, 12);
        }
    }

    private void drawPercentLabel(Graphics2D g2, int barX, int barY, int barWidth, int barHeight) {
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        final String percentText = (int) (progress * 100) + "%";
        final FontMetrics fm = g2.getFontMetrics();
        int pctX = barX + barWidth / 2 - fm.stringWidth(percentText) / 2;
        int pctY = barY + barHeight + 30;

        g2.setColor(new Color(0, 255, 220, 30));
        g2.drawString(percentText, pctX - 1, pctY);
        g2.drawString(percentText, pctX + 1, pctY);

        g2.setColor(new Color(255, 255, 255, 200));
        g2.drawString(percentText, pctX, pctY);
    }

    private void drawFooter(Graphics2D g2, int w, int h) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(180, 180, 200, 120));
        String hint = "Press ESC to cancel";
        FontMetrics hintFm = g2.getFontMetrics();
        g2.drawString(hint, (w - hintFm.stringWidth(hint)) / 2, h - 40);
    }
}