package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.gui.ButtonFactory;
import cz.matysekxx.beatbounce.gui.components.CreditsPanel;
import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import java.awt.*;

/**
 * Screen that displays the game credits.
 *
 * @author Matysekxx
 */
public class CreditsScreen extends Screen {
    /**
     * The panel that handles the scrolling and rendering of the credits text.
     */
    private final CreditsPanel creditsPanel;

    /**
     * Constructs a new CreditsScreen and initializes the UI components.
     * Sets up a back button to return to the IntroScreen.
     *
     * @param screenManager the screen manager used for navigation
     */
    public CreditsScreen(ScreenManager screenManager) {
        super();
        this.setLayout(new BorderLayout());

        creditsPanel = new CreditsPanel();
        creditsPanel.setLayout(new GridBagLayout());
        this.setContentPane(creditsPanel);

        final JButton backButton = ButtonFactory.createBackButton(_ -> {
            screenManager.showScreen(IntroScreen.class);
        });

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.insets = new Insets(0, 0, UIScale.scale(30), UIScale.scale(30));
        creditsPanel.add(backButton, gbc);
    }

    @Override
    public void start() {
        creditsPanel.startAnimation();
        this.requestFocusInWindow();
    }

    @Override
    public void stop() {
        creditsPanel.stopAnimation();
    }
}
