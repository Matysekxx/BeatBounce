package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.screen.GameScreen;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;
import cz.matysekxx.beatbounce.model.score.ScoreManager;
import cz.matysekxx.beatbounce.util.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * A UI component representing a single track in the song selection list.
 * It handles rendering track info, selection expansion, and initiating downloads/play.
 */
public class TrackRow extends JPanel {

    /**
     * Data model for this track row.
     */
    private final TrackData data;

    /**
     * Client for handling track downloads.
     */
    private final AudiusClient audiusClient;

    /**
     * Manager for screen navigation.
     */
    private final ScreenManager screenManager;

    /**
     * Whether the mouse is currently hovering over this row.
     */
    private boolean hovered = false;

    /**
     * Constructs a new TrackRow.
     *
     * @param data          the track data to display
     * @param audiusClient  the Audius client for downloads
     * @param screenManager the screen manager for navigation
     * @param onSelect      a callback for when the track is selected
     */
    public TrackRow(TrackData data, AudiusClient audiusClient, ScreenManager screenManager, Consumer<TrackData> onSelect) {
        this.data = data;
        this.audiusClient = audiusClient;
        this.screenManager = screenManager;

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
                if (onSelect != null) {
                    onSelect.accept(data);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (data.expansion > 0.8f) {
                    final int btnW = UIScale.scale(110), btnH = UIScale.scale(32);
                    final int bx = getWidth() - UIScale.scale(20) - btnW;
                    final int by = UIScale.scale(60);
                    final Rectangle playRect = new Rectangle(bx, by, btnW, btnH);
                    if (playRect.contains(e.getPoint()) && !data.downloading) {
                        handlePlay();
                    }
                }
            }
        });
    }

    /**
     * Returns the preferred size of the component, which varies based on expansion state.
     *
     * @return the preferred {@link Dimension}
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), UIScale.scale(64) + (int) (data.expansion * UIScale.scale(44)));
    }

    /**
     * Returns the maximum size of the component.
     *
     * @return the maximum {@link Dimension}
     */
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    /**
     * Paints the track row, including title, artist, duration, stars, and the play button if expanded.
     *
     * @param g the graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2);
        final int w = getWidth(), h = getHeight();

        drawBackground(g2, w, h);
        drawTrackDetails(g2, w, h);
        drawStats(g2, w, h);
        drawActionButton(g2, w, h);

        g2.dispose();
    }

    /**
     * Renders the background highlight based on hover and expansion state.
     */
    private void drawBackground(Graphics2D g2, int w, int h) {
        if (data.expanded) {
            Color acc = data.getAccent();
            g2.setColor(new Color(acc.getRed(), acc.getGreen(), acc.getBlue(), 38));
            g2.fillRect(0, 0, w, h);
            g2.setColor(acc);
            g2.fillRect(0, 0, UIScale.scale(3), h);
        } else if (hovered) {
            g2.setColor(new Color(255, 255, 255, 10));
            g2.fillRect(0, 0, w, h);
        }
        g2.setColor(new Color(255, 255, 255, 15));
        g2.drawLine(0, h - 1, w, h - 1);
    }

    /**
     * Renders title, artist and download status icon.
     */
    private void drawTrackDetails(Graphics2D g2, int w, int h) {
        final boolean downloaded = data.isDownloaded(audiusClient);
        g2.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_16));
        g2.setColor(downloaded ? RenderUtils.cyan : Color.GRAY);
        g2.drawString(downloaded ? "✓" : "☁", UIScale.scale(20), UIScale.scale(38));

        g2.setColor(Color.WHITE);
        g2.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_20));
        g2.drawString(data.title, UIScale.scale(50), UIScale.scale(28));
        g2.setColor(Color.GRAY);
        g2.setFont(UIScale.scaleFont(RenderCache.SANS_PLAIN_15));
        g2.drawString(data.artist, UIScale.scale(50), UIScale.scale(48));
    }

    /**
     * Renders track duration, stars and best score.
     */
    private void drawStats(Graphics2D g2, int w, int h) {
        final int rightX = w - UIScale.scale(20);
        g2.setFont(UIScale.scaleFont(RenderCache.SANS_PLAIN_20));
        final String sanitizedTitle = data.title.replaceAll("[\\\\/:*?\"<>|]", "_");
        final String info = String.format("%s  •  Best: %d", data.duration, ScoreManager.getBestScore(sanitizedTitle));
        final FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(200, 200, 200));
        g2.drawString(info, rightX - fm.stringWidth(info), UIScale.scale(38));

        final String stars = "★".repeat(data.stars) + "☆".repeat(10 - data.stars);
        g2.setColor(RenderUtils.cyan);
        g2.drawString(stars, rightX - fm.stringWidth(info) - g2.getFontMetrics().stringWidth(stars) - UIScale.scale(15), UIScale.scale(38));
    }

    /**
     * Renders the PLAY / DOWNLOADING / READY button.
     */
    private void drawActionButton(Graphics2D g2, int w, int h) {
        if (data.expansion <= 0.5f) return;
        final int btnW = UIScale.scale(110), btnH = UIScale.scale(32), bx = w - UIScale.scale(20) - btnW, by = UIScale.scale(60);

        if (data.starting) {
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(bx, by, btnW, btnH, UIScale.scale(8), UIScale.scale(8));
            g2.setColor(new Color(10, 10, 26));
            g2.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_13));
            drawCenteredString(g2, "READY!", bx, by, btnW, btnH);
            final float alpha = Math.max(0, 1.0f - data.startingProgress);
            g2.setColor(new Color(255, 255, 255, (int) (alpha * 120)));
            g2.fillRect(0, 0, w, h);

        } else if (data.downloading) {
            g2.setColor(new Color(255, 255, 255, 20));
            g2.fillRoundRect(bx, by, btnW, btnH, UIScale.scale(8), UIScale.scale(8));
            g2.setPaint(new GradientPaint(bx, by, RenderUtils.purple, bx + btnW, by, RenderUtils.cyan));
            g2.fillRoundRect(bx, by, (int) (btnW * data.downloadProgress), btnH, UIScale.scale(8), UIScale.scale(8));
            g2.setColor(Color.WHITE);
            g2.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_11));
            drawCenteredString(g2, (int) (data.downloadProgress * 100) + "%", bx, by, btnW, btnH);

        } else {
            g2.setColor(data.getAccent());
            g2.fillRoundRect(bx, by, btnW, btnH, UIScale.scale(8), UIScale.scale(8));
            g2.setColor(new Color(10, 10, 26));
            g2.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_13));
            drawCenteredString(g2, "PLAY", bx, by, btnW, btnH);
        }

        final Rectangle playRect = new Rectangle(bx, by, btnW, btnH);
        final Point mouse = getMousePosition();
        if (mouse != null && playRect.contains(mouse) && !data.downloading && !data.starting) {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    /**
     * Helper method to draw a centered string within a box.
     */
    private void drawCenteredString(Graphics2D g2, String text, int x, int y, int w, int h) {
        final FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + (h + fm.getAscent()) / 2 - 2);
    }

    /**
     * Logic for clicking the action button. Triggers download or launches the level.
     */
    private void handlePlay() {
        if (data.starting) return;
        if (data.isDownloaded(audiusClient)) {
            launchGame(data.findDownloadedPath(audiusClient), data.stars);
        } else {
            data.downloading = true;
            data.downloadProgress = 0.15f;
            audiusClient.downloadMusic(data.id, data.title).thenAccept(downloadedPath -> {
                data.downloadProgress = 1f;
                SwingUtilities.invokeLater(() -> {
                    data.downloading = false;
                    launchGame(downloadedPath, data.stars);
                });
            }).exceptionally(ex -> {
                data.downloading = false;
                ExceptionHandler.handle("Download failed for " + data.title, ex);
                return null;
            });
        }
    }

    /**
     * Transitions to the GameScreen and starts the level.
     */
    private void launchGame(Path audioPath, int stars) {
        if (audioPath == null) return;
        data.starting = true;
        data.startingProgress = 0f;

        new Thread(() -> {
            try {
                Thread.sleep(600);
                SwingUtilities.invokeLater(() -> {
                    try {
                        screenManager.initScreen(GameScreen.class);
                        final GameScreen gs = screenManager.getScreen(GameScreen.class);
                        screenManager.showScreen(GameScreen.class);
                        gs.setupGamePanel(audioPath, stars);
                        data.starting = false;
                    } catch (Exception ex) {
                        ExceptionHandler.handle("Failed to launch game", ex);
                    }
                });
            } catch (InterruptedException ignored) {
            }
        }).start();
    }
}
