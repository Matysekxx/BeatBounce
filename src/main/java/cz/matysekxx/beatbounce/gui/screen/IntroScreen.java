package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.gui.ButtonFactory;
import cz.matysekxx.beatbounce.gui.components.IntroPanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static cz.matysekxx.beatbounce.util.Time.sleep;

public class IntroScreen extends Screen {
    private final IntroPanel backgroundPanel;

    public IntroScreen(ScreenManager screenManager) {
        super();
        this.setLayout(new BorderLayout());
        backgroundPanel = new IntroPanel();
        backgroundPanel.setLayout(new BorderLayout());
        this.setContentPane(backgroundPanel);

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 100));
        buttonPanel.setOpaque(false);

        final JButton startButton = ButtonFactory.createStartButton(e -> {
            sleep(200);
            ((JButton) e.getSource()).setEnabled(false);
            ((JButton) e.getSource()).setText("LOADING...");
            final SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    screenManager.initScreen(MainMenuScreen.class);
                    return null;
                }

                @Override
                protected void done() {
                    screenManager.showScreen(MainMenuScreen.class);
                }
            };
            worker.execute();
        });

        buttonPanel.add(startButton);

        final JButton creditButton = ButtonFactory.createCreditButton(e -> {
            sleep(200);
            Thread.ofVirtual().start(() -> {
                try {
                    if (Desktop.isDesktopSupported())
                        Desktop.getDesktop().browse(new URI("https://github.com/Matysekxx/BeatBounce"));
                } catch (IOException | URISyntaxException ex) {
                    throw new RuntimeException("Cannot open browser", ex);
                }
            });
        });
        buttonPanel.add(creditButton);

        final JButton exitButton = ButtonFactory.createExitButton(e -> {
            sleep(200);
            System.exit(0);
        });
        buttonPanel.add(exitButton);

        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override
    public void start() {
        backgroundPanel.startAnimation();
    }

    @Override
    public void stop() {
        backgroundPanel.stopAnimation();
    }
}
