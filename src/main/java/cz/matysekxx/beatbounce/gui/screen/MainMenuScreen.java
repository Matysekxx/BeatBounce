package cz.matysekxx.beatbounce.gui.screen;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.components.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainMenuScreen extends Screen {

    private final MainMenuPanel backgroundPanel;
    private final AudiusClient audiusClient;
    private final ObjectMapper objectMapper;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    
    private final SongSelectionPanel songSelectionPanel;
    private final LibraryPanel libraryPanel;
    private final ScreenManager screenManager;

    public MainMenuScreen(ScreenManager screenManager) {
        super();
        this.audiusClient = new AudiusClient();
        this.objectMapper = new ObjectMapper();
        this.screenManager = screenManager;
        this.setLayout(new BorderLayout());

        backgroundPanel = new MainMenuPanel();
        backgroundPanel.setLayout(new BorderLayout());
        this.setContentPane(backgroundPanel);

        final JLayeredPane layeredPane = new JLayeredPane();
        backgroundPanel.add(layeredPane, BorderLayout.CENTER);
        layeredPane.setLayout(new OverlayLayout(layeredPane));

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        layeredPane.add(cardPanel, JLayeredPane.DEFAULT_LAYER);

        songSelectionPanel = new SongSelectionPanel(audiusClient, objectMapper, screenManager);
        libraryPanel = new LibraryPanel(audiusClient, screenManager);
        final SkinsPanel skinsPanel = new SkinsPanel();
        final ShopPanel shopPanel = new ShopPanel();
        final SettingsPanel settingsPanel = new SettingsPanel();

        cardPanel.add(songSelectionPanel, "SONGS");
        cardPanel.add(libraryPanel, "LIBRARY");
        cardPanel.add(skinsPanel, "SKINS");
        cardPanel.add(shopPanel, "SHOP");
        cardPanel.add(settingsPanel, "SETTINGS");

        final JPanel bottomBar = createBottomBar();
        backgroundPanel.add(bottomBar, BorderLayout.SOUTH);
    }

    private final String[] buttonsTitles = {
            "SONGS", "LIBRARY", "SKINS", "SHOP", "SETTINGS"
    };
    private JPanel createBottomBar() {
        final JPanel bottomBar = getJPanel();
        for (String name : buttonsTitles) {
            bottomBar.add(createNavButton(name, _ -> showPanel(name)));
        }
        bottomBar.add(createNavButton("BACK", _ -> screenManager.showScreen(IntroScreen.class)));
        return bottomBar;
    }

    private static JPanel getJPanel() {
        final JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphic2D(g2);
                g2.setColor(new Color(10, 10, 26, 242));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0, 255, 255, 51));
                g2.drawLine(0, 0, getWidth(), 0);
                g2.dispose();
            }
        };
        bottomBar.setPreferredSize(new Dimension(0, 52));
        return bottomBar;
    }

    private void showPanel(String name) {
        if (name.equals("LIBRARY")) {
            libraryPanel.loadLibrary();
        }
        cardLayout.show(cardPanel, name);
    }

    private JButton createNavButton(String title, ActionListener action) {
        final JButton btn = new JButton(title) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphic2D(g2);
                g2.setColor(RenderUtils.cyan);
                final FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 0, fm.getAscent());
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        return btn;
    }

    @Override
    public void start() {
        backgroundPanel.startAnimation();
        songSelectionPanel.startAnimations();
    }

    @Override
    public void stop() {
        backgroundPanel.stopAnimation();
        songSelectionPanel.stopAnimations();
    }
}