package cz.matysekxx.beatbounce.gui.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.screen.GameScreen;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;
import cz.matysekxx.beatbounce.model.ScoreManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SongSelectionPanel extends JPanel implements Runnable {

    private static final Logger LOG = Logger.getLogger(SongSelectionPanel.class.getName());

    private final AudiusClient audiusClient;
    private final ObjectMapper objectMapper;
    private final ScreenManager screenManager;
    private final List<Particle> particles = new ArrayList<>();
    private final JPanel songListPanel;
    private final JScrollPane scrollPane;
    private boolean running = false;
    private Thread animatorThread;
    private long lastTime;
    private List<TrackData> allTracks = new ArrayList<>();
    private List<TrackData> filteredTracks = new ArrayList<>();
    private TrackData selectedTrack = null;
    private String searchQuery = "";
    private String activeGenre = "All-Time";
    private JTextField searchField;

    private float expansionTime = 0.2f;

    public SongSelectionPanel(AudiusClient audiusClient, ObjectMapper objectMapper, ScreenManager screenManager) {
        this.audiusClient = audiusClient;
        this.objectMapper = objectMapper;
        this.screenManager = screenManager;

        setOpaque(true);
        setBackground(new Color(10, 10, 26));
        setLayout(new BorderLayout());

        for (int i = 0; i < 30; i++) particles.add(new Particle(1920, 1080));

        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        songListPanel = new JPanel();
        songListPanel.setLayout(new BoxLayout(songListPanel, BoxLayout.Y_AXIS));
        songListPanel.setOpaque(false);

        scrollPane = buildScrollPane(songListPanel);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomBar = createBottomBar();
        add(bottomBar, BorderLayout.SOUTH);

        loadTracks("allTime", null);
    }


    private static JScrollPane buildScrollPane(JPanel content) {
        JScrollPane sp = new JScrollPane(content);

        JScrollBar vsb = sp.getVerticalScrollBar();
        vsb.setUI(new ScrollBarUI());
        vsb.setOpaque(false);
        vsb.setBackground(new Color(0, 0, 0, 0));
        vsb.setPreferredSize(new Dimension(16, 0));
        vsb.setUnitIncrement(40);
        vsb.setBlockIncrement(120);

        JScrollBar hsb = sp.getHorizontalScrollBar();
        hsb.setUI(new ScrollBarUI());
        hsb.setOpaque(false);
        hsb.setPreferredSize(new Dimension(0, 0));

        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getViewport().setBackground(new Color(0, 0, 0, 0));
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(10, 10, 26));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topBar.setPreferredSize(new Dimension(0, 56));
        topBar.setBorder(new EmptyBorder(0, 20, 0, 20));

        JPanel searchContainer = new JPanel(new GridBagLayout());
        searchContainer.setOpaque(false);
        searchField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphic2D(g2);
                g2.setColor(new Color(20, 20, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        searchField.setOpaque(false);
        searchField.setBorder(new EmptyBorder(0, 15, 0, 15));
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setPreferredSize(new Dimension(500, 32));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchQuery = searchField.getText();
                filterTracks();
            }
        });
        searchContainer.add(searchField);
        topBar.add(searchContainer, BorderLayout.CENTER);

        JPanel genrePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        genrePanel.setOpaque(false);
        String[] genres = {"All-Time", "Trending", "Electronic", "Hip-Hop", "Pop", "World"};
        for (String g : genres) {
            genrePanel.add(createGenreChip(g));
        }
        topBar.add(genrePanel, BorderLayout.EAST);

        return topBar;
    }

    private JButton createGenreChip(String name) {
        JButton btn = new JButton(name) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphic2D(g2);
                boolean active = activeGenre.equals(getText());
                if (active) {
                    g2.setColor(RenderUtils.cyan);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                    g2.setColor(new Color(10, 10, 26));
                } else {
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                    g2.setColor(new Color(200, 200, 200, 150));
                }
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, getHeight() / 2 + 5);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(90, 28));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            activeGenre = name;
            String time = name.equals("Trending") ? "month" : "allTime";
            String genre = (name.equals("All-Time") || name.equals("Trending")) ? null : name;
            if (name.equals("Hip-Hop")) genre = "Hip-Hop/Rap";
            loadTracks(time, genre);
            repaint();
        });
        return btn;
    }

    private JPanel createBottomBar() {
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(10, 10, 26, 242));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0, 255, 255, 51));
                g2.drawLine(0, 0, getWidth(), 0);
                g2.dispose();
            }
        };
        bottomBar.setPreferredSize(new Dimension(0, 52));
        String[] navs = {"SETTINGS", "SHOP", "SKINS", "ACHIEVEMENTS"};
        for (String n : navs) {
            bottomBar.add(createNavButton(n));
        }
        return bottomBar;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphic2D(g2);
                Color c = getModel().isRollover() ? RenderUtils.cyan : new Color(0, 255, 255, 128);
                g2.setColor(c);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 0, fm.getAscent());
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadTracks(String time, String genre) {
        CompletableFuture<String> future = (genre != null)
                ? audiusClient.getTrendingTracksByGenre(genre, time)
                : audiusClient.getTrendingTracks(time);

        future.thenAccept(json -> SwingUtilities.invokeLater(() -> {
            try {
                JsonNode root = objectMapper.readTree(json);
                allTracks.clear();
                for (JsonNode node : root.path("data")) {
                    allTracks.add(new TrackData(node));
                }
                filterTracks();
                if (!filteredTracks.isEmpty()) {
                    selectTrack(filteredTracks.get(0));
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Failed to load tracks", e);
            }
        }));
    }

    private void filterTracks() {
        filteredTracks = allTracks.stream()
                .filter(t -> searchQuery.isEmpty() || t.title.toLowerCase().contains(searchQuery.toLowerCase()) || t.artist.toLowerCase().contains(searchQuery.toLowerCase()))
                .collect(Collectors.toList());
        updateSongList();
    }

    private void updateSongList() {
        songListPanel.removeAll();
        for (TrackData track : filteredTracks) {
            songListPanel.add(new TrackRow(track));
            songListPanel.add(Box.createRigidArea(new Dimension(0, 0)));
        }
        songListPanel.revalidate();
        songListPanel.repaint();
    }

    private void selectTrack(TrackData track) {
        if (selectedTrack != null) selectedTrack.expanded = false;
        selectedTrack = track;
        if (selectedTrack != null) selectedTrack.expanded = true;
    }

    public void startAnimations() {
        if (!running) {
            running = true;
            animatorThread = new Thread(this);
            animatorThread.start();
        }
    }

    public void stopAnimations() {
        running = false;
        if (animatorThread != null) {
            animatorThread.interrupt();
            animatorThread = null;
        }
    }

    @Override
    public void run() {
        lastTime = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            float dt = (now - lastTime) / 1_000_000_000f;
            lastTime = now;

            boolean needsRevalidate = false;
            for (TrackData t : allTracks) {
                if (t.expanded && t.expansion < 1f) {
                    t.expansion = Math.min(1f, t.expansion + dt / expansionTime);
                    needsRevalidate = true;
                } else if (!t.expanded && t.expansion > 0f) {
                    t.expansion = Math.max(0f, t.expansion - dt / expansionTime);
                    needsRevalidate = true;
                }
            }

            if (needsRevalidate) {
                SwingUtilities.invokeLater(() -> {
                    songListPanel.revalidate();
                });
            }

            Particle.updateAll(particles, dt, getWidth() > 0 ? getWidth() : 1920, getHeight() > 0 ? getHeight() : 1080);
            repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        RenderUtils.initGraphic2D(g2d);
        g2d.setColor(new Color(10, 10, 26));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        Particle.drawAll(g2d, particles);
        g2d.dispose();
    }

    private class TrackData {
        String id, title, artist;
        int bpm, stars, best;
        String duration = "3:42";
        boolean expanded = false;
        float expansion = 0f;
        boolean downloading = false;
        float downloadProgress = 0f;

        TrackData(JsonNode node) {
            this.id = node.path("id").asText();
            this.title = node.path("title").asText();
            this.artist = node.path("user").path("name").asText("Unknown Artist");
            int hash = id.hashCode();
            this.bpm = 100 + (Math.abs(hash) % 80);
            this.stars = 1 + (Math.abs(hash) % 5);
            this.best = ScoreManager.getBestScore(title);
        }

        Color getAccent() {
            if (bpm >= 150) return new Color(255, 0, 255);
            if (bpm >= 120) return new Color(0, 255, 255);
            return new Color(155, 48, 255);
        }

        boolean isDownloaded() {
            return findDownloadedPath(audiusClient) != null;
        }

        Path findDownloadedPath(AudiusClient client) {
            final String[] exts = {".mp3", ".ogg", ".wav", ".flac"};
            final Path dir = client.getDownloadDirectory();
            final String sanitized = title.replaceAll("[\\\\/:*?\"<>|]", "_");
            for (String ext : exts) {
                Path p = dir.resolve(sanitized + ext);
                if (Files.exists(p)) return p;
            }
            return null;
        }
    }

    private class TrackRow extends JPanel {
        TrackData data;
        boolean hovered = false;

        {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (data.expansion > 0.8f) {
                        int btnW = 110, btnH = 32;
                        int bx = getWidth() - 20 - btnW;
                        int by = 60;
                        Rectangle playRect = new Rectangle(bx, by, btnW, btnH);
                        if (playRect.contains(e.getPoint()) && !data.downloading) {
                            handlePlay();
                        }
                    }
                }
            });
        }

        TrackRow(TrackData data) {
            this.data = data;
            setOpaque(false);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    selectTrack(data);
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(getWidth(), 64 + (int) (data.expansion * 44));
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RenderUtils.initGraphic2D(g2);
            int w = getWidth();
            int h = getHeight();

            if (data.expanded) {
                Color acc = data.getAccent();
                g2.setColor(new Color(acc.getRed(), acc.getGreen(), acc.getBlue(), (int) (0.15 * 255)));
                g2.fillRect(0, 0, w, h);
                g2.setColor(acc);
                g2.fillRect(0, 0, 3, h);
            } else if (hovered) {
                g2.setColor(new Color(255, 255, 255, (int) (0.04 * 255)));
                g2.fillRect(0, 0, w, h);
            }

            g2.setColor(new Color(255, 255, 255, 15));
            g2.drawLine(0, h - 1, w, h - 1);

            boolean downloaded = data.isDownloaded();
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.setColor(downloaded ? RenderUtils.cyan : Color.GRAY);
            g2.drawString(downloaded ? "\u2713" : "\u2601", 20, 38);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.drawString(data.title, 50, 28);
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString(data.artist, 50, 48);

            int rightX = w - 20;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            String info = String.format("%d BPM  \u2022  %s  \u2022  Best: %d", data.bpm, data.duration, data.best);
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(new Color(200, 200, 200));
            g2.drawString(info, rightX - fm.stringWidth(info), 38);

            String stars = "\u2605".repeat(data.stars) + "\u2606".repeat(5 - data.stars);
            g2.setColor(RenderUtils.cyan);
            g2.drawString(stars, rightX - fm.stringWidth(info) - g2.getFontMetrics().stringWidth(stars) - 15, 38);

            if (data.expansion > 0.5f) {
                int btnW = 110, btnH = 32;
                int bx = w - 20 - btnW;
                int by = 60;
                g2.setColor(data.getAccent());
                g2.fillRoundRect(bx, by, btnW, btnH, 8, 8);
                g2.setColor(new Color(10, 10, 26));
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                String txt = data.downloading ? "..." : "PLAY";
                FontMetrics bfm = g2.getFontMetrics();
                g2.drawString(txt, bx + (btnW - bfm.stringWidth(txt)) / 2, by + 20);

                if (data.downloading) {
                    g2.setColor(new Color(255, 255, 255, 50));
                    g2.drawRect(bx, by + btnH + 5, btnW, 4);
                    g2.setColor(Color.WHITE);
                    g2.fillRect(bx, by + btnH + 5, (int) (btnW * data.downloadProgress), 4);
                }

                Rectangle playRect = new Rectangle(bx, by, btnW, btnH);
                Point mouse = getMousePosition();
                if (mouse != null && playRect.contains(mouse) && !data.downloading) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                    if (MouseInfo.getPointerInfo().getDevice().getType() == 0) {
                    }
                }
            }

            g2.dispose();
        }

        private void handlePlay() {
            if (data.isDownloaded()) {
                launchGame(data.findDownloadedPath(audiusClient));
            } else {
                data.downloading = true;
                data.downloadProgress = 0.1f;
                audiusClient.downloadMusic(data.id, data.title).thenAccept(downloadedPath -> {
                    data.downloading = false;
                    data.downloadProgress = 1f;
                    SwingUtilities.invokeLater(() -> launchGame(downloadedPath));
                }).exceptionally(ex -> {
                    data.downloading = false;
                    LOG.log(Level.WARNING, "Download failed for " + data.title, ex);
                    return null;
                });
            }
        }

        private void launchGame(Path audioPath) {
            if (audioPath == null) return;
            try {
                screenManager.initScreen(GameScreen.class);
                GameScreen gs = screenManager.getScreen(GameScreen.class);
                gs.setupGamePanel(audioPath);
                screenManager.showScreen(GameScreen.class);
                gs.start();
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Failed to launch game for " + data.title, ex);
            }
        }
    }
}