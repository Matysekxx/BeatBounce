package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SearchField extends JTextField {
    private final String placeholder;
    private boolean isHovered = false;

    public SearchField(String placeholder) {
        super(20);
        this.placeholder = placeholder;
        setPreferredSize(new Dimension(600, 50));
        setMaximumSize(new Dimension(600, 50));
        setFont(new Font("SansSerif", Font.PLAIN, 18));
        setForeground(Color.WHITE);
        setCaretColor(Color.WHITE);
        setOpaque(false);
        setBorder(new EmptyBorder(5, 20, 5, 50));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphic2D(g2);

        if (isFocusOwner()) {
            g2.setColor(new Color(255, 255, 255, 30));
        } else if (isHovered) {
            g2.setColor(new Color(255, 255, 255, 20));
        } else {
            g2.setColor(new Color(255, 255, 255, 10));
        }
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

        g2.setColor(isFocusOwner() ? new Color(255, 255, 255, 80) : new Color(255, 255, 255, 30));
        g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1.0f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());

        super.paintComponent(g);

        if (getText().isEmpty() && !isFocusOwner()) {
            g2.setColor(new Color(255, 255, 255, 100));
            FontMetrics fm = g.getFontMetrics();
            g2.drawString(placeholder, getInsets().left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
        }
        drawSearchIcon(g2);

        g2.dispose();
    }

    private void drawSearchIcon(Graphics2D g2) {
        int iconSize = 16;
        int x = getWidth() - 35;
        int y = (getHeight() - iconSize) / 2;

        g2.setColor(isFocusOwner() ? new Color(255, 255, 255, 150) : new Color(255, 255, 255, 80));
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(x, y, iconSize - 4, iconSize - 4);
        g2.drawLine(x + iconSize - 5, y + iconSize - 5, x + iconSize, y + iconSize);
    }
}
