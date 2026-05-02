package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IsometricButton extends JButton {
    private final Color frontSide;
    private final Color topFill;
    private final Color topGlow;
    private final int depth;
    private final int arc;
    private int currentPressOffset = 0;
    private float glowAlpha = 0f;
    private Timer glowTimer;

    public IsometricButton(String text, Color frontSide, Color topFill, Color topGlow, int depth, int arc, Dimension size) {
        super(text);
        this.frontSide = frontSide;
        this.topFill = topFill;
        this.topGlow = topGlow;
        this.depth = depth;
        this.arc = arc;
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.setForeground(Color.WHITE);
        this.setFont(new Font("Monospaced", Font.BOLD | Font.ITALIC, 24));
        this.setPreferredSize(size);
        this.setContentAreaFilled(false);
        this.setBorderPainted(false);
        this.setFocusPainted(false);
        this.setBackground(topFill);
        setAnimations();
    }

    private void setAnimations() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                startGlowAnimation(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                startGlowAnimation(false);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                currentPressOffset = 4;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                currentPressOffset = 0;
                repaint();
            }
        });
    }

    private void startGlowAnimation(boolean fadeIn) {
        if (glowTimer != null && glowTimer.isRunning()) glowTimer.stop();
        glowTimer = new Timer(16, _ -> {
            if (fadeIn) {
                glowAlpha += 0.1f;
                if (glowAlpha >= 1f) {
                    glowAlpha = 1f;
                    glowTimer.stop();
                }
            } else {
                glowAlpha -= 0.05f;
                if (glowAlpha <= 0f) {
                    glowAlpha = 0f;
                    glowTimer.stop();
                }
            }
            repaint();
        });
        glowTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2);
        final int w = getWidth();
        final int h = getHeight();

        if (glowAlpha > 0) {
            final float radius = w * 0.6f;
            final RadialGradientPaint glowPaint = new RadialGradientPaint(
                    w / 2f, h / 2f, radius,
                    new float[]{0f, 1f},
                    new Color[]{
                            new Color(topFill.getRed(), topFill.getGreen(), topFill.getBlue(), (int) (120 * glowAlpha)),
                            new Color(topFill.getRed(), topFill.getGreen(), topFill.getBlue(), 0)
                    }
            );
            g2.setPaint(glowPaint);
            g2.fillRect(0, 0, w, h);
        }

        g2.setColor(frontSide);
        g2.fillRoundRect(0, depth, w, h - depth, arc, arc);

        g2.setColor(getBackground());
        g2.fillRoundRect(0, currentPressOffset, w, h - depth, arc, arc);

        g2.setColor(topGlow);
        g2.setStroke(new BasicStroke(3.5f));
        g2.drawRoundRect(2, 2 + currentPressOffset, w - 4, h - depth - 4, arc, arc);

        g2.setFont(getFont());
        g2.setColor(getForeground());
        final FontMetrics fm = g2.getFontMetrics();
        final int textX = (w - fm.stringWidth(getText())) >> 1;
        final int textY = (((h - depth) + fm.getAscent() - fm.getDescent()) >> 1) + currentPressOffset;
        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawString(getText(), textX + 2, textY + 2);
        g2.setColor(getForeground());
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}