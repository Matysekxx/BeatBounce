package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SearchField extends JTextField {
    private final String placeholder;

    public SearchField(String placeholder) {
        super(20);
        this.placeholder = placeholder;
        setPreferredSize(new Dimension(600, 50));
        setMaximumSize(new Dimension(600, 50));
        setFont(new Font("SansSerif", Font.PLAIN, 18));
        setForeground(Color.WHITE);
        setCaretColor(Color.WHITE);
        setOpaque(false);
        setBorder(new EmptyBorder(5, 20, 5, 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphic2D(g2);
        g2.setColor(new Color(255, 255, 255, 15));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

        g2.setColor(isFocusOwner() ? new Color(255, 255, 255, 50) : new Color(255, 255, 255, 20));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());

        super.paintComponent(g);

        if (getText().isEmpty() && !isFocusOwner()) {
            g2.setColor(new Color(255, 255, 255, 100));
            FontMetrics fm = g.getFontMetrics();
            g2.drawString(placeholder, getInsets().left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
        }
        g2.dispose();
    }
}