package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.util.Time;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class LoadingPanel extends JPanel implements Runnable {
    private float time = 0.f;
    private float progress = 0.f;
    private Thread animationThread;
    private boolean running = false;
    private String text = "Loading Level...";

    public LoadingPanel() {
    }

    public void setText(String text) {
        this.text = text;
    }

    public void startAnimation() {
        if (!running) {
            running = true;
            progress = 0f;
            animationThread = new Thread(this);
            animationThread.start();
        }
    }

    public void stopAnimation() {
        running = false;
        if (animationThread != null) {
            animationThread.interrupt();
            animationThread = null;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g.create();
        final int w = getWidth(), h = getHeight();
        RenderUtils.initGraphic2D(g2);
        RenderUtils.drawAuroraBackground(g2, w, h, time);

        String drawText = text;
        if (text != null && text.contains("...")) {
            int dots = (int) (time * 3) % 4;
            String base = text.substring(0, text.indexOf("..."));
            drawText = base + ".".repeat(dots);
        }

        RenderUtils.drawTitle(g2, w, h, drawText);

        final int barWidth = Math.min(800, (int) (w * 0.5));
        final int barHeight = 24;
        final int barX = (w - barWidth) / 2;
        final int barY = (int) (h * 0.65);

        g2.setColor(new Color(255, 255, 255, 40));
        g2.drawRoundRect(barX, barY, barWidth, barHeight, barHeight, barHeight);

        final int fillWidth = (int) (barWidth * progress);
        if (fillWidth > 4) {
            Shape fillShape = new RoundRectangle2D.Float(barX + 2, barY + 2, fillWidth - 4, barHeight - 4, barHeight - 4, barHeight - 4);

            g2.setPaint(new Color(RenderUtils.cyan.getRed(), RenderUtils.cyan.getGreen(), RenderUtils.cyan.getBlue(), 80));
            g2.fill(new RoundRectangle2D.Float(barX, barY - 4, fillWidth, barHeight + 8, barHeight + 8, barHeight + 8));

            GradientPaint gp = new GradientPaint(barX, barY, RenderUtils.cyan, barX + fillWidth, barY, RenderUtils.purple);
            g2.setPaint(gp);
            g2.fill(fillShape);
        }

        g2.setPaint(new GradientPaint(barX, barY, new Color(255, 255, 255, 100), barX, barY + barHeight / 2f, new Color(255, 255, 255, 0)));
        if (fillWidth > 4) {
            g2.fill(new RoundRectangle2D.Float(barX + 2, barY + 2, fillWidth - 4, barHeight / 2f, barHeight - 4, barHeight - 4));
        }

        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        final String percentText = (int) (progress * 100) + "%";
        final FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        g2.drawString(percentText, barX + barWidth / 2 - fm.stringWidth(percentText) / 2, barY + barHeight + 25);

        RenderUtils.applyNoiseOverlay(g2, 0, 0, w, h);

        g2.dispose();
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();
        while (running) {
            long now = System.currentTimeMillis();
            final float dt = (now - lastTime) / 1000f;
            lastTime = now;

            time += dt;
            if (progress < 0.99f) {
                final float remaining = 1.0f - progress;
                final float step = remaining * dt * 0.8f + (float) (Math.random() * 0.002f);
                progress += step;
                if (progress > 0.99f) progress = 0.99f;
            }

            repaint();
            Time.sleep(16);
        }
    }
}