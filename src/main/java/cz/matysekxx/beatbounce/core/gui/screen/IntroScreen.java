package cz.matysekxx.beatbounce.core.gui.screen;

import cz.matysekxx.beatbounce.core.gui.ButtonFactory;

import javax.swing.*;
import java.awt.*;

public class IntroScreen extends Screen {
    public IntroScreen() {
        super();
        this.setLayout(new GridBagLayout());

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setOpaque(false);

        final JButton startButton = ButtonFactory.createStartButton(e -> {
            new MainMenuScreen();
            this.dispose();
        });
        buttonPanel.add(startButton);

        final JButton exitButton = ButtonFactory.createExitButton(e -> {
            System.exit(0);
        });
        buttonPanel.add(exitButton);

        this.add(buttonPanel);
    }
}
