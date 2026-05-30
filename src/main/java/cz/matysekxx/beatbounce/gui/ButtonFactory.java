package cz.matysekxx.beatbounce.gui;

import cz.matysekxx.beatbounce.gui.components.IsometricButton;
import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Factory class for creating styled isometric buttons used in the application.
 *
 * @author Matysekxx
 */
public class ButtonFactory {
    /**
     * Glow color for magenta-themed buttons.
     */
    private static final Color MAGENTA_TOP_GLOW = new Color(255, 100, 200);

    /**
     * Fill color for magenta-themed buttons.
     */
    private static final Color MAGENTA_TOP_FILL = new Color(255, 0, 128);

    /**
     * Front face color for magenta-themed buttons.
     */
    private static final Color MAGENTA_FRONT_SIDE = new Color(100, 0, 50);

    /**
     * Glow color for cyan-themed buttons.
     */
    private static final Color CYAN_TOP_GLOW = new Color(100, 255, 255);

    /**
     * Fill color for cyan-themed buttons.
     */
    private static final Color CYAN_TOP_FILL = new Color(0, 200, 255);

    /**
     * Front face color for cyan-themed buttons.
     */
    private static final Color CYAN_FRONT_SIDE = new Color(0, 70, 100);

    /**
     * Glow color for gold-themed buttons (improved yellow).
     */
    private static final Color GOLD_TOP_GLOW = new Color(255, 230, 100);

    /**
     * Fill color for gold-themed buttons (improved yellow).
     */
    private static final Color GOLD_TOP_FILL = new Color(220, 180, 0);

    /**
     * Front face color for gold-themed buttons (improved yellow).
     */
    private static final Color GOLD_FRONT_SIDE = new Color(100, 80, 0);

    /**
     * Glow color for red-themed buttons.
     */
    private static final Color RED_TOP_GLOW = new Color(255, 120, 120);

    /**
     * Fill color for red-themed buttons.
     */
    private static final Color RED_TOP_FILL = new Color(230, 0, 0);

    /**
     * Front face color for red-themed buttons.
     */
    private static final Color RED_FRONT_SIDE = new Color(110, 0, 0);

    /**
     * Default dimension for primary isometric buttons.
     */
    private static final Dimension PRIMARY_BUTTON_SIZE = new Dimension(280, 100);

    /**
     * Creates a magenta styled "START" button.
     *
     * @param actionListener the listener to be notified when the button is clicked
     * @return a {@link JButton} configured with start button styling
     */
    public static JButton createStartButton(ActionListener actionListener) {
        return createButton("START", MAGENTA_TOP_FILL, MAGENTA_TOP_GLOW, MAGENTA_FRONT_SIDE, actionListener);
    }

    /**
     * Creates a cyan styled "EXIT" button.
     *
     * @param actionListener the listener to be notified when the button is clicked
     * @return a {@link JButton} configured with exit button styling
     */
    public static JButton createExitButton(ActionListener actionListener) {
        return createButton("EXIT", CYAN_TOP_FILL, CYAN_TOP_GLOW, CYAN_FRONT_SIDE, actionListener);
    }

    /**
     * Creates a gold styled "CREDITS" button.
     *
     * @param listener the listener to be notified when the button is clicked
     * @return a {@link JButton} configured with credits button styling
     */
    public static JButton createCreditButton(ActionListener listener) {
        return createButton("CREDITS", GOLD_TOP_FILL, GOLD_TOP_GLOW, GOLD_FRONT_SIDE, listener);
    }

    /**
     * Creates a generic isometric button with the specified text, colors, and listener.
     *
     * @param text      the text to display on the button
     * @param topFill   the fill color of the top face of the button
     * @param topGlow   the glow color of the top face of the button
     * @param frontSide the color of the front side of the button
     * @param listener  the listener to be notified when the button is clicked
     * @return a new {@link IsometricButton} instance
     */
    private static JButton createButton(String text, Color topFill, Color topGlow, Color frontSide, ActionListener listener) {
        final IsometricButton button = new IsometricButton(
                text, frontSide, topFill, topGlow, 15, 50, PRIMARY_BUTTON_SIZE
        );
        button.addActionListener(listener);
        button.setOpaque(false);
        return button;
    }

    /**
     * Creates a subdued, less visible "BACK" button.
     *
     * @param actionListener the listener to be notified when the button is clicked
     * @return a {@link JButton} configured with subdued styling
     */
    public static JButton createBackButton(ActionListener actionListener) {
        final Color grayFront = new Color(40, 40, 50);
        final Color grayTop = new Color(60, 60, 75);
        final Color grayGlow = new Color(100, 100, 120, 100);

        final IsometricButton button = new IsometricButton(
                "BACK", grayFront, grayTop, grayGlow, 4, 15, new Dimension(110, 40)
        );
        button.setFont(UIScale.scaleFont(RenderCache.AUDIOWIDE_24));
        button.setForeground(new Color(180, 180, 190));
        button.addActionListener(actionListener);
        button.setOpaque(false);
        return button;
    }

    /**
     * Creates a red styled secondary button.
     *
     * @param text           the text to display on the button
     * @param actionListener the listener to be notified when the button is clicked
     * @return a {@link JButton} configured with secondary button styling
     */
    public static JButton createSecondaryButton(String text, ActionListener actionListener) {
        final IsometricButton button = new IsometricButton(
                text, RED_FRONT_SIDE, RED_TOP_FILL, RED_TOP_GLOW, 15, 50, PRIMARY_BUTTON_SIZE
        );
        button.addActionListener(actionListener);
        button.setOpaque(false);
        return button;
    }
}
