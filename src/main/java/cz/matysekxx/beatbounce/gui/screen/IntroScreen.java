package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.gui.ButtonFactory;
import cz.matysekxx.beatbounce.gui.components.IntroPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
        backgroundPanel.setLayout(new GridBagLayout());
        this.setContentPane(backgroundPanel);

        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 20));
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
            Thread.ofVirtual().start(() -> {
                try {
                    if (Desktop.isDesktopSupported())
                        Desktop.getDesktop().browse(new URI("https://github.com/Matysekxx/BeatBounce"));
                } catch (IOException | URISyntaxException ex) {
                    throw new RuntimeException("Cannot open browser", ex);
                }
            });
        });

        final JButton exitButton = ButtonFactory.createExitButton(e -> {
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

    @Override
    public void start() {
        backgroundPanel.startAnimation();
    }

    @Override
    public void stop() {
        backgroundPanel.stopAnimation();
    }
}