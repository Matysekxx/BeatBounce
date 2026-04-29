package cz.matysekxx.beatbounce.gui;

import cz.matysekxx.beatbounce.gui.components.IsometricButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ButtonFactory {
    private static final Color MAGENTA_TOP_GLOW = new Color(255, 100, 200);
    private static final Color MAGENTA_TOP_FILL = new Color(255, 0, 128);
    private static final Color MAGENTA_FRONT_SIDE = new Color(100, 0, 50);
    private static final Color CYAN_TOP_GLOW = new Color(100, 255, 255);
    private static final Color CYAN_TOP_FILL = new Color(0, 200, 255);
    private static final Color CYAN_FRONT_SIDE = new Color(0, 70, 100);
    private static final Color YELLOW_TOP_GLOW = new Color(255, 255, 150);
    private static final Color YELLOW_TOP_FILL = new Color(255, 215, 0);
    private static final Color YELLOW_FRONT_SIDE = new Color(120, 100, 0);

    public static JButton createStartButton(ActionListener actionListener) {
        return createButton("START", MAGENTA_TOP_FILL, MAGENTA_TOP_GLOW, MAGENTA_FRONT_SIDE, actionListener);
    }

    public static JButton createExitButton(ActionListener actionListener) {
        return createButton("EXIT", CYAN_TOP_FILL, CYAN_TOP_GLOW, CYAN_FRONT_SIDE, actionListener);
    }

    public static JButton createCreditButton(ActionListener listener) {
        return createButton("CREDITS", YELLOW_TOP_FILL, YELLOW_TOP_GLOW, YELLOW_FRONT_SIDE, listener);
    }

    private static JButton createButton(String text, Color topFill, Color topGlow, Color frontSide, ActionListener listener) {
        final IsometricButton button = new IsometricButton(
                text, frontSide, topFill, topGlow, 10, 40, new Dimension(220, 80)
        );
        button.addActionListener(listener);
        button.setOpaque(false);
        return button;
    }
}