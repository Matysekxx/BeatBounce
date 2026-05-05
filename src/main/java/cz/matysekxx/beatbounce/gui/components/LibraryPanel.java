package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.screen.GameScreen;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;
import cz.matysekxx.beatbounce.model.ScoreManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * A panel that displays the user's local song library.
 * It allows users to view downloaded songs and add new local audio files.
 */
public class LibraryPanel extends JPanel {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(LibraryPanel.class.getName());
    /**
     * Client used to interact with the Audius API.
     */
    private final AudiusClient audiusClient;
    /**
     * Manager used to switch between different screens.
     */
    private final ScreenManager screenManager;
    /**
     * Panel that contains the list of song rows.
     */
    private final JPanel listPanel;

    /**
     * Constructs a new LibraryPanel.
     *
     * @param audiusClient  the client used for audio operations
     * @param screenManager the screen manager used for navigation
     */
    public LibraryPanel(AudiusClient audiusClient, ScreenManager screenManager) {
        this.audiusClient = audiusClient;
        this.screenManager = screenManager;

        setOpaque(false);
        setLayout(new BorderLayout());

        final JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(25, 40, 15, 40));

        final JLabel title = new JLabel("YOUR LIBRARY");
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setForeground(RenderUtils.cyan);
        topBar.add(title, BorderLayout.CENTER);

        final JButton addBtn = createAddButton();
        topBar.add(addBtn, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        final JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        final JScrollBar vsb = scrollPane.getVerticalScrollBar();
        vsb.setUI(new ScrollBarUI());
        vsb.setOpaque(false);
        vsb.setBackground(new Color(0, 0, 0, 0));
        vsb.setPreferredSize(new Dimension(16, 0));
        vsb.setUnitIncrement(40);
        vsb.setBlockIncrement(120);

        add(scrollPane, BorderLayout.CENTER);

        loadLibrary();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2);
        g2.setPaint(new LinearGradientPaint(0, 0, getWidth(), getHeight(),
                new float[]{0f, 1f},
                new Color[]{new Color(15, 15, 35, 180), new Color(10, 10, 25, 100)}));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
        g2.setColor(new Color(0, 255, 255, 30));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
        g2.dispose();
        super.paintComponent(g);
    }

    /**
     * Creates the "ADD LOCAL SONG" button with custom rendering.
     *
     * @return a styled JButton for adding local songs
     */
    private JButton createAddButton() {
        final JButton btn = new JButton("+ ADD LOCAL SONG") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphics2D(g2);
                boolean hover = getModel().isRollover();

                g2.setColor(hover ? RenderUtils.cyan : new Color(255, 255, 255, 15));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                if (!hover) {
                    g2.setColor(new Color(0, 255, 255, 60));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                }

                g2.setColor(hover ? Color.BLACK : Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(200, 45));
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(_ -> addLocalSong());
        return btn;
    }

    /**
     * Opens a file chooser to select a local audio file and copies it to the download directory.
     * Supported formats: .mp3, .wav, .ogg, .flac.
     */
    private void addLocalSong() {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMinimumSize(new Dimension(800, 600));
        fileChooser.setPreferredSize(new Dimension(800, 600));
        fileChooser.setDialogTitle("Select Audio File");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Audio Files (*.mp3, *.wav, *.ogg, *.flac)", "mp3", "wav", "ogg", "flac"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            final Path source = fileChooser.getSelectedFile().toPath();
            final Path dest = audiusClient.getDownloadDirectory().resolve(source.getFileName());
            try {
                Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                loadLibrary();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Failed to copy custom song", ex);
                JOptionPane.showMessageDialog(this, "Failed to add song: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Loads the song library from the download directory.
     * It clears the current list and repopulates it with audio files (.mp3, .wav, .ogg, .flac)
     * found in the directory, sorted by last modified time.
     */
    public void loadLibrary() {
        listPanel.removeAll();
        listPanel.setBorder(new EmptyBorder(10, 40, 20, 40));
        final Path dir = audiusClient.getDownloadDirectory();

        if (Files.exists(dir)) {
            try (final Stream<Path> stream = Files.list(dir)) {
                stream.filter(p -> {
                    final String name = p.getFileName().toString().toLowerCase();
                    return name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".ogg") || name.endsWith(".flac");
                }).sorted((p1, p2) -> {
                    try {
                        return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
                    } catch (IOException e) {
                        return 0;
                    }
                }).forEach(p -> {
                    listPanel.add(new LocalTrackRow(p));
                    listPanel.add(Box.createRigidArea(new Dimension(0, 15)));
                });
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Failed to load library", e);
            }
        }

        if (listPanel.getComponentCount() == 0) {
            final JLabel empty = new JLabel("No songs downloaded yet.");
            empty.setForeground(new Color(255, 255, 255, 80));
            empty.setFont(new Font("SansSerif", Font.ITALIC, 22));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createRigidArea(new Dimension(0, 100)));
            listPanel.add(empty);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    /**
     * Switches the screen to the game screen and starts the game with the selected song.
     *
     * @param audioPath the path to the selected audio file
     * @param stars     the difficulty level represented by stars
     */
    private void launchGame(Path audioPath, int stars) {
        if (audioPath == null) return;
        try {
            screenManager.initScreen(GameScreen.class);
            final GameScreen gs = screenManager.getScreen(GameScreen.class);
            screenManager.showScreen(GameScreen.class);
            gs.setupGamePanel(audioPath, stars);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Failed to launch game", ex);
        }
    }

    /**
     * A UI component representing a single song in the library.
     */
    private class LocalTrackRow extends JPanel {
        /**
         * The difficulty level of the song (1-5).
         */
        private final int stars;
        /**
         * The display name of the song file (without extension).
         */
        private final String fileName;
        /**
         * The best score achieved by the user on this song.
         */
        private final String bestScore;
        /**
         * Whether the mouse is currently hovering over this row.
         */
        private boolean hovered = false;

        /**
         * Constructs a LocalTrackRow for the given song path.
         *
         * @param path the path to the audio file
         */
        public LocalTrackRow(Path path) {
            final String rawName = path.getFileName().toString();
            int dot = rawName.lastIndexOf('.');
            this.fileName = (dot > 0) ? rawName.substring(0, dot) : rawName;
            this.stars = 1 + (Math.abs(fileName.hashCode()) % 5);
            this.bestScore = String.valueOf(ScoreManager.getBestScore(fileName));

            this.setOpaque(false);
            this.setPreferredSize(new Dimension(0, 90));
            this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
            this.setCursor(new Cursor(Cursor.HAND_CURSOR));

            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    launchGame(path, stars);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            final Graphics2D g2 = (Graphics2D) g.create();
            RenderUtils.initGraphics2D(g2);
            final int w = getWidth();
            final int h = getHeight();

            if (hovered) {
                g2.setPaint(new LinearGradientPaint(0, 0, w, 0,
                        new float[]{0f, 1f},
                        new Color[]{new Color(0, 255, 255, 45), new Color(0, 255, 255, 5)}));
            } else {
                g2.setColor(new Color(255, 255, 255, 12));
            }
            g2.fillRoundRect(0, 0, w, h, 18, 18);

            g2.setColor(hovered ? new Color(0, 255, 255, 120) : new Color(255, 255, 255, 25));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);

            g2.setColor(new Color(255, 255, 255, 25));
            g2.fillRoundRect(18, 15, 60, 60, 12, 12);
            g2.setFont(new Font("SansSerif", Font.BOLD, 26));
            g2.setColor(hovered ? RenderUtils.cyan : Color.WHITE);
            final String icon = "🎵";
            FontMetrics fmIcon = g2.getFontMetrics();
            g2.drawString(icon, 18 + (60 - fmIcon.stringWidth(icon)) / 2, 15 + 42);

            g2.setFont(new Font("SansSerif", Font.BOLD, 22));
            g2.setColor(Color.WHITE);
            g2.drawString(fileName, 100, 42);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g2.setColor(new Color(180, 180, 200));
            final String subText = "Stars: " + "★".repeat(stars) + "☆".repeat(5 - stars);
            g2.drawString(subText, 100, 68);

            String scoreText = "BEST: " + bestScore;
            g2.setFont(new Font("Monospaced", Font.BOLD, 17));
            final FontMetrics fmScore = g2.getFontMetrics();
            final int scoreW = fmScore.stringWidth(scoreText) + 24;
            final int scoreX = w - 170 - scoreW;

            g2.setColor(new Color(255, 255, 255, 20));
            g2.fillRoundRect(scoreX, (h - 34) / 2, scoreW, 34, 10, 10);
            g2.setColor(RenderUtils.cyan);
            g2.drawString(scoreText, scoreX + 12, (h - 34) / 2 + 24);

            final int btnW = 140, btnH = 50;
            final int bx = w - 155;
            final int by = (h - btnH) / 2;

            g2.setColor(hovered ? RenderUtils.cyan : new Color(0, 200, 255));
            g2.fillRoundRect(bx, by, btnW, btnH, 14, 14);

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 20));
            String playTxt = "PLAY";
            final FontMetrics fmPlay = g2.getFontMetrics();
            g2.drawString(playTxt, bx + (btnW - fmPlay.stringWidth(playTxt)) / 2, by + 32);

            g2.dispose();
        }
    }
}