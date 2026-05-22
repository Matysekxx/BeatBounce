package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;

import javax.swing.*;
import java.awt.*;

/**
 * A class that represents a button-like UI element that is not a true Swing component.
 * It is used for custom rendering within a {@link JPanel} and handles its own hit detection and drawing.
 */
public class SimulatedButton {
    /**
     * Background color for the button in its normal state.
     */
    private static final Color HINT_BG = new Color(255, 255, 255, 15);

    /**
     * Border color for the button.
     */
    private static final Color HINT_BORDER = new Color(255, 255, 255, 30);

    /**
     * Label color for the button in its normal state.
     */
    private static final Color HINT_LABEL = new Color(220, 220, 220);

    /**
     * The text label to display on the button.
     */
    private String label;

    /**
     * The x-coordinate of the button's top-left corner.
     */
    private int x;

    /**
     * The y-coordinate of the button's top-left corner.
     */
    private int y;

    /**
     * The width of the button in pixels.
     */
    private int width;

    /**
     * The height of the button in pixels.
     */
    private int height;

    /**
     * The action to be triggered when this button is clicked.
     */
    private UIAction action;

    /**
     * Constructs a new SimulatedButton.
     *
     * @param label  the text to display on the button
     * @param x      the x-coordinate of the button
     * @param y      the y-coordinate of the button
     * @param width  the width of the button
     * @param height the height of the button
     * @param action the action to perform when the button is clicked
     */
    public SimulatedButton(String label, int x, int y, int width, int height, UIAction action) {
        setup(label, x, y, width, height, action);
    }

    /**
     * Updates the button's properties.
     *
     * @param label  the text label
     * @param x      the x-coordinate
     * @param y      the y-coordinate
     * @param width  the width
     * @param height the height
     * @param action the action
     */
    public void setup(String label, int x, int y, int width, int height, UIAction action) {
        this.label = label;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.action = action;
    }

    /**
     * Checks if the given coordinates are within the button's bounds.
     *
     * @param mx the x-coordinate to check
     * @param my the y-coordinate to check
     * @return true if the coordinates are within the button's bounds, false otherwise
     */
    public boolean contains(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    /**
     * Returns the action associated with this button.
     *
     * @return the UIAction
     */
    public UIAction getAction() {
        return action;
    }

    /**
     * Draws the button on the given graphics context.
     *
     * @param g2d        the graphics context
     * @param mouseX     the current x-coordinate of the mouse
     * @param mouseY     the current y-coordinate of the mouse
     * @param translateY the y-translation to apply to the button's position
     */
    public void draw(Graphics2D g2d, int mouseX, int mouseY, int translateY) {
        g2d.setFont(RenderCache.SANS_BOLD_18);
        final int labelW = g2d.getFontMetrics().stringWidth(label);

        final boolean hovered = contains(mouseX, mouseY - translateY);

        if (hovered) {
            g2d.setColor(new Color(255, 255, 255, 40));
        } else {
            g2d.setColor(HINT_BG);
        }
        g2d.fillRoundRect(x, y, width, height, 20, 20);

        g2d.setColor(hovered ? RenderUtils.cyan : HINT_BORDER);
        g2d.setStroke(hovered ? RenderCache.STROKE_2 : RenderCache.STROKE_1);
        g2d.drawRoundRect(x, y, width, height, 20, 20);
        g2d.setStroke(RenderCache.STROKE_1);

        g2d.setColor(hovered ? Color.WHITE : HINT_LABEL);
        g2d.drawString(label, x + (width - labelW) / 2, y + height / 2 + 5);
    }
}
