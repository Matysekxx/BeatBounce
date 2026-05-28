package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;
import cz.matysekxx.beatbounce.system.FileSystem;
import cz.matysekxx.beatbounce.util.ExceptionHandler;

import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * A panel that displays the user's local song library.
 * It allows users to view downloaded songs and add new local audio files.
 */
public class LibraryPanel extends BasePanel {
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
     * @param screenManager the screen manager used for navigation
     */
    public LibraryPanel(ScreenManager screenManager) {
        super();
        this.screenManager = screenManager;

        setOpaque(false);
        setLayout(new BorderLayout());

        final JPanel topBar = new JPanel(new BorderLayout()) {
            @Override
            public Insets getInsets() {
                return new Insets(UIScale.scale(25), UIScale.scale(40), UIScale.scale(15), UIScale.scale(40));
            }
        };
        topBar.setOpaque(false);

        final JLabel title = new JLabel("YOUR LIBRARY");
        title.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_36));
        title.setForeground(RenderUtils.cyan);
        topBar.add(title, BorderLayout.CENTER);

        final JButton addBtn = createAddButton();
        topBar.add(addBtn, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        final JScrollPane scrollPane = buildScrollPane(listPanel);
        add(scrollPane, BorderLayout.CENTER);

        loadLibrary();
    }

    /**
     * Builds a stylized scroll pane for the library content.
     *
     * @param content the panel to be scrolled
     * @return a configured JScrollPane
     */
    private JScrollPane buildScrollPane(JPanel content) {
        return SongSelectionPanel.buildScrollPane(content);
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
                final boolean hover = getModel().isRollover();

                g2.setColor(hover ? RenderUtils.cyan : new Color(255, 255, 255, 15));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UIScale.scale(12), UIScale.scale(12));

                if (!hover) {
                    g2.setColor(new Color(0, 255, 255, 60));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, UIScale.scale(12), UIScale.scale(12));
                }

                g2.setColor(hover ? Color.BLACK : Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(UIScale.scale(200), UIScale.scale(45));
            }
        };
        btn.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_15));
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
        final Dimension fileDim = new Dimension(UIScale.scale(800), UIScale.scale(600));
        fileChooser.setMinimumSize(fileDim);
        fileChooser.setPreferredSize(fileDim);
        fileChooser.setDialogTitle("Select Audio File");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Audio Files (*.mp3, *.wav, *.ogg, *.flac)", "mp3", "wav", "ogg", "flac"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            final Path source = fileChooser.getSelectedFile().toPath();
            final Path dest = FileSystem.getMusicDir().resolve(source.getFileName());
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
        listPanel.setBorder(new EmptyBorder(UIScale.scale(10), UIScale.scale(40), UIScale.scale(20), UIScale.scale(40)) {
            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(UIScale.scale(10), UIScale.scale(40), UIScale.scale(20), UIScale.scale(40));
            }
        });

        FileSystem.listMusicFiles().forEach(p -> {
            listPanel.add(new TrackRow(p, screenManager));
            listPanel.add(Box.createRigidArea(new Dimension(0, UIScale.scale(15))));
        });

        if (listPanel.getComponentCount() == 0) {
            final JLabel empty = new JLabel("No songs downloaded yet.");
            empty.setForeground(new Color(255, 255, 255, 80));
            empty.setFont(UIScale.scaleFont(RenderCache.SANS_ITALIC_22));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createRigidArea(new Dimension(0, UIScale.scale(100))));
            listPanel.add(empty);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }
}