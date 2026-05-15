package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import cz.matysekxx.beatbounce.util.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Stream;

/**
 * A panel that displays the user's local song library.
 * It allows users to view downloaded songs and add new local audio files.
 */
public class LibraryPanel extends JPanel {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(LibraryPanel.class);
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
        title.setFont(RenderCache.SANS_BOLD_36);
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
        RenderUtils.drawMenuBackground(g2, getWidth(), getHeight());
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
        btn.setFont(RenderCache.SANS_BOLD_15);
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
                ExceptionHandler.handle("Failed to copy custom song", ex);
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
                    listPanel.add(new LocalTrackRow(p, screenManager));
                    listPanel.add(Box.createRigidArea(new Dimension(0, 15)));
                });
            } catch (IOException e) {
                ExceptionHandler.handle("Failed to load library", e);
            }
        }

        if (listPanel.getComponentCount() == 0) {
            final JLabel empty = new JLabel("No songs downloaded yet.");
            empty.setForeground(new Color(255, 255, 255, 80));
            empty.setFont(RenderCache.SANS_ITALIC_22);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createRigidArea(new Dimension(0, 100)));
            listPanel.add(empty);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }
}