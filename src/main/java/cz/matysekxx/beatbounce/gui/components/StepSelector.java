package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import java.awt.*;

/**
 * A custom UI component that allows selecting a value from a discrete set of steps.
 * It is used for settings like Target FPS.
 */
public class StepSelector extends javax.swing.JComponent {
    private final int[] values;
    private int selectedIndex;

    /**
     * Constructs a new StepSelector with the given values and an initial selection.
     *
     * @param values       the available values to select from
     * @param currentValue the value that should be initially selected
     */
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

    /**
     * Sets the selected index based on the provided value.
     * If the value is not found, the first index is selected.
     *
     * @param val the value to select
     */
    public void setSelectedIndexByValue(int val) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == val) {
                selectedIndex = i;
                return;
            }
        }
        selectedIndex = 0;
    }

    /**
     * Returns the currently selected value.
     *
     * @return the selected value
     */
    public int getSelectedValue() {
        return values[selectedIndex];
    }

    /**
     * Paints the component, including all steps and the current selection indicator.
     *
     * @param g the graphics context
     */
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
