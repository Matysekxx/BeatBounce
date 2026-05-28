package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.achievement.Achievement;
import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Renders a decorative icon for an achievement based on its type.
 * Each type (High Score, Unique Songs, etc.) has its own vector-like procedural drawing.
 * Icons are desaturated for incomplete achievements.
 */
public class AchievementIconPanel extends JPanel {
    /**
     * The achievement whose type determines the icon.
     */
    private final Achievement achievement;

    /**
     * Constructs an icon panel.
     *
     * @param achievement the achievement to represent
     */
    public AchievementIconPanel(Achievement achievement) {
        this.achievement = achievement;
        this.setOpaque(false);
    }

    /**
     * Returns the preferred size of this panel, accounting for UI scaling.
     *
     * @return the preferred dimension
     */
    @Override
    public Dimension getPreferredSize() {
        final int boxSize = UIScale.scale(60);
        return new Dimension(boxSize, boxSize);
    }

    /**
     * Returns the minimum size of this panel, which is same as the preferred size.
     *
     * @return the minimum dimension
     */
    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /**
     * Returns the maximum size of this panel, which is same as the preferred size.
     *
     * @return the maximum dimension
     */
    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    /**
     * Dispatches the painting to a specific drawing method based on achievement type.
     *
     * @param g the Graphics context
     */
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

    /**
     * Draws a star icon representing a high score achievement.
     *
     * @param g2      the Graphics2D context
     * @param centerX the horizontal center of the icon
     * @param centerY the vertical center of the icon
     * @param alpha   the transparency level (lower for incomplete achievements)
     */
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

    /**
     * Draws a music note icon representing a unique songs achievement.
     *
     * @param g2      the Graphics2D context
     * @param centerX the horizontal center of the icon
     * @param centerY the vertical center of the icon
     * @param alpha   the transparency level
     */
    private void drawMusicNote(Graphics2D g2, int centerX, int centerY, float alpha) {
        final int head1X = centerX - UIScale.scale(10);
        final int head1Y = centerY + UIScale.scale(6);
        final int head2X = centerX + UIScale.scale(4);
        final int head2Y = centerY + UIScale.scale(2);
        final int noteW = UIScale.scale(9);
        final int noteW_H = UIScale.scale(7);

        g2.setStroke(RenderCache.STROKE_2);

        drawNoteHead(g2, head1X, head1Y, noteW, noteW_H, alpha);
        drawNoteHead(g2, head2X, head2Y, noteW, noteW_H, alpha);

        final int stem1X = head1X + noteW - UIScale.scale(1);
        final int stem2X = head2X + noteW - UIScale.scale(1);
        final int stemTopY = centerY - UIScale.scale(12);

        g2.setColor(new Color(255, 255, 255, (int) (200 * alpha)));
        g2.drawLine(stem1X, head1Y + noteW_H / 2, stem1X, stemTopY);
        g2.drawLine(stem2X, head2Y + noteW_H / 2, stem2X, stemTopY - UIScale.scale(4));

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

    /**
     * Draws a single note head for a music note icon.
     *
     * @param g2    the Graphics2D context
     * @param x     the x position
     * @param y     the y position
     * @param w     the width
     * @param h     the height
     * @param alpha the transparency level
     */
    private void drawNoteHead(Graphics2D g2, int x, int y, int w, int h, float alpha) {
        g2.setPaint(new GradientPaint(x, y, new Color(190, 0, 255, (int) (25 * alpha)), x + w, y + h, new Color(0, 255, 255, (int) (25 * alpha))));
        g2.fillOval(x, y, w, h);
        g2.setColor(new Color(255, 255, 255, (int) (200 * alpha)));
        g2.drawOval(x, y, w, h);
    }

    /**
     * Draws an orb icon representing an orbs held achievement.
     *
     * @param g2      the Graphics2D context
     * @param centerX the horizontal center of the icon
     * @param centerY the vertical center of the icon
     * @param alpha   the transparency level
     */
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

    /**
     * Draws a default diamond icon for unknown achievement types.
     *
     * @param g2      the Graphics2D context
     * @param centerX the horizontal center of the icon
     * @param centerY the vertical center of the icon
     * @param alpha   the transparency level
     */
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
