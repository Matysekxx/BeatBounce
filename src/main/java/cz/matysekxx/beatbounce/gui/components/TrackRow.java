package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.screen.GameScreen;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;
import cz.matysekxx.beatbounce.model.score.ScoreManager;
import cz.matysekxx.beatbounce.util.ExceptionHandler;
import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * A unified, highly-polished premium UI component representing a single track (local or online).
 * It features a beautiful, non-expanding round glass card design.
 */
public class TrackRow extends JPanel {
    private final TrackData data;
    private final Path localPath;
    private final AudiusClient audiusClient;
    private final ScreenManager screenManager;

    private final String title;
    private final String artist;
    private final int stars;
    private final String bestScore;

    private boolean hovered = false;

    /**
     * Constructor for online tracks (Audius API).
     */
    public TrackRow(TrackData data, AudiusClient audiusClient, ScreenManager screenManager, Consumer<TrackData> onSelect) {
        this.data = data;
        this.localPath = null;
        this.audiusClient = audiusClient;
        this.screenManager = screenManager;

        this.title = data.title;
        this.artist = data.artist;
        this.stars = data.stars;

        final String sanitized = data.title.replaceAll("[\\\\/:*?\"<>|]", "_");
        this.bestScore = String.valueOf(ScoreManager.getBestScore(sanitized));

        setupUI(onSelect);
    }

    /**
     * Constructor for local tracks (Library).
     */
    public TrackRow(Path path, ScreenManager screenManager) {
        this.data = null;
        this.localPath = path;
        this.audiusClient = null;
        this.screenManager = screenManager;

        final String rawName = path.getFileName().toString();
        int dot = rawName.lastIndexOf('.');
        this.title = (dot > 0) ? rawName.substring(0, dot) : rawName;
        this.artist = "Local Song";
        this.stars = 1 + (Math.abs(title.hashCode()) % 10);
        this.bestScore = String.valueOf(ScoreManager.getBestScore(title));

        setupUI(null);
    }

    private void setupUI(Consumer<TrackData> onSelect) {
        this.setOpaque(false);
        this.setPreferredSize(new Dimension(0, UIScale.scale(90)));
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(90)));
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
                if (localPath != null) {
                    launchGame(localPath, stars);
                } else if (data != null) {
                    if (onSelect != null) {
                        onSelect.accept(data);
                    }
                    if (!data.isDownloaded(audiusClient)) {
                        handlePlay();
                    } else {
                        launchGame(data.findDownloadedPath(audiusClient), stars);
                    }
                }
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
        g2.fillRoundRect(0, 0, w, h, UIScale.scale(18), UIScale.scale(18));

        g2.setColor(hovered ? new Color(0, 255, 255, 120) : new Color(255, 255, 255, 25));
        g2.drawRoundRect(0, 0, w - 1, h - 1, UIScale.scale(18), UIScale.scale(18));

        g2.setColor(new Color(255, 255, 255, 25));
        g2.fillRoundRect(UIScale.scale(18), UIScale.scale(15), UIScale.scale(60), UIScale.scale(60), UIScale.scale(12), UIScale.scale(12));
        g2.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_26));
        g2.setColor(hovered ? RenderUtils.cyan : Color.WHITE);

        String icon = "🎵";
        if (localPath == null && data != null) {
            if (data.isDownloaded(audiusClient)) {
                icon = "✓";
                g2.setColor(RenderUtils.cyan);
            } else {
                icon = "☁";
                g2.setColor(Color.GRAY);
            }
        }

        final FontMetrics fmIcon = g2.getFontMetrics();
        g2.drawString(icon, UIScale.scale(18) + (UIScale.scale(60) - fmIcon.stringWidth(icon)) / 2, UIScale.scale(15) + UIScale.scale(42));

        final String scoreText = "BEST: " + bestScore;
        g2.setFont(UIScale.scaleFont(RenderCache.MONO_BOLD_17));
        final FontMetrics fmScore = g2.getFontMetrics();
        final int scoreW = fmScore.stringWidth(scoreText) + UIScale.scale(24);
        final int scoreX = w - UIScale.scale(170) - scoreW;

        g2.setColor(new Color(255, 255, 255, 20));
        final int scoreH = UIScale.scale(34);
        g2.fillRoundRect(scoreX, (h - scoreH) / 2, scoreW, scoreH, UIScale.scale(10), UIScale.scale(10));
        g2.setColor(RenderUtils.cyan);
        g2.drawString(scoreText, scoreX + UIScale.scale(12), (h - scoreH) / 2 + UIScale.scale(24));

        g2.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_22));
        g2.setColor(Color.WHITE);

        final int textStartX = UIScale.scale(100);
        String displayTitle = getString(scoreX, textStartX, g2);
        g2.drawString(displayTitle, textStartX, UIScale.scale(42));

        g2.setFont(UIScale.scaleFont(RenderCache.SANS_PLAIN_20));
        g2.setColor(new Color(180, 180, 200));
        final String subText = artist + "  •  Difficulty: " + "★".repeat(stars) + "☆".repeat(10 - stars);
        g2.drawString(subText, textStartX, UIScale.scale(68));

        final int btnW = UIScale.scale(140), btnH = UIScale.scale(50);
        final int bx = w - UIScale.scale(155);
        final int by = (h - btnH) / 2;

        if (localPath != null || (data != null && !data.downloading && !data.starting)) {
            g2.setColor(hovered ? RenderUtils.cyan : new Color(0, 200, 255));
            g2.fillRoundRect(bx, by, btnW, btnH, UIScale.scale(14), UIScale.scale(14));

            g2.setColor(Color.BLACK);
            g2.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_20));
            final String playTxt = (localPath != null || data.isDownloaded(audiusClient)) ? "PLAY" : "GET";
            final FontMetrics fmPlay = g2.getFontMetrics();
            g2.drawString(playTxt, bx + (btnW - fmPlay.stringWidth(playTxt)) / 2, by + UIScale.scale(32));
        } else if (data != null && data.downloading) {
            g2.setColor(new Color(255, 255, 255, 20));
            g2.fillRoundRect(bx, by, btnW, btnH, UIScale.scale(14), UIScale.scale(14));

            g2.setPaint(new GradientPaint(bx, by, RenderUtils.purple, bx + btnW, by, RenderUtils.cyan));
            g2.fillRoundRect(bx, by, (int) (btnW * data.downloadProgress), btnH, UIScale.scale(14), UIScale.scale(14));

            g2.setColor(Color.WHITE);
            g2.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_18));
            final String pctTxt = (int) (data.downloadProgress * 100) + "%";
            final FontMetrics fmPct = g2.getFontMetrics();
            g2.drawString(pctTxt, bx + (btnW - fmPct.stringWidth(pctTxt)) / 2, by + UIScale.scale(32));
        } else if (data != null && data.startingProgress > 0f) {
            final float alpha = Math.max(0f, 1.0f - data.startingProgress);
            g2.setColor(new Color(255, 255, 255, (int) (alpha * 120)));
            g2.fillRoundRect(0, 0, w, h, UIScale.scale(18), UIScale.scale(18));
        }

        g2.dispose();
    }

    private String getString(int scoreX, int textStartX, Graphics2D g2) {
        final int maxTitleWidth = scoreX - textStartX - UIScale.scale(20);
        final FontMetrics fmTitle = g2.getFontMetrics();

        String displayTitle = title;
        if (fmTitle.stringWidth(displayTitle) > maxTitleWidth && maxTitleWidth > 0) {
            while (!displayTitle.isEmpty() && fmTitle.stringWidth(displayTitle + "...") > maxTitleWidth)
                displayTitle = displayTitle.substring(0, displayTitle.length() - 1);

            displayTitle += "...";
        }
        return displayTitle;
    }

    private void handlePlay() {
        if (data.starting) return;
        if (data.isDownloaded(audiusClient)) {
            launchGame(data.findDownloadedPath(audiusClient), data.stars);
        } else {
            data.downloading = true;
            data.downloadProgress = 0.15f;
            repaint();
            audiusClient.downloadMusic(data.id, data.title).thenAccept(downloadedPath -> {
                data.downloadProgress = 1f;
                SwingUtilities.invokeLater(() -> {
                    data.downloading = false;
                    launchGame(downloadedPath, data.stars);
                });
            }).exceptionally(ex -> {
                data.downloading = false;
                repaint();
                ExceptionHandler.handle("Download failed for " + data.title, ex);
                return null;
            });
        }
    }

    private void launchGame(Path audioPath, int stars) {
        if (audioPath == null) return;
        if (data != null) {
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
        } else {
            try {
                screenManager.initScreen(GameScreen.class);
                final GameScreen gs = screenManager.getScreen(GameScreen.class);
                screenManager.showScreen(GameScreen.class);
                gs.setupGamePanel(audioPath, stars);
            } catch (Exception ex) {
                ExceptionHandler.handle("Failed to launch game", ex);
            }
        }
    }
}
