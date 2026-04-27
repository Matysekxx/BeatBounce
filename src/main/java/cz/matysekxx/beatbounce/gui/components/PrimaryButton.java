package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PrimaryButton extends JButton {
    public PrimaryButton(String text) {
        super(text);
        setFont(new Font("SansSerif", Font.BOLD, 12));
        setForeground(Color.BLACK);
        setPreferredSize(new Dimension(110, 32));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) setForeground(Color.WHITE);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()) setForeground(Color.BLACK);
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphic2D(g2);

        g2.setColor(!isEnabled() ? new Color(50, 50, 50) : RenderUtils.cyan.darker());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

        setForeground(!isEnabled() ? Color.GRAY : getForeground());
        g2.setColor(getForeground());

        final FontMetrics fm = g2.getFontMetrics(getFont());
        final int x = (getWidth() - fm.stringWidth(getText())) / 2;
        final int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), x, y);

        g2.dispose();
    }
}