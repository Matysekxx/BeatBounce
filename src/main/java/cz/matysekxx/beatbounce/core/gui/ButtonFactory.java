package cz.matysekxx.beatbounce.core.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ButtonFactory {
    public static JButton createStartButton(ActionListener actionListener) {
        final JButton button = new JButton("Start");
        button.addActionListener(actionListener);
        button.setBackground(new Color(56, 22, 104));
        button.setForeground(new Color(60, 18, 184));
        return button;
    }
}
