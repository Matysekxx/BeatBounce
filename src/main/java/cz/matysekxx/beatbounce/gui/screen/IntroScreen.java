package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.gui.ButtonFactory;
import cz.matysekxx.beatbounce.gui.components.IntroPanel;
import cz.matysekxx.beatbounce.model.audio.AudioManager;

import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static cz.matysekxx.beatbounce.util.Time.sleep;

/**
 * The initial screen of the game, featuring the game title and navigation buttons.
 */
public class IntroScreen extends Screen {
    /**
     * The animated background panel for the intro screen.
     */
    private final IntroPanel backgroundPanel;

    /**
     * Constructs a new IntroScreen.
     *
     * @param screenManager the screen manager used for navigation
     */
    public IntroScreen(ScreenManager screenManager) {
        super();
        this.setLayout(new BorderLayout());
        backgroundPanel = new IntroPanel();
        backgroundPanel.setLayout(new GridBagLayout());
        this.setContentPane(backgroundPanel);

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, UIScale.scale(40), UIScale.scale(20)));
        buttonPanel.setOpaque(false);

        final JButton startButton = ButtonFactory.createStartButton(e -> {
            sleep(200);
            final JButton source = (JButton) e.getSource();
            source.setEnabled(false);
            final SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    screenManager.initScreen(MainMenuScreen.class);
                    return null;
                }

                @Override
                protected void done() {
                    screenManager.showScreen(MainMenuScreen.class);
                    source.setText("START");
                    source.setEnabled(true);
                }
            };
            worker.execute();
        });

        final JButton creditButton = ButtonFactory.createCreditButton(e -> {
            sleep(200);
            final JButton source = (JButton) e.getSource();
            source.setEnabled(false);
            final SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    screenManager.initScreen(CreditsScreen.class);
                    return null;
                }

                @Override
                protected void done() {
                    screenManager.showScreen(CreditsScreen.class);
                    source.setEnabled(true);
                }
            };
            worker.execute();
        });

        final JButton exitButton = ButtonFactory.createExitButton(_ -> {
            sleep(200);
            System.exit(0);
        });

        buttonPanel.add(startButton);
        buttonPanel.add(creditButton);
        buttonPanel.add(exitButton);

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.6;
        backgroundPanel.add(Box.createVerticalGlue(), gbc);

        gbc.gridy = 1;
        gbc.weighty = 0.4;
        backgroundPanel.add(buttonPanel, gbc);

        this.setFocusable(true);
    }

    /**
     * Starts the intro screen animations and requests focus.
     */
    @Override
    public void start() {
        AudioManager.playMenuMusic("/background-sound-2.mp3");
        backgroundPanel.startAnimation();
        this.requestFocusInWindow();
    }

    /**
     * Stops the intro screen animations.
     */
    @Override
    public void stop() {
        backgroundPanel.stopAnimation();
    }
}