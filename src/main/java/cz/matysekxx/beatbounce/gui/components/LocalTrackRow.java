package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.screen.GameScreen;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;
import cz.matysekxx.beatbounce.model.score.ScoreManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A UI component representing a single song in the library.
 */
public class LocalTrackRow extends JPanel {
    private static final Logger LOG = Logger.getLogger(LocalTrackRow.class.getName());
    /**
     * The difficulty level of the song (1-5).
     */
    private final int stars;
    /**
     * The display name of the song file (without extension).
     */
    private final String fileName;
    /**
     * The best score achieved by the user on this song.
     */
    private final String bestScore;
    private final ScreenManager screenManager;
    /**
     * Whether the mouse is currently hovering over this row.
     */
    private boolean hovered = false;

    /**
     * Constructs a LocalTrackRow for the given song path.
     *
     * @param path the path to the audio file
     */
    public LocalTrackRow(Path path, ScreenManager screenManager) {
        this.screenManager = screenManager;
        final String rawName = path.getFileName().toString();
        int dot = rawName.lastIndexOf('.');
        this.fileName = (dot > 0) ? rawName.substring(0, dot) : rawName;
        this.stars = 1 + (Math.abs(fileName.hashCode()) % 10);
        this.bestScore = String.valueOf(ScoreManager.getBestScore(fileName));

        this.setOpaque(false);
        this.setPreferredSize(new Dimension(0, 90));
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
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
                launchGame(path, stars);
            }
        });
    }

    /**
     * Switches the screen to the game screen and starts the game with the selected song.
     *
     * @param audioPath the path to the selected audio file
     * @param stars     the difficulty level represented by stars
     */
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
        g2.fillRoundRect(0, 0, w, h, 18, 18);

        g2.setColor(hovered ? new Color(0, 255, 255, 120) : new Color(255, 255, 255, 25));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);

        g2.setColor(new Color(255, 255, 255, 25));
        g2.fillRoundRect(18, 15, 60, 60, 12, 12);
        g2.setFont(RenderCache.SANS_BOLD_26);
        g2.setColor(hovered ? RenderUtils.cyan : Color.WHITE);
        final String icon = "🎵";
        FontMetrics fmIcon = g2.getFontMetrics();
        g2.drawString(icon, 18 + (60 - fmIcon.stringWidth(icon)) / 2, 15 + 42);

        g2.setFont(RenderCache.SANS_BOLD_22);
        g2.setColor(Color.WHITE);
        g2.drawString(fileName, 100, 42);

        g2.setFont(RenderCache.SANS_PLAIN_20);
        g2.setColor(new Color(180, 180, 200));
        final String subText = "Stars: " + "★".repeat(stars) + "☆".repeat(10 - stars);
        g2.drawString(subText, 100, 68);

        String scoreText = "BEST: " + bestScore;
        g2.setFont(RenderCache.MONO_BOLD_17);
        final FontMetrics fmScore = g2.getFontMetrics();
        final int scoreW = fmScore.stringWidth(scoreText) + 24;
        final int scoreX = w - 170 - scoreW;

        g2.setColor(new Color(255, 255, 255, 20));
        g2.fillRoundRect(scoreX, (h - 34) / 2, scoreW, 34, 10, 10);
        g2.setColor(RenderUtils.cyan);
        g2.drawString(scoreText, scoreX + 12, (h - 34) / 2 + 24);

        final int btnW = 140, btnH = 50;
        final int bx = w - 155;
        final int by = (h - btnH) / 2;

        g2.setColor(hovered ? RenderUtils.cyan : new Color(0, 200, 255));
        g2.fillRoundRect(bx, by, btnW, btnH, 14, 14);

        g2.setColor(Color.BLACK);
        g2.setFont(RenderCache.SANS_BOLD_20);
        String playTxt = "PLAY";
        final FontMetrics fmPlay = g2.getFontMetrics();
        g2.drawString(playTxt, bx + (btnW - fmPlay.stringWidth(playTxt)) / 2, by + 32);

        g2.dispose();
    }
}