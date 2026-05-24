package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.achievement.Achievement;
import cz.matysekxx.beatbounce.model.achievement.AchievementType;
import cz.matysekxx.beatbounce.util.UIScale;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

public class AchievementIconPanel extends JPanel {
    private final Achievement achievement;

    public AchievementIconPanel(Achievement achievement) {
        this.achievement = achievement;
        setOpaque(false);
        final int iconSize = UIScale.scale(44);
        setPreferredSize(new Dimension(iconSize, iconSize));
        setMinimumSize(new Dimension(iconSize, iconSize));
        setMaximumSize(new Dimension(iconSize, iconSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2);

        final int w = getWidth();
        final int h = getHeight();
        final int centerX = w / 2;
        final int centerY = h / 2;
        final float alpha = achievement.isCompleted() ? 1.0f : 0.35f;

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
        Path2D.Double star = new Path2D.Double();
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
        g2.setColor(new Color(255, 215, 0, (int) (22 * alpha)));
        g2.fill(star);
        g2.setColor(new Color(210, 180, 100, (int) (255 * alpha)));
        g2.setStroke(RenderCache.STROKE_1_5);
        g2.draw(star);
    }

    private void drawMusicNote(Graphics2D g2, int centerX, int centerY, float alpha) {
        final int headX = centerX - UIScale.scale(6);
        final int headY = centerY + UIScale.scale(4);
        final int headW = UIScale.scale(10);
        final int headH = UIScale.scale(8);
        g2.setColor(new Color(191, 0, 255, (int) (25 * alpha)));
        g2.fillOval(headX, headY, headW, headH);
        g2.setColor(new Color(170, 130, 210, (int) (255 * alpha)));
        g2.setStroke(RenderCache.STROKE_1_5);
        g2.drawOval(headX, headY, headW, headH);
        final int stemX = headX + headW - UIScale.scale(1);
        g2.drawLine(stemX, headY + headH / 2, stemX, centerY - UIScale.scale(12));
        final Path2D.Double flag = getADouble(centerY, stemX);
        g2.fill(flag);
    }

    private static Path2D.Double getADouble(int centerY, int stemX) {
        final Path2D.Double flag = new Path2D.Double();
        flag.moveTo(stemX, centerY - UIScale.scale(12));
        flag.curveTo(stemX + UIScale.scale(6), centerY - UIScale.scale(9), stemX + UIScale.scale(7), centerY - UIScale.scale(3), stemX + UIScale.scale(6), centerY + UIScale.scale(1));
        flag.lineTo(stemX + UIScale.scale(6), centerY - UIScale.scale(3));
        flag.curveTo(stemX + UIScale.scale(5), centerY - UIScale.scale(7), stemX + UIScale.scale(2), centerY - UIScale.scale(8), stemX, centerY - UIScale.scale(8));
        flag.closePath();
        return flag;
    }

    private void drawOrb(Graphics2D g2, int centerX, int centerY, float alpha) {
        final int r = UIScale.scale(12);
        final int rInner = UIScale.scale(7);
        g2.setColor(new Color(255, 120, 0, (int) (22 * alpha)));
        g2.fillOval(centerX - r, centerY - r, r * 2, r * 2);
        g2.setColor(new Color(210, 140, 90, (int) (255 * alpha)));
        g2.setStroke(RenderCache.STROKE_1_5);
        g2.drawOval(centerX - r, centerY - r, r * 2, r * 2);
        g2.drawOval(centerX - rInner, centerY - rInner, rInner * 2, rInner * 2);
    }

    private void drawDefault(Graphics2D g2, int centerX, int centerY, float alpha) {
        Path2D.Double tri = new Path2D.Double();
        final int triW = UIScale.scale(8);
        final int triH = UIScale.scale(8);
        tri.moveTo(centerX - triW / 2. + UIScale.scale(2), centerY - triH);
        tri.lineTo(centerX + triW + UIScale.scale(2), centerY);
        tri.lineTo(centerX - triW / 2. + UIScale.scale(2), centerY + triH);
        tri.closePath();
        g2.setColor(new Color(0, 255, 220, (int) (25 * alpha)));
        g2.fill(tri);
        g2.setColor(new Color(100, 190, 190, (int) (255 * alpha)));
        g2.setStroke(RenderCache.STROKE_1_5);
        g2.draw(tri);
    }
}
