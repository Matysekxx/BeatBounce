package cz.matysekxx.beatbounce.gui.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.configuration.Settings;
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

/**
 * A panel that allows users to browse and select songs from the Audius API.
 * It includes search functionality and genre filters.
 */
public class SongSelectionPanel extends JPanel implements Runnable {
    private static final Logger LOG = Logger.getLogger(SongSelectionPanel.class.getName());

    /**
     * Client for interacting with the Audius API.
     */
    private final AudiusClient audiusClient;

    /**
     * JSON mapper for parsing API responses.
     */
    private final ObjectMapper objectMapper;

    /**
     * Manager for handling screen transitions.
     */
    private final ScreenManager screenManager;

    /**
     * Inner panel containing the list of track rows.
     */
    private final JPanel songListPanel;

    /**
     * Full list of tracks fetched from the API.
     */
    private final List<TrackData> allTracks = new ArrayList<>();

    /**
     * Flag indicating if the animation thread is running.
     */
    private boolean running = false;

    /**
     * Thread responsible for running UI animations.
     */
    private Thread animatorThread;

    /**
     * List of tracks after applying search filters.
     */
    private List<TrackData> filteredTracks = new ArrayList<>();

    /**
     * The track currently selected by the user.
     */
    private TrackData selectedTrack = null;

    /**
     * Current search query string.
     */
    private String searchQuery = "";

    /**
     * Currently active genre filter.
     */
    private String activeGenre = "All-Time";

    /**
     * Search input field.
     */
    private JTextField searchField;

    /**
     * Constructs a new SongSelectionPanel.
     *
     * @param audiusClient  the client used for API requests
     * @param objectMapper  the mapper used for JSON parsing
     * @param screenManager the screen manager used for navigation
     */
    public SongSelectionPanel(AudiusClient audiusClient, ObjectMapper objectMapper, ScreenManager screenManager) {
        this.audiusClient = audiusClient;
        this.objectMapper = objectMapper;
        this.screenManager = screenManager;

        setOpaque(false);
        setLayout(new BorderLayout());

        final JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        songListPanel = new JPanel();
        songListPanel.setLayout(new BoxLayout(songListPanel, BoxLayout.Y_AXIS));
        songListPanel.setOpaque(false);

        final JScrollPane scrollPane = buildScrollPane(songListPanel);
        add(scrollPane, BorderLayout.CENTER);

        loadTracks("allTime", null);
    }

    /**
     * Configures a custom JScrollPane with stylized scrollbars.
     *
     * @param content The panel to be wrapped in the scroll pane.
     * @return The configured JScrollPane.
     */
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

    /**
     * Creates the top navigation bar containing the search field and genre filters.
     *
     * @return The top bar JPanel.
     */
    private JPanel createTopBar() {
        final JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setPreferredSize(new Dimension(0, 56));
        topBar.setBorder(new EmptyBorder(0, 20, 0, 20));

        final JPanel searchContainer = new JPanel(new GridBagLayout());
        searchContainer.setOpaque(false);
        searchField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphics2D(g2);
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                if (hasFocus()) {
                    g2.setColor(new Color(0, 255, 255, 60));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                }
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

    /**
     * Creates a button acting as a genre selection "chip".
     *
     * @param name The name of the genre.
     * @return The stylized JButton.
     */
    private JButton createGenreChip(String name) {
        final JButton btn = new JButton(name) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphics2D(g2);
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
        btn.addActionListener(_ -> {
            activeGenre = name;
            final String time = name.equals("Trending") ? "month" : "allTime";
            String genre = (name.equals("All-Time") || name.equals("Trending")) ? null : name;
            if (name.equals("Hip-Hop")) genre = "Hip-Hop/Rap";
            loadTracks(time, genre);
            repaint();
        });
        return btn;
    }

    /**
     * Asynchronously loads tracks from the Audius API based on time and genre filters.
     *
     * @param time  The timeframe (e.g., "allTime", "month").
     * @param genre The genre string, or null for all genres.
     */
    private void loadTracks(String time, String genre) {
        final CompletableFuture<String> future;
        if (genre != null) future = audiusClient.getTrendingTracksByGenre(genre, time);
        else future = audiusClient.getTrendingTracks(time);
        future.thenAccept(json -> SwingUtilities.invokeLater(() -> loadSongs(json)));
    }

    /**
     * Parses the JSON response from Audius and populates the track list.
     *
     * @param json The JSON string response from the API.
     */
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
        } catch (Exception exception) {
            LOG.log(Level.WARNING, "Failed to load tracks", exception);
        }
    }

    /**
     * Filters the internal track list based on the current search query and updates the UI.
     */
    private void filterTracks() {
        filteredTracks = allTracks.stream()
                .filter(t -> searchQuery.isEmpty() ||
                        t.title.toLowerCase().contains(searchQuery.toLowerCase()) ||
                        t.artist.toLowerCase().contains(searchQuery.toLowerCase()))
                .collect(Collectors.toList());
        updateSongList();
    }

    /**
     * Rebuilds the UI components in the song list panel based on filtered tracks.
     */
    private void updateSongList() {
        songListPanel.removeAll();
        for (TrackData track : filteredTracks) {
            songListPanel.add(new TrackRow(track, audiusClient, screenManager, this::selectTrack));
            songListPanel.add(Box.createRigidArea(new Dimension(0, 0)));
        }
        songListPanel.revalidate();
        songListPanel.repaint();
    }

    /**
     * Marks a track as selected and handles expansion state for animations.
     *
     * @param track The track to select.
     */
    private void selectTrack(TrackData track) {
        if (selectedTrack != null) selectedTrack.expanded = false;
        selectedTrack = track;
        if (selectedTrack != null) selectedTrack.expanded = true;
    }

    /**
     * Starts the animation thread for the track expansion effects.
     */
    public void startAnimations() {
        if (!running) {
            running = true;
            animatorThread = new Thread(this);
            animatorThread.start();
        }
    }

    /**
     * Stops the animation thread.
     */
    public void stopAnimations() {
        running = false;
        if (animatorThread != null) {
            animatorThread.interrupt();
            animatorThread = null;
        }
    }

    /**
     * The main animation loop for track expansion and UI updates.
     */
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            final float dt = (now - lastTime) / 1_000_000_000f;
            lastTime = now;

            boolean needsRevalidate = false;
            if (allTracks != null) {
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
            }

            if (needsRevalidate) {
                SwingUtilities.invokeLater(songListPanel::revalidate);
            }

            repaint();
            try {
                final long frameTimeMs = (long) (1000.0 / Settings.targetFps);
                Thread.sleep(frameTimeMs);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Paints the background and border of the song selection panel with a gradient and rounded corners.
     *
     * @param g the Graphics object to protect
     */
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
}