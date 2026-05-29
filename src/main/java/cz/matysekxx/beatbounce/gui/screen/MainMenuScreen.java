package cz.matysekxx.beatbounce.gui.screen;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.components.*;
import cz.matysekxx.beatbounce.model.audio.AudioManager;
import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import java.awt.*;

/**
 * The main menu screen of the BeatBounce application.
 * It provides navigation to various sections of the game such as song selection,
 * library, skins, shop, and settings.
 */
public class MainMenuScreen extends Screen {

    /**
     * The background panel for the main menu.
     */
    private final MainPanel backgroundPanel;

    /**
     * Client for Audius API interactions.
     */
    private final AudiusClient audiusClient;

    /**
     * JSON object mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * Layout manager for switching between different sub-panels.
     */
    private final CardLayout cardLayout;

    /**
     * The panel that holds the sub-sections (Songs, Library, Settings).
     */
    private final JPanel cardPanel;

    /**
     * Sub-panel for browsing and selecting songs.
     */
    private final SongSelectionPanel songSelectionPanel;

    /**
     * Sub-panel for managing the local song library.
     */
    private final LibraryPanel libraryPanel;

    /**
     * Sub-panel for showing achievements.
     */
    private final AchievementsPanel achievementsPanel;

    /**
     * Manager for handling screen transitions.
     */
    private final ScreenManager screenManager;

    /**
     * Titles for the primary sidebar navigation buttons.
     */
    private final String[] buttonsTitles = {
            "SONGS", "LIBRARY", "ACHIEVEMENTS", "SETTINGS"
    };

    /**
     * The sidebar panel containing navigation links.
     */
    private final JPanel sidebar;

    /**
     * The name of the currently active sub-panel.
     */
    private String activePanel = "SONGS";

    /**
     * Constructs a new MainMenuScreen.
     *
     * @param screenManager the screen manager used for navigation
     */
    public MainMenuScreen(ScreenManager screenManager) {
        super();
        this.audiusClient = new AudiusClient();
        this.objectMapper = new ObjectMapper();
        this.screenManager = screenManager;
        this.setLayout(new BorderLayout());

        backgroundPanel = new MainPanel();
        backgroundPanel.setLayout(new BorderLayout());
        this.setContentPane(backgroundPanel);

        sidebar = createSidebar();
        backgroundPanel.add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(UIScale.scale(20), UIScale.scale(20), UIScale.scale(20), UIScale.scale(20)));
        backgroundPanel.add(cardPanel, BorderLayout.CENTER);

        songSelectionPanel = new SongSelectionPanel(audiusClient, objectMapper, screenManager);
        libraryPanel = new LibraryPanel(screenManager);
        achievementsPanel = new AchievementsPanel();
        final SettingsPanel settingsPanel = new SettingsPanel(screenManager);

        cardPanel.add(songSelectionPanel, "SONGS");
        cardPanel.add(libraryPanel, "LIBRARY");
        cardPanel.add(achievementsPanel, "ACHIEVEMENTS");
        cardPanel.add(settingsPanel, "SETTINGS");
    }

    /**
     * Helper to create a styled transparent JPanel.
     *
     * @return a new styled JPanel
     */
    private static JPanel getJPanel() {
        final JPanel p = new JPanel() {
            /**
             * Paints the sidebar background with a linear gradient and a border line.
             *
             * @param g the Graphics context to use for painting
             */
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphics2D(g2);
                g2.setPaint(new LinearGradientPaint(0, 0, getWidth(), 0,
                        new float[]{0f, 1f},
                        new Color[]{new Color(15, 15, 35, 220), new Color(10, 10, 25, 100)}));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0, 255, 255, 40));
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }

            /**
             * Returns the preferred size of the sidebar panel, applying UI scaling to the width.
             *
             * @return the scaled preferred dimension
             */
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(UIScale.scale(280), super.getPreferredSize().height);
            }
        };
        p.setLayout(new GridBagLayout());
        p.setOpaque(false);
        return p;
    }

    /**
     * Creates and populates the sidebar navigation panel.
     *
     * @return the created sidebar JPanel
     */
    private JPanel createSidebar() {
        final JPanel p = getJPanel();

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(UIScale.scale(40), 0, UIScale.scale(50), 0);

        final JLabel logo = new JLabel("BEAT BOUNCE");
        logo.setFont(UIScale.scaleFont(RenderCache.AUDIOWIDE_24));
        logo.setForeground(RenderUtils.cyan);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(logo, gbc);

        gbc.insets = new Insets(UIScale.scale(5), UIScale.scale(20), UIScale.scale(5), 0);
        gbc.weightx = 1.0;
        int row = 1;
        for (String name : buttonsTitles) {
            gbc.gridy = row++;
            p.add(createSidebarButton(name), gbc);
        }

        gbc.gridy = row;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = new Insets(0, UIScale.scale(20), UIScale.scale(30), 0);
        p.add(createSidebarButton("EXIT"), gbc);

        return p;
    }

    /**
     * Creates a styled button for the sidebar.
     *
     * @param title the text to display on the button
     * @return the created sidebar JButton
     */
    private JButton createSidebarButton(String title) {
        final JButton btn = new JButton(title) {
            /**
             * Paints the sidebar button with a custom background and text styling.
             *
             * @param g the Graphics context to use for painting
             */
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphics2D(g2);

                final boolean active = activePanel.equals(getText());
                if (active || getModel().isRollover()) {
                    g2.setPaint(new LinearGradientPaint(0, 0, getWidth(), 0,
                            new float[]{0f, 1f},
                            new Color[]{new Color(0, 255, 255, 40), new Color(0, 255, 255, 0)}));
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    g2.setColor(RenderUtils.cyan);
                    g2.fillRect(0, UIScale.scale(5), UIScale.scale(4), getHeight() - UIScale.scale(10));
                }

                g2.setFont(getFont());
                g2.setColor(active ? Color.WHITE : (getModel().isRollover() ? RenderUtils.cyan : new Color(200, 200, 220)));
                final FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), UIScale.scale(30), (getHeight() + fm.getAscent() - fm.getDescent()) / 2);

                g2.dispose();
            }

            /**
             * Returns the preferred size of the sidebar button, applying UI scaling.
             *
             * @return the scaled preferred dimension
             */
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(UIScale.scale(260), UIScale.scale(55));
            }
        };
        btn.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_18));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(_ -> {
            AudioManager.playSFX("/click-sound.mp3");
            if (title.equals("EXIT")) {
                screenManager.showScreen(IntroScreen.class);
            } else {
                activePanel = title;
                showPanel(title);
                sidebar.repaint();
            }
        });

        return btn;
    }

    /**
     * Switches the displayed sub-panel.
     *
     * @param name the name of the panel to show
     */
    private void showPanel(String name) {
        switch (name) {
            case "LIBRARY" -> libraryPanel.loadLibrary();
            case "ACHIEVEMENTS" -> achievementsPanel.loadAchievements();
        }
        cardLayout.show(cardPanel, name);
    }

    /**
     * Starts the animations for the main menu screen.
     */
    @Override
    public void start() {
        AudioManager.playMenuMusic("/background-sound.mp3");
        backgroundPanel.startAnimation();
        songSelectionPanel.startAnimations();
    }

    /**
     * Stops the animations for the main menu screen.
     */
    @Override
    public void stop() {
        backgroundPanel.stopAnimation();
        songSelectionPanel.stopAnimations();
    }
}
