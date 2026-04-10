package cz.matysekxx.beatbounce.core.gui;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ButtonFactory {
    private static final Color MAGENTA_GLOW = new Color(255, 23, 140);
    private static final Color MAGENTA_DARK = new Color(130, 0, 120);
    private static final Color CYAN_GLOW = new Color(0, 255, 255);
    private static final Color CYAN_DARK = new Color(0, 80, 100);
    private static final Color WHITE = new Color(255, 255, 255);

    public static JButton createStartButton(ActionListener actionListener) {
        final JButton button = new JButton("Start");
        button.addActionListener(actionListener);
        button.setBackground(MAGENTA_DARK);
        button.setForeground(WHITE);
        button.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, MAGENTA_GLOW, MAGENTA_DARK.darker()));
        return button;
    }

    public static JButton createExitButton(ActionListener actionListener) {
        final JButton button = new JButton("Exit");
        button.addActionListener(actionListener);
        button.setBackground(CYAN_DARK);
        button.setForeground(WHITE);

        button.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, CYAN_GLOW, CYAN_DARK.darker()));
        styleNeonButton(button);
        return button;
    }

    private static void styleNeonButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 18));
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(button.getBackground().brighter());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(button.getBackground().darker());
            }
        });
    }
}
