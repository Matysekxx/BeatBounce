package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import javax.swing.*;
import java.awt.*;

/**
 * A custom {@link JButton} that cycles through a list of options when clicked.
 * Each click advances to the next option in the array.
 */
public class CycleButton extends JButton {

    /**
     * The array of available string options to cycle through.
     */
    public final String[] options;

    /**
     * The index of the currently selected option.
     */
    public int currentIndex;

    /**
     * Constructs a new CycleButton.
     *
     * @param options      the array of options to be displayed on the button
     * @param initialIndex the index of the option to start with
     */
    public CycleButton(String[] options, int initialIndex) {
        super();
        this.options = options;
        this.currentIndex = initialIndex;
        setText(options[currentIndex]);
        setOpaque(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addActionListener(e -> {
            currentIndex = (currentIndex + 1) % options.length;
            setText(this.options[currentIndex]);
        });
    }

    /**
     * Returns the index of the currently selected option.
     *
     * @return the current index
     */
    public int getSelectedIndex() {
        return currentIndex;
    }

    /**
     * Returns the string value of the currently selected option.
     *
     * @return the selected option string
     */
    public String getSelectedOption() {
        return options[currentIndex];
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2);
        boolean hover = getModel().isRollover();
        g2.setColor(hover ? new Color(40, 40, 70) : new Color(30, 30, 50));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.setColor(hover ? Color.WHITE : RenderUtils.cyan);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        String t = "◄  " + getText() + "  ►";
        g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
