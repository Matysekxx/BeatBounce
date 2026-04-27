package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.screen.GameScreen;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TrackCard extends JPanel {
    private int hoverAlpha = 120;
    private float animOffset = 30f;
    private float animAlpha = 0f;
    private Timer enterTimer;

    private final String id;
    private final String title;
    private final String artist;
    private final AudiusClient audiusClient;
    private final ScreenManager screenManager;


    public TrackCard(String id, String title, String artist, AudiusClient audiusClient, ScreenManager screenManager, int index) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.audiusClient = audiusClient;
        this.screenManager = screenManager;

        setupLayout();
        initAnimations(index);
        initComponents();
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(1000, 85));
        setMaximumSize(new Dimension(1000, 85));
        setBorder(new EmptyBorder(10, 15, 10, 20));
        setToolTipText(title + " by " + artist);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hoverAlpha = 180;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverAlpha = 120;
                repaint();
            }
        });
    }

    private void initAnimations(int index) {
        enterTimer = new Timer(16, e -> {
            animOffset = Math.max(0, animOffset - 2f);
            animAlpha = Math.min(1f, animAlpha + 0.05f);
            if (animOffset == 0 && animAlpha >= 1f) enterTimer.stop();
            repaint();
        });
        enterTimer.setInitialDelay(index * 40);
        enterTimer.start();
    }

    private void initComponents() {
        add(getJPanel(title, artist), BorderLayout.CENTER);

        final JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        rightPanel.setOpaque(false);

        final Path existingPath = getDownloadedPath(title);
        final PrimaryButton actionBtn = new PrimaryButton(existingPath != null ? "PLAY" : "DOWNLOAD");

        if (existingPath != null) {
            setupPlayAction(actionBtn, existingPath);
        } else {
            setupDownloadAction(actionBtn);
        }

        rightPanel.add(actionBtn);
        add(rightPanel, BorderLayout.EAST);
    }

    private void setupPlayAction(PrimaryButton button, Path path) {
        button.addActionListener(e -> {
            screenManager.initScreen(GameScreen.class);
            GameScreen gameScreen = screenManager.getScreen(GameScreen.class);
            try {
                gameScreen.setupGamePanel(path);
                screenManager.showScreen(GameScreen.class);
                gameScreen.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void setupDownloadAction(PrimaryButton button) {
        button.addActionListener(e -> {
            button.setEnabled(false);
            button.setText("WAIT...");
            new SwingWorker<Path, Void>() {
                @Override protected Path doInBackground() throws Exception {
                    return audiusClient.downloadMusic(id, title).get();
                }
                @Override protected void done() {
                    try {
                        Path downloadedPath = get();
                        button.setText("PLAY");
                        button.setEnabled(true);
                        for (var al : button.getActionListeners()) button.removeActionListener(al);
                        setupPlayAction(button, downloadedPath);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        button.setText("ERROR");
                    }
                }
            }.execute();
        });
    }
    
    private Path getDownloadedPath(String title) {
        final String[] extensions = {".mp3", ".ogg", ".wav", ".flac"};
        final Path downloadDir = audiusClient.getDownloadDirectory();

        final String sanitizedTitle = title.replaceAll("[\\\\/:*?\"<>|]", "_");
        
        for (String ext : extensions) {
            final Path path = downloadDir.resolve(sanitizedTitle + ext);
            if (Files.exists(path)) {
                return path;
            }
        }
        return null;
    }

    private static JPanel getJPanel(String title, String artist) {
        final JPanel textPanel = new JPanel(new GridLayout(3, 1, 0, 0));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(4, 0, 4, 0));

        final JLabel titleLbl = new JLabel(title.length() > 70 ? title.substring(0, 67) + "..." : title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);

        final JLabel artistLbl = new JLabel(artist);
        artistLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        artistLbl.setForeground(new Color(180, 180, 180));

        textPanel.add(titleLbl);
        textPanel.add(artistLbl);
        return textPanel;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, animAlpha));
        g2.translate(0, (int) animOffset);
        super.paint(g2);
        g2.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphic2D(g2);
        g2.setColor(new Color(25, 25, 30, hoverAlpha));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.setColor(new Color(255, 255, 255, hoverAlpha > 120 ? 30 : 10));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.dispose();
    }
}