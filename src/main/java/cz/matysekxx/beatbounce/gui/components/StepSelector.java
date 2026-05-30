package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;

import cz.matysekxx.beatbounce.model.audio.AudioManager;
import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A custom UI component that allows selecting a value from a discrete set of steps.
 * It is used for settings like Target FPS.
 *
 * @author Matysekxx
 */
public class StepSelector extends JComponent {
    /**
     * The array of discrete integer values that can be selected.
     */
    private final int[] values;

    /**
     * The index of the currently selected value in the array.
     */
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
        final int w = UIScale.scale(450);
        final int h = UIScale.scale(60);
        setMinimumSize(new Dimension(w, h));
        setPreferredSize(new Dimension(w, h));
        setMaximumSize(new Dimension(w, h));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                AudioManager.playSFX("/click-sound.mp3");
                final int w = getWidth() / values.length;
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
        final int w = getWidth() / values.length;
        final int margin = UIScale.scale(4);

        for (int i = 0; i < values.length; i++) {
            final int x = i * w + margin;
            final int itemW = w - margin * 2;

            g2.setColor(i == selectedIndex ? RenderUtils.cyan : new Color(40, 40, 45));
            g2.fillRoundRect(x, UIScale.scale(4), itemW, UIScale.scale(30), UIScale.scale(12), UIScale.scale(12));

            if (i == selectedIndex) {
                g2.setColor(new Color(0, 255, 255, 40));
                g2.fillRoundRect(x - UIScale.scale(2), UIScale.scale(2), itemW + UIScale.scale(4), UIScale.scale(34), UIScale.scale(14), UIScale.scale(14));
            }

            g2.setColor(i == selectedIndex ? Color.WHITE : new Color(255, 255, 255, 150));
            g2.setFont(i == selectedIndex ? UIScale.scaleFont(RenderCache.SANS_BOLD_18) : UIScale.scaleFont(RenderCache.SANS_PLAIN_18));
            final String s = String.valueOf(values[i]);
            final FontMetrics fm = g2.getFontMetrics();
            g2.drawString(s, x + (itemW - fm.stringWidth(s)) / 2, UIScale.scale(25));
        }
        g2.dispose();
    }
}