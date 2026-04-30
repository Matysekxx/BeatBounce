package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.screen.GameScreen;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrackRow extends JPanel {
    private static final Logger LOG = Logger.getLogger(TrackRow.class.getName());

    private final TrackData data;
    private final AudiusClient audiusClient;
    private final ScreenManager screenManager;
    private boolean hovered = false;

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
                    final int btnW = 110, btnH = 32;
                    final int bx = getWidth() - 20 - btnW;
                    final int by = 60;
                    final  Rectangle playRect = new Rectangle(bx, by, btnW, btnH);
                    if (playRect.contains(e.getPoint()) && !data.downloading) {
                        handlePlay();
                    }
                }
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
        final int w = getWidth();
        final int h = getHeight();

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

        final boolean downloaded = data.isDownloaded(audiusClient);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.setColor(downloaded ? RenderUtils.cyan : Color.GRAY);
        g2.drawString(downloaded ? "✓" : "☁", 20, 38);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.drawString(data.title, 50, 28);
        g2.setColor(Color.GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString(data.artist, 50, 48);

        final int rightX = w - 20;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        final String info = String.format("%s  •  Best: %d", data.duration, data.best);
        final FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(200, 200, 200));
        g2.drawString(info, rightX - fm.stringWidth(info), 38);

        final String stars = "★".repeat(data.stars) + "☆".repeat(5 - data.stars);
        g2.setColor(RenderUtils.cyan);
        g2.drawString(stars, rightX - fm.stringWidth(info) - g2.getFontMetrics().stringWidth(stars) - 15, 38);

        if (data.expansion > 0.5f) {
            final int btnW = 110, btnH = 32;
            final int bx = w - 20 - btnW;
            final int by = 60;
            g2.setColor(data.getAccent());
            g2.fillRoundRect(bx, by, btnW, btnH, 8, 8);
            g2.setColor(new Color(10, 10, 26));
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            final String txt = data.downloading ? "..." : "PLAY";
            final FontMetrics bfm = g2.getFontMetrics();
            g2.drawString(txt, bx + (btnW - bfm.stringWidth(txt)) / 2, by + 20);

            if (data.downloading) {
                g2.setColor(new Color(255, 255, 255, 50));
                g2.drawRect(bx, by + btnH + 5, btnW, 4);
                g2.setColor(Color.WHITE);
                g2.fillRect(bx, by + btnH + 5, (int) (btnW * data.downloadProgress), 4);
            }

            final Rectangle playRect = new Rectangle(bx, by, btnW, btnH);
            final Point mouse = getMousePosition();
            if (mouse != null && playRect.contains(mouse) && !data.downloading) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        }

        g2.dispose();
    }

    private void handlePlay() {
        if (data.isDownloaded(audiusClient)) {
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
            final GameScreen gs = screenManager.getScreen(GameScreen.class);
            screenManager.showScreen(GameScreen.class);
            gs.setupGamePanel(audioPath);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Failed to launch game for " + data.title, ex);
        }
    }
}