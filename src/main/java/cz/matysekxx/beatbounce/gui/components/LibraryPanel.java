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

public class LibraryPanel extends JPanel {
    private static final Logger LOG = Logger.getLogger(LibraryPanel.class.getName());
    private final AudiusClient audiusClient;
    private final ScreenManager screenManager;
    private final JPanel listPanel;

    public LibraryPanel(AudiusClient audiusClient, ScreenManager screenManager) {
        this.audiusClient = audiusClient;
        this.screenManager = screenManager;

        setOpaque(true);
        setBackground(new Color(10, 10, 26));
        setLayout(new BorderLayout());

        final JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(15, 30, 15, 30));

        final JLabel title = new JLabel("Your Library");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        topBar.add(title, BorderLayout.WEST);

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

    private JButton createAddButton() {
        final JButton btn = new JButton("+ ADD LOCAL SONG") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphics2D(g2);
                if (getModel().isRollover()) {
                    g2.setColor(RenderUtils.cyan);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new Color(10, 10, 26));
                } else {
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    g2.setColor(Color.WHITE);
                }
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, getHeight() / 2 + 5);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(160, 36));
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(_ -> addLocalSong());
        return btn;
    }

    private void addLocalSong() {
        final JFileChooser fileChooser = new JFileChooser();
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

    public void loadLibrary() {
        listPanel.removeAll();
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
                    listPanel.add(Box.createRigidArea(new Dimension(0, 0)));
                });
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Failed to load library", e);
            }
        }

        if (listPanel.getComponentCount() == 0) {
            final JLabel empty = new JLabel("No songs downloaded yet.");
            empty.setForeground(Color.GRAY);
            empty.setFont(new Font("SansSerif", Font.PLAIN, 18));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createRigidArea(new Dimension(0, 50)));
            listPanel.add(empty);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

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

    private class LocalTrackRow extends JPanel {
        private final Path path;
        private final int stars;
        private boolean hovered = false;

        public LocalTrackRow(Path path) {
            this.path = path;
            String fileName = path.getFileName().toString();
            int dot = fileName.lastIndexOf('.');
            if (dot > 0) fileName = fileName.substring(0, dot);
            this.stars = 1 + (Math.abs(fileName.hashCode()) % 5);

            this.setOpaque(false);
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
                    final int btnW = 100, btnH = 32;
                    final int bx = getWidth() - 20 - btnW;
                    final int by = (getHeight() - btnH) / 2;
                    if (new Rectangle(bx, by, btnW, btnH).contains(e.getPoint())) {
                        launchGame(path, stars);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RenderUtils.initGraphics2D(g2);
            final int w = getWidth();
            final int h = getHeight();

            if (hovered) {
                g2.setColor(new Color(255, 255, 255, (int) (0.04 * 255)));
                g2.fillRect(0, 0, w, h);
            }

            g2.setColor(new Color(255, 255, 255, 15));
            g2.drawLine(0, h - 1, w, h - 1);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.setColor(RenderUtils.cyan);
            g2.drawString("🎵", 20, 38);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            String fileName = path.getFileName().toString();

            final int dot = fileName.lastIndexOf('.');
            if (dot > 0) fileName = fileName.substring(0, dot);
            g2.drawString(fileName, 50, 38);

            final int rightX = w - 140;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            
            final String info = String.format("Best: %d", ScoreManager.getBestScore(fileName));
            final FontMetrics fm = g2.getFontMetrics();
            g2.setColor(new Color(200, 200, 200));
            g2.drawString(info, rightX - fm.stringWidth(info), 38);

            final String starsStr = "★".repeat(stars) + "☆".repeat(5 - stars);
            g2.setColor(RenderUtils.cyan);
            g2.drawString(starsStr, rightX - fm.stringWidth(info) - fm.stringWidth(starsStr) - 15, 38);

            final int btnW = 100, btnH = 32;
            final int bx = w - 20 - btnW;
            final int by = (h - btnH) >> 1;

            final Color acc = RenderUtils.purple;
            g2.setColor(acc);
            g2.fillRoundRect(bx, by, btnW, btnH, 8, 8);
            g2.setColor(new Color(10, 10, 26));
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));

            final String txt = "PLAY";
            final FontMetrics bfm = g2.getFontMetrics();
            g2.drawString(txt, bx + (btnW - bfm.stringWidth(txt)) / 2, by + 20);
            g2.dispose();
        }
    }
}