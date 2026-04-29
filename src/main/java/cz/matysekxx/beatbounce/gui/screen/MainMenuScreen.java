package cz.matysekxx.beatbounce.gui.screen;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.gui.components.MainMenuPanel;
import cz.matysekxx.beatbounce.gui.components.SongSelectionPanel;

import javax.swing.*;
import java.awt.*;

public class MainMenuScreen extends Screen {

    private final MainMenuPanel backgroundPanel;
    private final AudiusClient audiusClient;
    private final ObjectMapper objectMapper;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    public MainMenuScreen(ScreenManager screenManager) {
        super();
        this.audiusClient = new AudiusClient();
        this.objectMapper = new ObjectMapper();

        this.setLayout(new BorderLayout());

        backgroundPanel = new MainMenuPanel();
        backgroundPanel.setLayout(new BorderLayout());
        this.setContentPane(backgroundPanel);

        JLayeredPane layeredPane = new JLayeredPane();
        backgroundPanel.add(layeredPane, BorderLayout.CENTER);
        layeredPane.setLayout(new OverlayLayout(layeredPane));

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        layeredPane.add(cardPanel, JLayeredPane.DEFAULT_LAYER);

        cardPanel.add(new SongSelectionPanel(audiusClient, objectMapper, screenManager), "SONG_SELECTION");
    }

    @Override
    public void start() {
        backgroundPanel.startAnimation();
        for (Component c : cardPanel.getComponents()) {
            if (c instanceof SongSelectionPanel ssp) ssp.startAnimations();
        }
    }

    @Override
    public void stop() {
        backgroundPanel.stopAnimation();
        for (Component c : cardPanel.getComponents()) {
            if (c instanceof SongSelectionPanel ssp) ssp.stopAnimations();
        }
    }
}