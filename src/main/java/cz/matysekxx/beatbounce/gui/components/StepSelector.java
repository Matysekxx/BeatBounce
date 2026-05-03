package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import java.awt.*;

public class StepSelector extends javax.swing.JComponent {
    private final int[] values;
    private int selectedIndex;

    public StepSelector(int[] values, int currentValue) {
        this.values = values;
        setSelectedIndexByValue(currentValue);
        setPreferredSize(new Dimension(420, 60));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int w = getWidth() / values.length;
                selectedIndex = e.getX() / w;
                if (selectedIndex < 0) selectedIndex = 0;
                if (selectedIndex >= values.length) selectedIndex = values.length - 1;
                repaint();
            }
        });
    }

    public void setSelectedIndexByValue(int val) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == val) {
                selectedIndex = i;
                return;
            }
        }
        selectedIndex = 0;
    }

    public int getSelectedValue() {
        return values[selectedIndex];
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2);
        int w = getWidth() / values.length;
        int margin = 4;

        for (int i = 0; i < values.length; i++) {
            int x = i * w + margin;
            int itemW = w - margin * 2;

            g2.setColor(i == selectedIndex ? RenderUtils.cyan : new Color(40, 40, 45));
            g2.fillRoundRect(x, 4, itemW, 24, 12, 12);

            if (i == selectedIndex) {
                g2.setColor(new Color(0, 255, 255, 40));
                g2.fillRoundRect(x - 2, 2, itemW + 4, 28, 14, 14);
            }

            g2.setColor(i == selectedIndex ? Color.WHITE : Color.GRAY);
            g2.setFont(new Font("SansSerif", i == selectedIndex ? Font.BOLD : Font.PLAIN, 14));
            String s = String.valueOf(values[i]);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(s, x + (itemW - fm.stringWidth(s)) / 2, 52);
        }
        g2.dispose();
    }
}
