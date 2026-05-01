package cz.matysekxx.beatbounce.gui.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
    private boolean running = false;
    private Thread animatorThread;
    private final List<TrackData> allTracks = new ArrayList<>();
    private List<TrackData> filteredTracks = new ArrayList<>();
    private TrackData selectedTrack = null;
    private String searchQuery = "";
    private String activeGenre = "All-Time";
    private JTextField searchField;

    public SongSelectionPanel(AudiusClient audiusClient, ObjectMapper objectMapper, ScreenManager screenManager) {
        this.audiusClient = audiusClient;
        this.objectMapper = objectMapper;
        this.screenManager = screenManager;

        setOpaque(true);
        setBackground(new Color(10, 10, 26));
        setLayout(new BorderLayout());

        for (int i = 0; i < 30; i++) particles.add(new Particle(1920, 1080));

        final JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        songListPanel = new JPanel();
        songListPanel.setLayout(new BoxLayout(songListPanel, BoxLayout.Y_AXIS));
        songListPanel.setOpaque(false);

        final JScrollPane scrollPane = buildScrollPane(songListPanel);
        add(scrollPane, BorderLayout.CENTER);

        loadTracks("allTime", null);
    }

    private static JScrollPane buildScrollPane(JPanel content) {
        final JScrollPane sp = new JScrollPane(content);

        final JScrollBar vsb = sp.getVerticalScrollBar();
        vsb.setUI(new ScrollBarUI());
        vsb.setOpaque(false);
        vsb.setBackground(new Color(0, 0, 0, 0));
        vsb.setPreferredSize(new Dimension(16, 0));
        vsb.setUnitIncrement(40);
        vsb.setBlockIncrement(120);

        final JScrollBar hsb = sp.getHorizontalScrollBar();
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
        final JPanel topBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(10, 10, 26));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topBar.setPreferredSize(new Dimension(0, 56));
        topBar.setBorder(new EmptyBorder(0, 20, 0, 20));

        final JPanel searchContainer = new JPanel(new GridBagLayout());
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
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchQuery = searchField.getText();
                    audiusClient.searchTracks(searchQuery).thenAccept(
                            json -> SwingUtilities.invokeLater(() -> loadSongs(json))
                    );
                }
            }
        });
        searchContainer.add(searchField);
        topBar.add(searchContainer, BorderLayout.CENTER);

        final JPanel genrePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        genrePanel.setOpaque(false);
        final String[] genres = {"All-Time", "Trending", "Electronic", "Hip-Hop", "Pop", "World"};
        for (String g : genres) {
            genrePanel.add(createGenreChip(g));
        }
        topBar.add(genrePanel, BorderLayout.EAST);

        return topBar;
    }

    private JButton createGenreChip(String name) {
        final JButton btn = new JButton(name) {
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
                final FontMetrics fm = g2.getFontMetrics();
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
            final String time = name.equals("Trending") ? "month" : "allTime";
            String genre = (name.equals("All-Time") || name.equals("Trending")) ? null : name;
            if (name.equals("Hip-Hop")) genre = "Hip-Hop/Rap";
            loadTracks(time, genre);
            repaint();
        });
        return btn;
    }

    private void loadTracks(String time, String genre) {
        final CompletableFuture<String> future;
        if (genre != null) future = audiusClient.getTrendingTracksByGenre(genre, time);
        else future = audiusClient.getTrendingTracks(time);
        future.thenAccept(json -> SwingUtilities.invokeLater(() -> loadSongs(json)));
    }

    private void loadSongs(String json) {
        try {
            final JsonNode root = objectMapper.readTree(json);
            allTracks.clear();
            for (JsonNode node : root.path("data")) {
                allTracks.add(new TrackData(node));
            }
            filterTracks();
            if (!filteredTracks.isEmpty()) {
                selectTrack(filteredTracks.getFirst());
            }
        } catch (Exception exception) { LOG.log(Level.WARNING, "Failed to load tracks", exception); }
    }

    private void filterTracks() {
        filteredTracks = allTracks.stream()
                .filter(t -> searchQuery.isEmpty() ||
                        t.title.toLowerCase().contains(searchQuery.toLowerCase()) ||
                        t.artist.toLowerCase().contains(searchQuery.toLowerCase()))
                .collect(Collectors.toList());
        updateSongList();
    }

    private void updateSongList() {
        songListPanel.removeAll();
        for (TrackData track : filteredTracks) {
            songListPanel.add(new TrackRow(track, audiusClient, screenManager, this::selectTrack));
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
        long lastTime = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            final float dt = (now - lastTime) / 1_000_000_000f;
            lastTime = now;

            boolean needsRevalidate = false;
            for (TrackData t : allTracks) {
                final float expansionTime = 0.2f;
                if (t.expanded && t.expansion < 1f) {
                    t.expansion = Math.min(1f, t.expansion + dt / expansionTime);
                    needsRevalidate = true;
                } else if (!t.expanded && t.expansion > 0f) {
                    t.expansion = Math.max(0f, t.expansion - dt / expansionTime);
                    needsRevalidate = true;
                }
            }

            if (needsRevalidate) { SwingUtilities.invokeLater(songListPanel::revalidate); }

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
}