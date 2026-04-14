package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.gui.ButtonFactory;
import static cz.matysekxx.beatbounce.util.Time.sleep;

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
            sleep(200);
            screenManager.showScreen(GameScreen.class);
            this.dispose();
        });
        buttonPanel.add(startButton);

        final JButton exitButton = ButtonFactory.createExitButton(e -> {
            sleep(200);
            System.exit(0);
        });
        buttonPanel.add(exitButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }
}
