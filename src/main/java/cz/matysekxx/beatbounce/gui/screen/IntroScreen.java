package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.gui.ButtonFactory;
import cz.matysekxx.beatbounce.util.Utility;

import javax.swing.*;
import java.awt.*;

public class IntroScreen extends Screen {;
    public IntroScreen(ScreenManager screenManager) {
        super();
        this.setLayout(new BorderLayout());

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 100));
        buttonPanel.setOpaque(false);


        final JButton startButton = ButtonFactory.createStartButton(e -> {
            Utility.sleep(200);
            screenManager.showScreen(MainMenuScreen.class);
            this.dispose();
        });
        buttonPanel.add(startButton);

        final JButton exitButton = ButtonFactory.createExitButton(e -> {
            Utility.sleep(200);
            System.exit(0);
        });
        buttonPanel.add(exitButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
        this.setVisible(true);
    }
}
