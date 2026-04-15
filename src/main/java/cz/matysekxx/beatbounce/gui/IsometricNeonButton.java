package cz.matysekxx.beatbounce.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IsometricNeonButton extends JButton {
    private final Color frontSide;
    private final Color topFill;
    private final Color topGlow;
    private final int depth;
    private final int arc;
    private int currentPressOffset = 0;

    public IsometricNeonButton(String text, Color frontSide, Color topFill, Color topGlow, int depth, int arc, Dimension size) {
        super(text);
        this.frontSide = frontSide;
        this.topFill = topFill;
        this.topGlow = topGlow;
        this.depth = depth;
        this.arc = arc;

        this.setForeground(Color.WHITE);
        this.setFont(new Font("Monospaced", Font.BOLD, 22));
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
                setBackground(topFill.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(topFill);
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

    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final int w = getWidth();
        final int h = getHeight();
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
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}