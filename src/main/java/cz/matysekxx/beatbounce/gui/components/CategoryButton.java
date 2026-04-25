package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import javax.swing.*;
import java.awt.*;

public class CategoryButton extends JButton {
    public CategoryButton(String text) {
        super(text);
        setFont(new Font("SansSerif", Font.BOLD, 12));
        setForeground(Color.WHITE);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setPreferredSize(new Dimension(100, 30));
    }
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphic2D(g2);
        g2.setColor(new Color(255, 255, 255, 20));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        super.paintComponent(g);
        g2.dispose();
    }
}