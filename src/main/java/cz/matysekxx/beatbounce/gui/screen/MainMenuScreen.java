package cz.matysekxx.beatbounce.gui.screen;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.gui.components.DiscoverPanel;
import cz.matysekxx.beatbounce.gui.components.MainMenuPanel;
import cz.matysekxx.beatbounce.gui.components.SidebarButton;
import cz.matysekxx.beatbounce.gui.components.SidebarPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

public class MainMenuScreen extends Screen {

    private final MainMenuPanel backgroundPanel;
    private final AudiusClient audiusClient;
    private final ObjectMapper objectMapper;
    private final ScreenManager screenManager;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public MainMenuScreen(ScreenManager screenManager) {
        super();
        this.screenManager = screenManager;
        this.audiusClient = new AudiusClient();
        this.objectMapper = new ObjectMapper();

        this.setLayout(new BorderLayout());

        backgroundPanel = new MainMenuPanel();
        backgroundPanel.setLayout(new BorderLayout());
        this.setContentPane(backgroundPanel);
        
        final JPanel sidebar = new SidebarPanel();
        sidebar.setLayout(new BorderLayout());

        final JLabel logo = new JLabel("BeatBounce");
        logo.setFont(new Font("SansSerif", Font.BOLD, 28));
        logo.setForeground(Color.WHITE);
        logo.setBorder(new EmptyBorder(40, 30, 50, 30));
        sidebar.add(logo, BorderLayout.NORTH);

        final JPanel navLinks = new JPanel();
        navLinks.setLayout(new BoxLayout(navLinks, BoxLayout.Y_AXIS));
        navLinks.setOpaque(false);
        navLinks.setBorder(new EmptyBorder(0, 15, 0, 15));

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        final Consumer<String> cardSwitcher = target -> cardLayout.show(cardPanel, target);

        navLinks.add(createAlignedButton(new SidebarButton("Discover", "DISCOVER", true, cardSwitcher)));
        navLinks.add(Box.createRigidArea(new Dimension(0, 5)));
        navLinks.add(createAlignedButton(new SidebarButton("Library", "LIBRARY", false, null)));
        navLinks.add(Box.createRigidArea(new Dimension(0, 5)));
        navLinks.add(createAlignedButton(new SidebarButton("Skins", "SKINS", false, null)));
        navLinks.add(Box.createRigidArea(new Dimension(0, 5)));
        navLinks.add(createAlignedButton(new SidebarButton("Achievements", "ACHIEVEMENTS", false, null)));
        navLinks.add(Box.createRigidArea(new Dimension(0, 5)));
        navLinks.add(createAlignedButton(new SidebarButton("Shop", "SHOP", false, null)));
        
        navLinks.add(Box.createVerticalGlue());
        
        sidebar.add(navLinks, BorderLayout.CENTER);

        final JPanel bottomSidebar = new JPanel();
        bottomSidebar.setLayout(new BoxLayout(bottomSidebar, BoxLayout.Y_AXIS));
        bottomSidebar.setOpaque(false);
        bottomSidebar.setBorder(new EmptyBorder(0, 15, 40, 15));

        final JButton profileBtn = createAlignedButton(new SidebarButton("Settings", null, false, null));
        final JButton exitBtn = createAlignedButton(new SidebarButton("Exit", null, false, null, e -> screenManager.showScreen(IntroScreen.class)));
        exitBtn.setForeground(new Color(255, 100, 100, 200));

        bottomSidebar.add(profileBtn);
        bottomSidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        bottomSidebar.add(exitBtn);
        
        sidebar.add(bottomSidebar, BorderLayout.SOUTH);

        backgroundPanel.add(sidebar, BorderLayout.WEST);

        cardPanel.add(new DiscoverPanel(audiusClient, objectMapper, screenManager), "DISCOVER");

        backgroundPanel.add(cardPanel, BorderLayout.CENTER);
    }
    private JButton createAlignedButton(SidebarButton button) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        return button;
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
