package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.GameModel;
import cz.matysekxx.beatbounce.model.ScoreManager;

import javax.sound.sampled.Clip;
import java.awt.*;

public class GameUIRenderer {
    private final GameModel gameModel;
    private final Clip clip;

    public GameUIRenderer(GameModel gameModel, Clip clip) {
        this.gameModel = gameModel;
        this.clip = clip;
    }

    public void drawCountdown(Graphics2D g2d, int width, int height) {
        final int count = (int) Math.ceil(gameModel.getCountdownTime());
        final String text = String.valueOf(count);
        final Color color = RenderUtils.cyan;

        final double countFrac = gameModel.getCountdownTime() % 1.0;
        final int ringR = (int) (countFrac * 230);
        final int ringAlpha = (int) (220 * (1.0 - countFrac));

        if (ringR > 2 && ringAlpha > 0) {
            drawRing(g2d, width / 2, height / 2, ringR, color, ringAlpha, 4f);
            drawRing(g2d, width / 2, height / 2, Math.max(0, ringR - 20), color, (int) (ringAlpha * 0.6), 2f);
            drawRing(g2d, width / 2, height / 2, Math.max(0, ringR - 40), color, (int) (ringAlpha * 0.3), 1f);
            g2d.setStroke(new BasicStroke(1f));
        }

        g2d.setFont(new Font("Monospaced", Font.BOLD, 150));
        final FontMetrics fm = g2d.getFontMetrics();
        RenderUtils.drawText(g2d, text, (width - fm.stringWidth(text)) / 2, (height + fm.getAscent()) / 2, color);
    }

    private void drawRing(Graphics2D g2d, int cx, int cy, int r, Color color, int alpha, float stroke) {
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(255, alpha)));
        g2d.setStroke(new BasicStroke(stroke));
        g2d.drawOval(cx - r, cy - r, r * 2, r * 2);
    }

    private void drawGlassCard(Graphics2D g2d, int width, int height, int cardW, int cardH, Color accentColor, float pulse) {
        final int cardX = (width - cardW) / 2;
        final int cardY = (height - cardH) / 2;

        g2d.setPaint(new RadialGradientPaint(width / 2f, height / 2f, cardW * 0.7f, new float[]{0f, 1f}, new Color[]{new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), (int) (18 + 15 * pulse)), new Color(0, 0, 0, 0)}));
        g2d.fillRoundRect(cardX - 20, cardY - 20, cardW + 40, cardH + 40, 30, 30);

        g2d.setColor(new Color(6, 0, 18, 230));
        g2d.fillRoundRect(cardX, cardY, cardW, cardH, 18, 18);

        g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), (int) (110 + 100 * pulse)));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(cardX, cardY, cardW, cardH, 18, 18);

        g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), (int) (25 + 15 * pulse)));
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawRoundRect(cardX + 4, cardY + 4, cardW - 8, cardH - 8, 14, 14);
    }

    public void drawPauseScreen(Graphics2D g2d, int width, int height) {
        g2d.setColor(new Color(0, 0, 8, 170));
        g2d.fillRect(0, 0, width, height);

        final float pulse = (float) ((Math.sin(System.currentTimeMillis() / 700.0) + 1.0) / 2.0);
        final int cardW = 460;
        final int cardH = 260;
        final int cardY = (height - cardH) / 2;

        drawGlassCard(g2d, width, height, cardW, cardH, RenderUtils.cyan, pulse);

        g2d.setFont(new Font("Monospaced", Font.BOLD | Font.ITALIC, 78));
        final String title = "PAUSED";
        RenderUtils.drawText(g2d, title, (width - g2d.getFontMetrics().stringWidth(title)) / 2, cardY + 96, RenderUtils.cyan);

        g2d.setStroke(new BasicStroke(1f));
        g2d.setColor(new Color(0, 255, 220, 55));
        g2d.drawLine((width - cardW) / 2 + 40, cardY + 115, (width + cardW) / 2 - 40, cardY + 115);

        final int hintY = cardY + 195;
        drawKeyHint(g2d, "ESC", "Resume", width / 2 - 130, hintY);
        drawKeyHint(g2d, "ENTER", "Quit to Menu", width / 2 + 30, hintY);
    }

    public void drawFinishedScreen(Graphics2D g2d, int width, int height) {
        final float pulse = (float) ((Math.sin(System.currentTimeMillis() / 600.0) + 1.0) / 2.0);
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRect(0, 0, width, height);

        final int cardW = 600;
        final int cardH = 340;
        final int cardY = (height - cardH) / 2;

        drawGlassCard(g2d, width, height, cardW, cardH, RenderUtils.yellow, pulse);

        final String text = "LEVEL COMPLETE";
        g2d.setFont(new Font("Monospaced", Font.BOLD | Font.ITALIC, 65));
        RenderUtils.drawText(g2d, text, (width - g2d.getFontMetrics().stringWidth(text)) / 2, cardY + 100, RenderUtils.yellow);

        g2d.setColor(new Color(255, 215, 0, (int) (70 + 50 * pulse)));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine((width - cardW) / 2 + 40, cardY + 130, (width + cardW) / 2 - 40, cardY + 130);
        g2d.setStroke(new BasicStroke(1f));

        drawPostGameScore(g2d, cardY + 70, width, RenderUtils.yellow);

        final int hintY = cardY + 280;
        drawKeyHint(g2d, "R", "Restart", width / 2 - 130, hintY);
        drawKeyHint(g2d, "ENTER", "Continue", width / 2 + 30, hintY);
    }

    public void drawGameOverScreen(Graphics2D g2d, int width, int height) {
        final long t = System.currentTimeMillis();
        final float pulse = (float) ((Math.sin(t / 600.0) + 1.0) / 2.0);

        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(new Color(0, 0, 0, 20));
        for (int sy = 0; sy < height; sy += 4) {
            g2d.drawLine(0, sy, width, sy);
        }

        final int cardW = 540;
        final int cardH = 340;
        final int cardY = (height - cardH) / 2;

        drawGlassCard(g2d, width, height, cardW, cardH, new Color(255, 40, 40), pulse);

        final String text = "GAME OVER";
        g2d.setFont(new Font("Monospaced", Font.BOLD, 85));
        final int x = (width - g2d.getFontMetrics().stringWidth(text)) / 2;
        final int y = cardY + 110;

        final int glitchOffset = (int) (4 + Math.sin(t / 80.0) * 2);
        g2d.setColor(new Color(0, 255, 220, 50));
        g2d.drawString(text, x - glitchOffset, y + 1);
        g2d.setColor(new Color(255, 0, 80, 40));
        g2d.drawString(text, x + glitchOffset, y - 1);

        RenderUtils.drawText(g2d, text, x, y, new Color(255, 40, 40));

        g2d.setColor(new Color(255, 40, 40, (int) (70 + 50 * pulse)));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine((width - cardW) / 2 + 40, cardY + 140, (width + cardW) / 2 - 40, cardY + 140);
        g2d.setStroke(new BasicStroke(1f));

        drawPostGameScore(g2d, cardY + 80, width, new Color(255, 100, 100));

        final int hintY = cardY + 280;
        drawKeyHint(g2d, "R", "Restart", width / 2 - 130, hintY);
        drawKeyHint(g2d, "ENTER", "Main Menu", width / 2 + 30, hintY);
    }

    private void drawPostGameScore(Graphics2D g2d, int titleY, int width, Color accentColor) {
        final String label = "F I N A L   S C O R E";
        final String scoreText = String.format("%,d", gameModel.getScore());
        final int cardY = titleY + 55;

        g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2d.setColor(new Color(180, 180, 180, 180));
        g2d.drawString(label, (width - g2d.getFontMetrics().stringWidth(label)) / 2, cardY + 12);

        g2d.setFont(new Font("Monospaced", Font.BOLD | Font.ITALIC, 48));
        RenderUtils.drawText(g2d, scoreText, (width - g2d.getFontMetrics().stringWidth(scoreText)) / 2, cardY + 55, accentColor);

        final String orbsLabel = "ORBS COLLECTED: " + gameModel.getCollectedOrbs();
        g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2d.setColor(new Color(255, 200, 0));
        g2d.drawString(orbsLabel, (width - g2d.getFontMetrics().stringWidth(orbsLabel)) / 2, cardY + 85);
        
        final String totalOrbsLabel = "TOTAL CURRENCY: " + ScoreManager.getCurrency();
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString(totalOrbsLabel, (width - g2d.getFontMetrics().stringWidth(totalOrbsLabel)) / 2, cardY + 105);
    }

    public void drawScore(Graphics2D g2d, int width, float scorePopAlpha) {
        final double pulse = (Math.sin(System.currentTimeMillis() / 550.0) + 1.0) / 2.0;
        final Integer score = gameModel.getScore();
        final Color c = switch (score) {
            case Integer i when i < 500 -> RenderUtils.cyan;
            case Integer i when i < 750 -> RenderUtils.green;
            case Integer i when i < 1000 -> RenderUtils.blue;
            case Integer i when i < 1500 -> RenderUtils.purple;
            default -> RenderUtils.yellow;
        };

        final int haloR = (int) (70 + pulse * 18);
        g2d.setPaint(new RadialGradientPaint(width / 2f, 68, haloR, new float[]{0f, 1f}, new Color[]{new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (50 + 30 * pulse)), new Color(0, 0, 0, 0)}));
        g2d.fillOval(width / 2 - haloR, 68 - haloR, haloR * 2, haloR * 2);

        if (scorePopAlpha > 0) {
            final int popR = (int) (90 + (1f - scorePopAlpha) * 60);
            drawRing(g2d, width / 2, 68, popR, c, (int) (scorePopAlpha * 80), 2f);
            g2d.setStroke(new BasicStroke(1f));
        }

        final String label = "S  C  O  R  E";
        g2d.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2d.setColor(new Color(255, 255, 255, 160));
        g2d.drawString(label, (width - g2d.getFontMetrics().stringWidth(label)) / 2, 30);

        g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (100 + 80 * pulse)));
        g2d.drawLine(width / 2 - 35, 35, width / 2 + 35, 35);

        final String text = Integer.toString(score);
        g2d.setFont(new Font("Monospaced", Font.BOLD | Font.ITALIC, 60));
        RenderUtils.drawText(g2d, text, (width - g2d.getFontMetrics().stringWidth(text)) / 2, 88, c);

        final String orbsText = "Orbs: " + gameModel.getCollectedOrbs();
        g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2d.setColor(new Color(255, 200, 0, 200));
        g2d.drawString(orbsText, 20, 40);
    }

    public void drawProgressBar(Graphics2D g2d, int width, int height) {
        if (clip == null) return;
        final double current = clip.getMicrosecondPosition() / 1_000_000.0;
        final double total = clip.getMicrosecondLength() / 1_000_000.0;
        final double progress = Math.min(1.0, current / total);

        final int barY = height - 16;
        g2d.setColor(new Color(255, 255, 255, 22));
        g2d.fillRoundRect(0, barY, width, 15, 3, 3);

        final int fillW = (int) (width * progress);
        if (fillW > 3) {
            g2d.setPaint(new LinearGradientPaint(0, barY, width, barY, new float[]{0f, 0.5f, 1f}, new Color[]{RenderUtils.cyan, RenderUtils.purple, RenderUtils.yellow}));
            g2d.fillRoundRect(0, barY, fillW, 15, 3, 3);
            if (fillW < width) {
                g2d.setPaint(new RadialGradientPaint(fillW, barY + 7.5f, 14, new float[]{0f, 1f}, new Color[]{new Color(255, 255, 255, 200), new Color(255, 255, 255, 0)}));
                g2d.fillOval(fillW - 14, barY - 9, 28, 33);
            }
        }

        g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.drawString(String.format("%d:%02d  /  %d:%02d", (int) current / 60, (int) current % 60, (int) total / 60, (int) total % 60), 10, barY - 7);
    }

    private void drawKeyHint(Graphics2D g2d, String key, String label, int x, int y) {
        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        final int keyW = g2d.getFontMetrics().stringWidth(key);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        final int totalW = keyW + g2d.getFontMetrics().stringWidth(label) + 32;

        g2d.setColor(new Color(255, 255, 255, 15));
        g2d.fillRoundRect(x, y - 23, totalW, 28, 28, 28);
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.drawRoundRect(x, y - 23, totalW, 28, 28, 28);
        g2d.setColor(new Color(0, 255, 220, 20));
        g2d.fillRoundRect(x + 4, y - 19, keyW + 12, 20, 20, 20);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2d.setColor(RenderUtils.cyan);
        g2d.drawString(key, x + 10, y - 4);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2d.setColor(new Color(220, 220, 220));
        g2d.drawString(label, x + keyW + 22, y - 4);
    }
}