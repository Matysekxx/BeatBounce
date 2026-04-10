package cz.matysekxx.beatbounce.core.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ButtonFactory {
    private static final Color MAGENTA_TOP_GLOW = new Color(255, 30, 240);
    private static final Color MAGENTA_TOP_FILL = new Color(230, 0, 200);

    private static final Color MAGENTA_FRONT_SIDE = new Color(100, 0, 90);

    private static final Color CYAN_TOP_GLOW = new Color(0, 255, 255);
    private static final Color CYAN_TOP_FILL = new Color(0, 200, 220);
    private static final Color CYAN_FRONT_SIDE = new Color(0, 70, 90);
    private static final Color WHITE = new Color(255, 255, 255);

    public static JButton createStartButton(ActionListener actionListener) {
        return createButton("START", MAGENTA_TOP_FILL, MAGENTA_TOP_GLOW, MAGENTA_FRONT_SIDE, actionListener);
    }

    public static JButton createExitButton(ActionListener actionListener) {
        return createButton("EXIT", CYAN_TOP_FILL, CYAN_TOP_GLOW, CYAN_FRONT_SIDE, actionListener);
    }

    private static JButton createButton(String text, Color topFill, Color topGlow, Color frontSide, ActionListener listener) {
        final JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                final Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                final int w = getWidth();
                final int h = getHeight();

                final int depth = 6;
                final int arc = 40;
                g2.setColor(frontSide);
                g2.fillRoundRect(0, depth, w, h - depth, arc, arc);

                g2.setColor(topFill);
                g2.fillRoundRect(0, 0, w, h - depth, arc, arc);
                g2.setColor(topGlow);
                g2.setStroke(new BasicStroke(3.5f));
                g2.drawRoundRect(2, 2, w - 4, h - depth - 4, arc, arc);

                g2.setFont(getFont());
                g2.setColor(getForeground());
                final FontMetrics fm = g2.getFontMetrics();
                final int textX = (w - fm.stringWidth(getText())) >> 1;
                final int textY = ((h - depth) + fm.getAscent() - fm.getDescent()) >> 1;
                g2.drawString(getText(), textX, textY);

                g2.dispose();
            }
        };

        button.addActionListener(listener);
        button.setForeground(WHITE);
        button.setFont(new Font("Monospaced", Font.BOLD, 22));
        button.setPreferredSize(new Dimension(220, 80));

        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        return button;
    }
}