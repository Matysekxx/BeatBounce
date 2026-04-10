package cz.matysekxx.beatbounce.core.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ButtonFactory {
    private static final Color MAGENTA_TOP_GLOW = new Color(255, 30, 240);
    private static final Color MAGENTA_TOP_FILL = new Color(230, 0, 200);
    private static final Color MAGENTA_FRONT_SIDE = new Color(100, 0, 90);
    private static final Color CYAN_TOP_GLOW = new Color(0, 255, 255);
    private static final Color CYAN_TOP_FILL = new Color(0, 200, 220);
    private static final Color CYAN_FRONT_SIDE = new Color(0, 70, 90);

    public static JButton createStartButton(ActionListener actionListener) {
        return createButton("START", MAGENTA_TOP_FILL, MAGENTA_TOP_GLOW, MAGENTA_FRONT_SIDE, actionListener);
    }

    public static JButton createExitButton(ActionListener actionListener) {
        return createButton("EXIT", CYAN_TOP_FILL, CYAN_TOP_GLOW, CYAN_FRONT_SIDE, actionListener);
    }

    private static JButton createButton(String text, Color topFill, Color topGlow, Color frontSide, ActionListener listener) {
        final IsometricNeonButton button = new IsometricNeonButton(
                text, frontSide, topFill, topGlow, 10, 40, new Dimension(220, 80)
        );
        button.addActionListener(listener);
        button.setOpaque(false);
        return button;
    }
}