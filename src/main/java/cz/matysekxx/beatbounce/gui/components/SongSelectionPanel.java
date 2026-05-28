package cz.matysekxx.beatbounce.gui.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;
import cz.matysekxx.beatbounce.model.audio.AudioManager;
import cz.matysekxx.beatbounce.util.ExceptionHandler;
import cz.matysekxx.beatbounce.util.Time;
import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * A panel that allows users to browse and select songs from the Audius API.
 * It includes search functionality and genre filters.
 */
public class SongSelectionPanel extends BasePanel implements Runnable {

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
    private final CopyOnWriteArrayList<TrackData> allTracks = new CopyOnWriteArrayList<>();

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
        super();
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

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                cachedW = -1;
                cachedH = -1;
                revalidate();
                songListPanel.revalidate();
            }
        });

        loadTracks("allTime", null);
    }

    /**
     * Configures a custom JScrollPane with stylized scrollbars.
     *
     * @param content The panel to be wrapped in the scroll pane.
     * @return The configured JScrollPane.
     */
    public static JScrollPane buildScrollPane(JPanel content) {
        final JScrollPane sp = new JScrollPane(content);

        final JScrollBar vsb = sp.getVerticalScrollBar();
        vsb.setUI(new ScrollBarUI());
        vsb.setOpaque(false);
        vsb.setBackground(new Color(0, 0, 0, 0));
        vsb.setUnitIncrement(UIScale.scale(40));
        vsb.setBlockIncrement(UIScale.scale(120));

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
     * Draws the background of the panel using a stylized menu background.
     *
     * @param g2d the graphics context
     * @param w   the width of the panel
     * @param h   the height of the panel
     */
    @Override
    protected void drawBackground(Graphics2D g2d, int w, int h) {
        RenderUtils.drawMenuBackground(g2d, w, h);
    }

    /**
     * Creates the top navigation bar containing the search field and genre filters.
     *
     * @return The top bar JPanel.
     */
    private JPanel createTopBar() {
        final JPanel topBar = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, UIScale.scale(56));
            }

            @Override
            public Insets getInsets() {
                return new Insets(0, UIScale.scale(20), 0, UIScale.scale(20));
            }
        };
        topBar.setOpaque(false);

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

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(UIScale.scale(500), UIScale.scale(32));
            }

            @Override
            public Insets getInsets() {
                return new Insets(0, UIScale.scale(15), 0, UIScale.scale(15));
            }
        };
        searchField.setOpaque(false);
        searchField.setBorder(null);
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
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
        topBar.add(searchContainer, BorderLayout.WEST);

        final JPanel genrePanel = getJPanel();
        final String[] genres = {"All-Time", "Trending", "Electronic", "Hip-Hop", "Pop", "World"};
        for (String g : genres) genrePanel.add(createGenreChip(g));
        topBar.add(genrePanel, BorderLayout.CENTER);

        return topBar;
    }

    /**
     * Creates and configures the genre selection panel with a right-aligned flow layout.
     *
     * @return the configured JPanel for genres
     */
    private JPanel getJPanel() {
        final JPanel genrePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UIScale.scale(8), UIScale.scale(12))) {
            @Override
            public void doLayout() {
                setFlowLayoutGap(this);
                super.doLayout();
            }

            private void setFlowLayoutGap(JPanel p) {
                final LayoutManager lm = p.getLayout();
                if (lm instanceof FlowLayout fl) {
                    fl.setHgap(UIScale.scale(8));
                    fl.setVgap(UIScale.scale(12));
                }
            }
        };
        genrePanel.setOpaque(false);
        return genrePanel;
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
                final boolean active = activeGenre.equals(getText());
                if (active) {
                    g2.setColor(RenderUtils.cyan);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                    g2.setColor(new Color(10, 10, 26));
                } else {
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                    g2.setColor(new Color(200, 200, 200, 150));
                }
                g2.setFont(UIScale.scaleFont(RenderCache.SANS_PLAIN_20));
                final FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, getHeight() / 2 + UIScale.scale(6));
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(UIScale.scale(100), UIScale.scale(38));
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(_ -> {
            AudioManager.playSFX("/click-sound.mp3");
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
            ExceptionHandler.handle("Failed to load tracks", exception);
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
        final long optimalTimeNanos = 1_000_000_000L / Settings.targetFps;
        long lastTime = System.nanoTime();
        while (running) {
            final long now = System.nanoTime();
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

                    if (t.starting && t.startingProgress < 1f)
                        t.startingProgress = Math.min(1f, t.startingProgress + dt * 2.5f);
                }
            }

            if (needsRevalidate) SwingUtilities.invokeLater(songListPanel::revalidate);

            repaint();
            if (Settings.vsync) Toolkit.getDefaultToolkit().sync();

            Time.delay(optimalTimeNanos, now);
        }
    }
}