package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.achievement.Achievement;
import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.GradientPaint;
import java.awt.geom.Path2D;

public class AchievementIconPanel extends JPanel {
    private final Achievement achievement;

    public AchievementIconPanel(Achievement achievement) {
        this.achievement = achievement;
        this.setOpaque(false);
        final int boxSize = UIScale.scale(60);
        this.setPreferredSize(new Dimension(boxSize, boxSize));
        this.setMinimumSize(new Dimension(boxSize, boxSize));
        this.setMaximumSize(new Dimension(boxSize, boxSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2);

        final int w = getWidth();
        final int h = getHeight();
        final int centerX = w / 2;
        final int centerY = h / 2;
        final float alpha = achievement.isCompleted() ? 1.0f : 0.35f;

        g2.setColor(new Color(255, 255, 255, 25));
        g2.fillRoundRect(0, 0, w, h, UIScale.scale(12), UIScale.scale(12));

        switch (achievement.getType()) {
            case HIGH_SCORE -> drawStar(g2, centerX, centerY, alpha);
            case UNIQUE_SONGS -> drawMusicNote(g2, centerX, centerY, alpha);
            case ORBS_HELD -> drawOrb(g2, centerX, centerY, alpha);
            default -> drawDefault(g2, centerX, centerY, alpha);
        }

        g2.dispose();
    }

    private void drawStar(Graphics2D g2, int centerX, int centerY, float alpha) {
        final int points = 5;
        final int outerRadius = UIScale.scale(14);
        final int innerRadius = UIScale.scale(6);
        final Path2D.Double star = new Path2D.Double();
        final double angleStep = Math.PI / points;
        for (int j = 0; j < 2 * points; j++) {
            final double r = (j % 2 == 0) ? outerRadius : innerRadius;
            final double angle = j * angleStep - Math.PI / 2;
            final double x = centerX + Math.cos(angle) * r;
            final double y = centerY + Math.sin(angle) * r;
            if (j == 0) star.moveTo(x, y);
            else star.lineTo(x, y);
        }
        star.closePath();

        final float[] dist = {0.0f, 1.0f};
        final Color[] colors = {
                new Color(255, 245, 170, (int) (255 * alpha)),
                new Color(255, 170, 0, (int) (255 * alpha))
        };
        g2.setPaint(new LinearGradientPaint(centerX - outerRadius, centerY - outerRadius, centerX + outerRadius, centerY + outerRadius, dist, colors));
        g2.fill(star);

        g2.setColor(new Color(255, 255, 255, (int) (220 * alpha)));
        g2.setStroke(RenderCache.STROKE_1_5);
        g2.draw(star);
    }

    private void drawMusicNote(Graphics2D g2, int centerX, int centerY, float alpha) {
        final int head1X = centerX - UIScale.scale(10);
        final int head1Y = centerY + UIScale.scale(6);
        final int head2X = centerX + UIScale.scale(4);
        final int head2Y = centerY + UIScale.scale(2);
        final int noteW = UIScale.scale(9);
        final int noteH = UIScale.scale(7);

        g2.setStroke(RenderCache.STROKE_2);

        drawNoteHead(g2, head1X, head1Y, noteW, noteH, alpha);
        drawNoteHead(g2, head2X, head2Y, noteW, noteH, alpha);

        final int stem1X = head1X + noteW - UIScale.scale(1);
        final int stem2X = head2X + noteW - UIScale.scale(1);
        final int stemTopY = centerY - UIScale.scale(12);

        g2.setColor(new Color(255, 255, 255, (int) (200 * alpha)));
        g2.drawLine(stem1X, head1Y + noteH / 2, stem1X, stemTopY);
        g2.drawLine(stem2X, head2Y + noteH / 2, stem2X, stemTopY - UIScale.scale(4));

        Path2D.Double beam = new Path2D.Double();
        beam.moveTo(stem1X, stemTopY);
        beam.lineTo(stem2X, stemTopY - UIScale.scale(4));
        beam.lineTo(stem2X, stemTopY - UIScale.scale(4) + UIScale.scale(4));
        beam.lineTo(stem1X, stemTopY + UIScale.scale(4));
        beam.closePath();

        g2.setPaint(new GradientPaint(stem1X, stemTopY, new Color(190, 0, 255, (int) (25 * alpha)), stem2X, stemTopY - UIScale.scale(4), new Color(0, 255, 255, (int) (25 * alpha))));
        g2.fill(beam);
        g2.setColor(new Color(255, 255, 255, (int) (200 * alpha)));
        g2.draw(beam);
    }

    private void drawNoteHead(Graphics2D g2, int x, int y, int w, int h, float alpha) {
        g2.setPaint(new GradientPaint(x, y, new Color(190, 0, 255, (int) (25 * alpha)), x + w, y + h, new Color(0, 255, 255, (int) (25 * alpha))));
        g2.fillOval(x, y, w, h);
        g2.setColor(new Color(255, 255, 255, (int) (200 * alpha)));
        g2.drawOval(x, y, w, h);
    }

    private void drawOrb(Graphics2D g2, int centerX, int centerY, float alpha) {
        final int r = UIScale.scale(13);
        final int rInner = UIScale.scale(8);

        g2.setPaint(new RadialGradientPaint(centerX - UIScale.scale(3), centerY - UIScale.scale(3), r * 1.5f,
                new float[]{0f, 1f},
                new Color[]{
                        new Color(255, 205, 100, (int) (255 * alpha)),
                        new Color(255, 80, 0, (int) (255 * alpha))
                }));
        g2.fillOval(centerX - r, centerY - r, r * 2, r * 2);

        g2.setColor(new Color(255, 255, 255, (int) (220 * alpha)));
        g2.setStroke(RenderCache.STROKE_1_5);
        g2.drawOval(centerX - r, centerY - r, r * 2, r * 2);

        g2.setColor(new Color(255, 255, 255, (int) (140 * alpha)));
        g2.drawOval(centerX - rInner, centerY - rInner, rInner * 2, rInner * 2);

        g2.setColor(new Color(255, 255, 255, (int) (230 * alpha)));
        g2.fillOval(centerX - UIScale.scale(6), centerY - UIScale.scale(6), UIScale.scale(4), UIScale.scale(4));
    }

    private void drawDefault(Graphics2D g2, int centerX, int centerY, float alpha) {
        final Path2D.Double diamond = new Path2D.Double();
        final int size = UIScale.scale(13);
        diamond.moveTo(centerX, centerY - size);
        diamond.lineTo(centerX + size, centerY);
        diamond.lineTo(centerX, centerY + size);
        diamond.lineTo(centerX - size, centerY);
        diamond.closePath();

        g2.setPaint(new GradientPaint(centerX - size, centerY - size, new Color(0, 255, 200, (int) (255 * alpha)), centerX + size, centerY + size, new Color(0, 150, 255, (int) (255 * alpha))));
        g2.fill(diamond);

        g2.setColor(new Color(255, 255, 255, (int) (220 * alpha)));
        g2.setStroke(RenderCache.STROKE_1_5);
        g2.draw(diamond);
    }
}
