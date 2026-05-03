package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import javax.swing.*;
import java.awt.*;

public class CustomCheckBox extends JCheckBox {
    public CustomCheckBox(String text, boolean selected) {
        super(text, selected);
        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.LIGHT_GRAY);
        setFont(new Font("SansSerif", Font.PLAIN, 17));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addChangeListener(_ -> setForeground(isSelected() ? Color.WHITE : Color.LIGHT_GRAY));
        setIcon(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphics2D(g2);
                g2.setColor(isSelected() ? RenderUtils.cyan : new Color(60, 60, 65));
                g2.fillRoundRect(x, y + 2, 40, 20, 20, 20);
                g2.setColor(Color.WHITE);
                final int knobX = isSelected() ? x + 22 : x + 2;
                g2.fillOval(knobX, y + 4, 16, 16);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 55;
            }

            @Override
            public int getIconHeight() {
                return 26;
            }
        });
    }
}
