package cz.matysekxx.beatbounce.gui.screen;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.components.*;

import javax.swing.*;
import java.awt.*;

/**
 * The main menu screen of the BeatBounce application.
 * It provides navigation to various sections of the game such as song selection,
 * library, skins, shop, and settings.
 */
public class MainMenuScreen extends Screen {

    private final MainMenuPanel backgroundPanel;
    private final AudiusClient audiusClient;
    private final ObjectMapper objectMapper;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    private final SongSelectionPanel songSelectionPanel;
    private final LibraryPanel libraryPanel;
    private final ScreenManager screenManager;
    private final String[] buttonsTitles = {
            "SONGS", "LIBRARY", "SKINS", "SHOP", "SETTINGS"
    };
    private final JPanel sidebar;
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

        backgroundPanel = new MainMenuPanel();
        backgroundPanel.setLayout(new BorderLayout());
        this.setContentPane(backgroundPanel);

        sidebar = createSidebar();
        backgroundPanel.add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        backgroundPanel.add(cardPanel, BorderLayout.CENTER);

        songSelectionPanel = new SongSelectionPanel(audiusClient, objectMapper, screenManager);
        libraryPanel = new LibraryPanel(audiusClient, screenManager);
        final SkinsPanel skinsPanel = new SkinsPanel();
        final ShopPanel shopPanel = new ShopPanel();
        final SettingsPanel settingsPanel = new SettingsPanel(screenManager);

        cardPanel.add(songSelectionPanel, "SONGS");
        cardPanel.add(libraryPanel, "LIBRARY");
        cardPanel.add(skinsPanel, "SKINS");
        cardPanel.add(shopPanel, "SHOP");
        cardPanel.add(settingsPanel, "SETTINGS");
    }

    private static JPanel getJPanel() {
        final JPanel p = new JPanel() {
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
        };
        p.setPreferredSize(new Dimension(280, 0));
        p.setLayout(new GridBagLayout());
        p.setOpaque(false);
        return p;
    }

    private JPanel createSidebar() {
        final JPanel p = getJPanel();

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(40, 0, 50, 0);

        final JLabel logo = new JLabel("BEAT BOUNCE");
        logo.setFont(new Font("Monospaced", Font.BOLD | Font.ITALIC, 32));
        logo.setForeground(RenderUtils.cyan);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(logo, gbc);

        gbc.insets = new Insets(5, 20, 5, 0);
        gbc.weightx = 1.0;
        int row = 1;
        for (String name : buttonsTitles) {
            gbc.gridy = row++;
            p.add(createSidebarButton(name), gbc);
        }

        gbc.gridy = row++;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = new Insets(0, 20, 30, 0);
        p.add(createSidebarButton("EXIT"), gbc);

        return p;
    }

    private JButton createSidebarButton(String title) {
        final JButton btn = new JButton(title) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphics2D(g2);

                boolean active = activePanel.equals(getText());
                if (active || getModel().isRollover()) {
                    g2.setPaint(new LinearGradientPaint(0, 0, getWidth(), 0,
                            new float[]{0f, 1f},
                            new Color[]{new Color(0, 255, 255, 40), new Color(0, 255, 255, 0)}));
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    g2.setColor(RenderUtils.cyan);
                    g2.fillRect(0, 5, 4, getHeight() - 10);
                }

                g2.setFont(getFont());
                g2.setColor(active ? Color.WHITE : (getModel().isRollover() ? RenderUtils.cyan : new Color(200, 200, 220)));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 30, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);

                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        btn.setPreferredSize(new Dimension(260, 55));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(_ -> {
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

    private void showPanel(String name) {
        if (name.equals("LIBRARY")) {
            libraryPanel.loadLibrary();
        }
        cardLayout.show(cardPanel, name);
    }

    /**
     * Starts the animations for the main menu screen.
     */
    @Override
    public void start() {
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