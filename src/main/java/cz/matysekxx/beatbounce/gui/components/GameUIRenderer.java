package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.game.GameEngine;
import cz.matysekxx.beatbounce.model.game.ReviveManager;
import cz.matysekxx.beatbounce.model.game.state.GameState;
import cz.matysekxx.beatbounce.model.score.ScoreManager;

import cz.matysekxx.beatbounce.util.LevelUtil;
import cz.matysekxx.beatbounce.util.UIScale;

import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A renderer class responsible for drawing the game's user interface elements.
 * This includes countdowns, pause screens, game over screens, scores, and progress bars.
 */
public class GameUIRenderer {
    /**
     * Background color for the pause screen.
     */
    private static final Color PAUSE_BG = new Color(0, 0, 8, 170);

    /**
     * Accent line color for the pause screen.
     */
    private static final Color PAUSE_LINE = new Color(0, 255, 220, 55);

    /**
     * Background color for the level finished screen.
     */
    private static final Color FINISHED_BG = new Color(0, 0, 0, 160);

    /**
     * Yellow accent line color for the finished screen.
     */
    private static final Color FINISHED_YELLOW_LINE = new Color(255, 215, 0);

    /**
     * Background color for the game over screen.
     */
    private static final Color GAMEOVER_BG = new Color(0, 0, 0, 180);

    /**
     * Scanline color for the game over screen.
     */
    private static final Color GAMEOVER_LINES = new Color(0, 0, 0, 20);

    /**
     * Primary red color for game over text.
     */
    private static final Color GAMEOVER_RED = new Color(255, 40, 40);

    /**
     * Light red color for game over secondary text.
     */
    private static final Color GAMEOVER_RED_LIGHT = new Color(255, 100, 100);

    /**
     * Cyan component for the glitch effect.
     */
    private static final Color GLITCH_CYAN = new Color(0, 255, 220, 50);

    /**
     * Red component for the glitch effect.
     */
    private static final Color GLITCH_RED = new Color(255, 0, 80, 40);

    /**
     * Color for score labels.
     */
    private static final Color SCORE_LABEL_COLOR = new Color(180, 180, 180, 180);

    /**
     * Primary color for orbs.
     */
    private static final Color ORBS_COLOR = new Color(255, 200, 0);

    /**
     * Color for total orb count text.
     */
    private static final Color TOTAL_ORBS_COLOR = new Color(200, 200, 200);

    /**
     * Primary text color for scores.
     */
    private static final Color SCORE_TEXT_COLOR = new Color(255, 255, 255, 160);

    /**
     * Text color for orb collection count.
     */
    private static final Color ORBS_TEXT_COLOR = new Color(255, 200, 0, 200);

    /**
     * The background color for the song progress bar.
     */
    private static final Color PROGRESS_BG = new Color(255, 255, 255, 22);

    /**
     * The title of the song being played.
     */
    private final String songTitle;

    /**
     * The artist of the song being played.
     */
    private final String songArtist;

    /**
     * The game engine instance to pull data from.
     */
    private final GameEngine gameEngine;

    /**
     * The audio clip to track song progress.
     */
    private final Clip clip;

    /**
     * List of buttons currently active on the screen for interaction.
     */
    private final ArrayList<SimulatedButton> activeButtons = new ArrayList<>();

    /**
     * Pool of buttons to avoid recreation.
     */
    private final ArrayList<SimulatedButton> buttonPool = new ArrayList<>();
    /**
     * Reusable StringBuilder for time formatting.
     */
    private final StringBuilder timeStringBuilder = new StringBuilder(16);
    /**
     * Reusable RoundRectangle2D for rendering.
     */
    private final RoundRectangle2D.Float rectScratch = new RoundRectangle2D.Float();
    /**
     * Reusable array for gradient fractions (2 stops).
     */
    private final float[] fractions2 = {0f, 1f};
    /**
     * Reusable array for gradient colors (2 stops).
     */
    private final Color[] colors2 = new Color[2];
    /**
     * Reusable array for gradient fractions (3 stops).
     */
    private final float[] fractions3 = {0f, 0.5f, 1f};
    /**
     * Reusable array for gradient colors (3 stops).
     */
    private final Color[] colors3 = new Color[3];
    /**
     * Current index in the button pool.
     */
    private int buttonPoolIndex = 0;
    /**
     * Tracks the last known game state for animation triggers.
     */
    private GameState lastState = GameState.COUNTDOWN;
    /**
     * Timer for screen entrance animations.
     */
    private float screenAppearTimer = 0f;
    /**
     * Timer for the tutorial animation.
     */
    private float tutorialTimer = 0f;
    /**
     * List of buttons rendered in the previous frame.
     */
    private List<SimulatedButton> renderedButtons = Collections.emptyList();
    /**
     * Current vertical translation for animation.
     */
    private int currentTranslateY = 0;
    /**
     * Current mouse X-coordinate in virtual space.
     */
    private int mouseX = -1;
    /**
     * Current mouse Y-coordinate in virtual space.
     */
    private int mouseY = -1;
    /**
     * Cached RadialGradientPaint for the score halo effect.
     */
    private RadialGradientPaint cachedHaloPaint;
    /**
     * The last color used for the cached halo paint.
     */
    private Color lastHaloColor;
    /**
     * The last pulse value used for the cached halo paint.
     */
    private float lastHaloPulse = -1;
    /**
     * Cached LinearGradientPaint for the progress bar.
     */
    private LinearGradientPaint cachedProgressGradient;
    /**
     * The width of the progress bar when the gradient was cached.
     */
    private int cachedProgressWidth = -1;
    /**
     * Cached RadialGradientPaint for the progress bar glow.
     */
    private RadialGradientPaint cachedProgressGlow;

    /**
     * Constructs a new GameUIRenderer.
     *
     * @param gameEngine the game model to retrieve state and score from
     * @param clip       the audio clip to track progress
     * @param songTitle  the title of the song
     * @param songArtist the artist of the song
     */
    public GameUIRenderer(GameEngine gameEngine, Clip clip, String songTitle, String songArtist) {
        this.gameEngine = gameEngine;
        this.clip = clip;
        this.songTitle = songTitle;
        this.songArtist = songArtist;
    }

    /**
     * Updates the internal mouse position for button hover detection.
     *
     * @param mx mouse X
     * @param my mouse Y
     */
    public void setMousePosition(int mx, int my) {
        this.mouseX = mx;
        this.mouseY = my;
    }

    /**
     * Checks if a click occurred within any rendered button and returns its action.
     *
     * @param mx click X
     * @param my click Y
     * @return the {@link UIAction} to perform
     */
    public UIAction handleClick(int mx, int my) {
        for (SimulatedButton btn : renderedButtons) {
            if (btn.contains(mx, my)) {
                return btn.getAction();
            }
        }
        return UIAction.NONE;
    }

    /**
     * Updates the UI state, specifically for animations.
     *
     * @param dt delta time in seconds
     */
    public void update(float dt) {
        final GameState currentState = gameEngine.getGameState();
        if (currentState != lastState) {
            lastState = currentState;
            screenAppearTimer = 0f;
            if (currentState == GameState.COUNTDOWN) {
                tutorialTimer = 0f;
            }
        }
        if (screenAppearTimer < 1f) {
            screenAppearTimer += dt;
        }
        if (currentState == GameState.PLAYING || currentState == GameState.COUNTDOWN) {
            tutorialTimer += dt;
        }
    }

    /**
     * Draws the mouse tutorial at the beginning of the level.
     *
     * @param g2d    the graphics context
     * @param width  the width of the rendering area
     * @param height the height of the rendering area
     */
    public void drawTutorial(Graphics2D g2d, int width, int height) {
        if (tutorialTimer > 6.0f) return;
        final float alpha = tutorialTimer > 5.0f ? 1.0f - (tutorialTimer - 5.0f) : 1.0f;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        final int centerX = width / 2;
        final int centerY = height / 2 + UIScale.scale(180);
        final int amplitude = UIScale.scale(120);
        final double speed = 3.5;

        for (int i = 1; i <= 5; i++) {
            final float trailAlpha = (1.0f - (i / 6.0f)) * 0.3f;
            final double trailTime = tutorialTimer - (i * 0.02);
            final int trailX = centerX + (int) (Math.sin(trailTime * speed) * amplitude);
            drawMouseShape(g2d, trailX, centerY, RenderCache.customColorWithAlpha(RenderUtils.cyan, (int) (trailAlpha * 255)), false);
        }

        final int mX = centerX + (int) (Math.sin(tutorialTimer * speed) * amplitude);
        drawMouseShape(g2d, mX, centerY, RenderUtils.cyan, true);
        g2d.setFont(UIScale.scaleFont(RenderCache.MONTSERRAT_BLACK_18));
        final String text = "MOVE MOUSE TO CONTROL";
        final FontMetrics fm = g2d.getFontMetrics();
        RenderUtils.drawText(g2d, text, (width - fm.stringWidth(text)) / 2, centerY + UIScale.scale(80), RenderUtils.cyan);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    /**
     * Renders a mouse icon for the tutorial.
     */
    private void drawMouseShape(Graphics2D g2d, int x, int y, Color color, boolean fullDetail) {
        final int mouseW = UIScale.scale(50);
        final int mouseH = UIScale.scale(70);
        final int mouseXPos = x - mouseW / 2;
        final int mouseYPos = y - mouseH / 2;

        if (fullDetail) {
            g2d.setColor(new Color(0, 0, 0, 160));
            g2d.fillRoundRect(mouseXPos, mouseYPos, mouseW, mouseH, UIScale.scale(24), UIScale.scale(24));
        }

        g2d.setStroke(RenderCache.STROKE_3);
        g2d.setColor(color);
        g2d.drawRoundRect(mouseXPos, mouseYPos, mouseW, mouseH, UIScale.scale(24), UIScale.scale(24));

        if (fullDetail) {
            g2d.setStroke(RenderCache.STROKE_2);
            g2d.drawLine(x, mouseYPos, x, mouseYPos + UIScale.scale(25));
            g2d.drawLine(mouseXPos, mouseYPos + UIScale.scale(25), mouseXPos + mouseW, mouseYPos + UIScale.scale(25));
        }
    }

    /**
     * Draws the countdown animation on the screen.
     *
     * @param g2d    the graphics context
     * @param width  the width of the rendering area
     * @param height the height of the rendering area
     */
    public void drawCountdown(Graphics2D g2d, int width, int height) {
        final String text = String.valueOf((int) Math.ceil(gameEngine.getCountdownTime()));

        final double countFrac = gameEngine.getCountdownTime() % 1.0;
        final int ringR = (int) (countFrac * UIScale.scale(230));
        final int ringAlpha = (int) (220 * (1.0 - countFrac));

        if (ringR > 2 && ringAlpha > 0) {
            drawRing(g2d, width / 2, height / 2, ringR, RenderUtils.cyan, ringAlpha, RenderCache.STROKE_2);
            drawRing(g2d, width / 2, height / 2, Math.max(0, ringR - UIScale.scale(20)), RenderUtils.cyan, (int) (ringAlpha * 0.6), RenderCache.STROKE_2);
            drawRing(g2d, width / 2, height / 2, Math.max(0, ringR - UIScale.scale(40)), RenderUtils.cyan, (int) (ringAlpha * 0.3), RenderCache.STROKE_1);
            g2d.setStroke(RenderCache.STROKE_1);
        }

        g2d.setFont(UIScale.scaleFont(RenderCache.AUDIOWIDE_150));
        final FontMetrics fm = g2d.getFontMetrics();
        RenderUtils.drawText(g2d, text, (width - fm.stringWidth(text)) / 2, (height + fm.getAscent()) / 2, RenderUtils.cyan);
    }

    /**
     * Helper to draw a glowing ring.
     */
    private void drawRing(Graphics2D g2d, int cx, int cy, int r, Color color, int alpha, BasicStroke stroke) {
        g2d.setColor(RenderCache.customColorWithAlpha(color, alpha));
        g2d.setStroke(stroke);
        g2d.drawOval(cx - r, cy - r, r * 2, r * 2);
    }

    /**
     * Draws a "glass" styled card with a background and border.
     */
    private void drawGlassCard(Graphics2D g2d, int width, int height, int cardW, int cardH, Color accentColor, float pulse) {
        final int cardX = (width - cardW) / 2;
        final int cardY = (height - cardH) / 2;

        colors2[0] = RenderCache.customColorWithAlpha(accentColor, (int) (18 + 15 * pulse));
        colors2[1] = RenderCache.BLACK_ALPHA[0];
        g2d.setPaint(new RadialGradientPaint(width / 2f, height / 2f, cardW * 0.7f, fractions2, colors2));
        g2d.fillRoundRect(cardX - UIScale.scale(20), cardY - UIScale.scale(20), cardW + UIScale.scale(40), cardH + UIScale.scale(40), UIScale.scale(30), UIScale.scale(30));

        g2d.setColor(new Color(6, 0, 18, 230));
        g2d.fillRoundRect(cardX, cardY, cardW, cardH, UIScale.scale(18), UIScale.scale(18));

        g2d.setColor(RenderCache.customColorWithAlpha(accentColor, (int) (110 + 100 * pulse)));
        g2d.setStroke(RenderCache.STROKE_2);
        g2d.drawRoundRect(cardX, cardY, cardW, cardH, UIScale.scale(18), UIScale.scale(18));

        g2d.setColor(RenderCache.customColorWithAlpha(accentColor, (int) (25 + 15 * pulse)));
        g2d.setStroke(RenderCache.STROKE_1);
        g2d.drawRoundRect(cardX + UIScale.scale(4), cardY + UIScale.scale(4), cardW - UIScale.scale(8), cardH - UIScale.scale(8), UIScale.scale(14), UIScale.scale(14));
    }

    /**
     * Calculates a pulsation value.
     */
    private float getPulse(double speed) {
        return (float) ((Math.sin(System.currentTimeMillis() / speed) + 1.0) / 2.0);
    }

    /**
     * Sets up the translation and renders the card for screens.
     */
    private int setupScreenCard(Graphics2D g2d, int width, int height, int cardW, int cardH, Color accentColor, float pulse) {
        final float appearProgress = Math.min(1f, screenAppearTimer / 0.4f);
        final float easedAppear = 1f - (float) Math.pow(1f - appearProgress, 3);
        final int offsetY = (int) (UIScale.scale(-50) * (1f - easedAppear));
        g2d.translate(0, offsetY);
        this.currentTranslateY = offsetY;

        final int cardY = (height - cardH) / 2;
        drawGlassCard(g2d, width, height, cardW, cardH, accentColor, pulse);
        return cardY;
    }

    /**
     * Reverts translation after rendering screen card.
     */
    private void teardownScreenCard(Graphics2D g2d) {
        final float appearProgress = Math.min(1f, screenAppearTimer / 0.4f);
        final float easedAppear = 1f - (float) Math.pow(1f - appearProgress, 3);
        final int offsetY = (int) (UIScale.scale(-50) * (1f - easedAppear));
        g2d.translate(0, -offsetY);
        this.currentTranslateY = 0;
    }

    /**
     * Draws a separator line inside a card.
     */
    private void drawCardLine(Graphics2D g2d, int width, int cardW, int y, Color color, float pulse) {
        g2d.setColor(RenderCache.customColorWithAlpha(color, (int) (70 + 50 * pulse)));
        g2d.setStroke(RenderCache.STROKE_2);
        g2d.drawLine((width - cardW) / 2 + UIScale.scale(40), y, (width + cardW) / 2 - UIScale.scale(40), y);
        g2d.setStroke(RenderCache.STROKE_1);
    }

    /**
     * Helper to draw two buttons side-by-side at the bottom of an end screen.
     */
    private void drawEndScreenButtons(Graphics2D g2d, int width, int y, String l1, UIAction a1, String l2, UIAction a2) {
        drawButton(g2d, l1, width / 2 - UIScale.scale(230), y, UIScale.scale(220), a1);
        drawButton(g2d, l2, width / 2 + UIScale.scale(10), y, UIScale.scale(220), a2);
    }

    /**
     * Draws the pause screen overlay.
     *
     * @param g2d    the graphics context
     * @param width  the width of the rendering area
     * @param height the height of the rendering area
     */
    public void drawPauseScreen(Graphics2D g2d, int width, int height) {
        g2d.setColor(PAUSE_BG);
        g2d.fillRect(0, 0, width, height);

        final float pulse = getPulse(700.0);
        final int cardW = UIScale.scale(600);
        final int cardH = UIScale.scale(340);
        final int cardY = (height - cardH) / 2;

        drawGlassCard(g2d, width, height, cardW, cardH, RenderUtils.cyan, pulse);

        g2d.setFont(UIScale.scaleFont(RenderCache.AUDIOWIDE_64));
        final String title = "PAUSED";
        RenderUtils.drawText(g2d, title, (width - g2d.getFontMetrics().stringWidth(title)) / 2, cardY + UIScale.scale(110), RenderUtils.cyan);

        g2d.setStroke(RenderCache.STROKE_1);
        g2d.setColor(PAUSE_LINE);
        g2d.drawLine((width - cardW) / 2 + UIScale.scale(40), cardY + UIScale.scale(135), (width + cardW) / 2 - UIScale.scale(40), cardY + UIScale.scale(135));

        drawEndScreenButtons(g2d, width, cardY + UIScale.scale(240), "Resume", UIAction.RESUME, "Quit to Menu", UIAction.QUIT);
    }

    /**
     * Draws the level complete (finished) screen overlay.
     *
     * @param g2d    the graphics context
     * @param width  the width of the rendering area
     * @param height the height of the rendering area
     */
    public void drawFinishedScreen(Graphics2D g2d, int width, int height) {
        final float pulse = getPulse(600.0);
        g2d.setColor(FINISHED_BG);
        g2d.fillRect(0, 0, width, height);

        final int cardW = UIScale.scale(750);
        final int cardH = gameEngine.isNewHighScore() ? UIScale.scale(520) : UIScale.scale(480);
        final int cardY = setupScreenCard(g2d, width, height, cardW, cardH, RenderUtils.yellow, pulse);

        final String text = "LEVEL COMPLETE";
        g2d.setFont(UIScale.scaleFont(RenderCache.AUDIOWIDE_78));
        RenderUtils.drawText(g2d, text, (width - g2d.getFontMetrics().stringWidth(text)) / 2, cardY + UIScale.scale(110), RenderUtils.yellow);

        drawCardLine(g2d, width, cardW, cardY + UIScale.scale(140), FINISHED_YELLOW_LINE, pulse);
        drawPostGameScore(g2d, cardY + UIScale.scale(100), width, RenderUtils.yellow);
        drawEndScreenButtons(g2d, width, cardY + cardH - UIScale.scale(80), "Restart", UIAction.RESTART, "Continue", UIAction.QUIT);

        teardownScreenCard(g2d);
    }

    /**
     * Draws the game over screen overlay.
     *
     * @param g2d    the graphics context
     * @param width  the width of the rendering area
     * @param height the height of the rendering area
     */
    public void drawGameOverScreen(Graphics2D g2d, int width, int height) {
        final float pulse = getPulse(600.0);

        g2d.setColor(GAMEOVER_BG);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(GAMEOVER_LINES);
        for (int sy = 0; sy < height; sy += UIScale.scale(4)) {
            g2d.drawLine(0, sy, width, sy);
        }

        final int cardW = UIScale.scale(720);
        final int cardH = gameEngine.isNewHighScore() ? UIScale.scale(520) : UIScale.scale(480);
        final int cardY = setupScreenCard(g2d, width, height, cardW, cardH, GAMEOVER_RED, pulse);

        final String text = "GAME OVER";
        g2d.setFont(UIScale.scaleFont(RenderCache.AUDIOWIDE_85));
        final int x = (width - g2d.getFontMetrics().stringWidth(text)) / 2;
        final int y = cardY + UIScale.scale(120);

        final long t = System.currentTimeMillis();
        final int glitchOffset = (int) (UIScale.scale(4) + Math.sin(t / 80.0) * UIScale.scale(2));
        g2d.setColor(GLITCH_CYAN);
        g2d.drawString(text, x - glitchOffset, y + 1);
        g2d.setColor(GLITCH_RED);
        g2d.drawString(text, x + glitchOffset, y - 1);

        RenderUtils.drawText(g2d, text, x, y, GAMEOVER_RED);

        drawCardLine(g2d, width, cardW, cardY + UIScale.scale(150), GAMEOVER_RED, pulse);
        drawPostGameScore(g2d, cardY + UIScale.scale(110), width, GAMEOVER_RED_LIGHT);
        drawEndScreenButtons(g2d, width, cardY + cardH - UIScale.scale(80), "Restart", UIAction.RESTART, "Main Menu", UIAction.QUIT);

        teardownScreenCard(g2d);
    }

    /**
     * Draws detailed score statistics at the end of a level.
     */
    private void drawPostGameScore(Graphics2D g2d, int titleY, int width, Color accentColor) {
        final String label = "F I N A L   S C O R E";
        final String scoreText = String.format("%,d", gameEngine.getScore());

        int currentY = titleY + UIScale.scale(40);

        g2d.setFont(UIScale.scaleFont(RenderCache.MONTSERRAT_BLACK_16));
        g2d.setColor(SCORE_LABEL_COLOR);
        g2d.drawString(label, (width - g2d.getFontMetrics().stringWidth(label)) / 2, currentY);

        currentY += UIScale.scale(60);
        g2d.setFont(UIScale.scaleFont(RenderCache.AUDIOWIDE_64));
        RenderUtils.drawText(g2d, scoreText, (width - g2d.getFontMetrics().stringWidth(scoreText)) / 2, currentY, accentColor);

        currentY += UIScale.scale(60);

        if (gameEngine.isNewHighScore()) {
            g2d.setFont(UIScale.scaleFont(RenderCache.AUDIOWIDE_36));
            g2d.setColor(RenderUtils.yellow);
            final String hsText = "NEW HIGH SCORE!";
            g2d.drawString(hsText, (width - g2d.getFontMetrics().stringWidth(hsText)) / 2, currentY);
        } else {
            g2d.setFont(UIScale.scaleFont(RenderCache.AUDIOWIDE_36));
            g2d.setColor(new Color(150, 150, 150));
            final String songId = LevelUtil.getCleanSongName(gameEngine.getLevel());
            final String bestText = "BEST: " + String.format("%,d", ScoreManager.getBestScore(songId));
            g2d.drawString(bestText, (width - g2d.getFontMetrics().stringWidth(bestText)) / 2, currentY);
        }
        currentY += UIScale.scale(40);

        final String orbsLabel = "ORBS COLLECTED: " + gameEngine.getCollectedOrbs();
        g2d.setFont(UIScale.scaleFont(RenderCache.MONTSERRAT_BLACK_16));
        g2d.setColor(ORBS_COLOR);
        g2d.drawString(orbsLabel, (width - g2d.getFontMetrics().stringWidth(orbsLabel)) / 2, currentY);
        currentY += UIScale.scale(30);

        final String totalOrbsLabel = "TOTAL ORBS: " + ScoreManager.getCurrency();
        g2d.setFont(UIScale.scaleFont(RenderCache.MONTSERRAT_BLACK_14));
        g2d.setColor(TOTAL_ORBS_COLOR);
        g2d.drawString(totalOrbsLabel, (width - g2d.getFontMetrics().stringWidth(totalOrbsLabel)) / 2, currentY);
    }

    /**
     * Draws the current score and orb count on the screen.
     *
     * @param g2d           the graphics context
     * @param width         the width of the rendering area
     * @param scorePopAlpha the alpha value for the score pop animation
     */
    public void drawScore(Graphics2D g2d, int width, float scorePopAlpha) {
        final double pulse = (Math.sin(System.currentTimeMillis() / 550.0) + 1.0) / 2.0;
        final Integer score = gameEngine.getScore();
        final Color c = switch (score) {
            case Integer i when i < 500 -> RenderUtils.cyan;
            case Integer i when i < 750 -> RenderUtils.green;
            case Integer i when i < 1000 -> RenderUtils.blue;
            case Integer i when i < 1500 -> RenderUtils.purple;
            default -> RenderUtils.yellow;
        };

        final int haloR = (int) (UIScale.scale(70) + pulse * UIScale.scale(18));
        final int centerY = UIScale.scale(68);

        if (cachedHaloPaint == null || lastHaloColor != c || Math.abs(lastHaloPulse - pulse) > 0.05) {
            colors2[0] = RenderCache.customColorWithAlpha(c, (int) (50 + 30 * pulse));
            colors2[1] = RenderCache.BLACK_ALPHA[0];
            cachedHaloPaint = new RadialGradientPaint(width / 2f, centerY, haloR, fractions2, colors2);
            lastHaloColor = c;
            lastHaloPulse = (float) pulse;
        }
        g2d.setPaint(cachedHaloPaint);
        g2d.fillOval(width / 2 - haloR, centerY - haloR, haloR * 2, haloR * 2);

        if (scorePopAlpha > 0) {
            final int popR = (int) (UIScale.scale(90) + (1f - scorePopAlpha) * UIScale.scale(60));
            drawRing(g2d, width / 2, centerY, popR, c, (int) (scorePopAlpha * 80), RenderCache.STROKE_2);
            g2d.setStroke(RenderCache.STROKE_1);
        }

        final String label = "S  C  O  R  E";
        g2d.setFont(UIScale.scaleFont(RenderCache.MONTSERRAT_BLACK_12));
        g2d.setColor(SCORE_TEXT_COLOR);
        g2d.drawString(label, (width - g2d.getFontMetrics().stringWidth(label)) / 2, UIScale.scale(30));


        g2d.setColor(RenderCache.customColorWithAlpha(c, (int) (100 + 80 * pulse)));
        g2d.drawLine(width / 2 - UIScale.scale(35), UIScale.scale(35), width / 2 + UIScale.scale(35), UIScale.scale(35));

        final String text = Integer.toString(score);
        g2d.setFont(UIScale.scaleFont(RenderCache.AUDIOWIDE_48));
        RenderUtils.drawText(g2d, text, (width - g2d.getFontMetrics().stringWidth(text)) / 2, UIScale.scale(88), c);

        final String orbsText = String.valueOf(gameEngine.getCollectedOrbs());
        g2d.setFont(UIScale.scaleFont(RenderCache.MONTSERRAT_BLACK_24));
        final int orbsW = g2d.getFontMetrics().stringWidth(orbsText);

        final int boxW = orbsW + UIScale.scale(50);
        final int boxH = UIScale.scale(40);
        final int boxX = UIScale.scale(20);
        final int boxY = UIScale.scale(20);

        colors2[0] = RenderCache.customColorWithAlpha(ORBS_COLOR, 30);
        colors2[1] = RenderCache.BLACK_ALPHA[0];
        g2d.setPaint(new RadialGradientPaint(boxX + boxW / 2f, boxY + boxH / 2f, boxW * 0.8f, fractions2, colors2));
        rectScratch.setRoundRect(boxX - UIScale.scale(10), boxY - UIScale.scale(10), boxW + UIScale.scale(20), boxH + UIScale.scale(20), UIScale.scale(20), UIScale.scale(20));
        g2d.fill(rectScratch);

        g2d.setColor(new Color(0, 0, 0, 150));
        rectScratch.setRoundRect(boxX, boxY, boxW, boxH, UIScale.scale(15), UIScale.scale(15));
        g2d.fill(rectScratch);

        g2d.setColor(RenderCache.customColorWithAlpha(ORBS_COLOR, (int) (100 + 50 * pulse)));
        g2d.setStroke(RenderCache.STROKE_2);
        g2d.draw(rectScratch);
        g2d.setStroke(RenderCache.STROKE_1);

        g2d.setColor(ORBS_COLOR);
        final int orbR = UIScale.scale(8);
        final int orbX = boxX + UIScale.scale(16);
        final int orbY = boxY + boxH / 2;
        g2d.fillOval(orbX - orbR, orbY - orbR, orbR * 2, orbR * 2);

        g2d.setColor(ORBS_TEXT_COLOR);
        g2d.drawString(orbsText, boxX + UIScale.scale(35), boxY + UIScale.scale(28));
        gameEngine.getScorePopups().forEach(popup -> popup.paint(g2d, width));
    }

    /**
     * Draws the song progress bar at the bottom of the screen.
     *
     * @param g2d    the graphics context
     * @param width  the width of the rendering area
     * @param height the height of the rendering area
     */
    public void drawProgressBar(Graphics2D g2d, int width, int height) {
        if (clip == null) return;
        final double current = clip.getMicrosecondPosition() / 1_000_000.0;
        final double total = clip.getMicrosecondLength() / 1_000_000.0;
        final double progress = Math.min(1.0, current / total);

        final int barY = height - UIScale.scale(16);
        final int barH = UIScale.scale(15);
        g2d.setColor(PROGRESS_BG);
        g2d.fillRoundRect(0, barY, width, barH, UIScale.scale(3), UIScale.scale(3));

        final int fillW = (int) (width * progress);
        if (fillW > 3) {
            if (cachedProgressGradient == null || cachedProgressWidth != width) {
                colors3[0] = RenderUtils.cyan;
                colors3[1] = RenderUtils.purple;
                colors3[2] = RenderUtils.yellow;
                cachedProgressGradient = new LinearGradientPaint(0, barY, width, barY, fractions3, colors3);
                cachedProgressWidth = width;
            }
            g2d.setPaint(cachedProgressGradient);
            g2d.fillRoundRect(0, barY, fillW, barH, UIScale.scale(3), UIScale.scale(3));

            if (fillW < width) {
                if (cachedProgressGlow == null) {
                    colors2[0] = RenderCache.whiteWithAlpha(200);
                    colors2[1] = RenderCache.whiteWithAlpha(0);
                    cachedProgressGlow = new RadialGradientPaint(0, 0, UIScale.scale(14), fractions2, colors2);
                }
                g2d.translate(fillW, barY + barH / 2f);
                g2d.setPaint(cachedProgressGlow);
                g2d.fillOval(-UIScale.scale(14), -UIScale.scale(16), UIScale.scale(28), UIScale.scale(33));
                g2d.translate(-fillW, -(barY + barH / 2f));
            }
        }

        g2d.setFont(UIScale.scaleFont(RenderCache.MONTSERRAT_BLACK_18));
        g2d.setColor(RenderCache.whiteWithAlpha(150));
        timeStringBuilder.setLength(0);
        timeStringBuilder.append((int) current / 60).append(":");
        final int currentSec = (int) current % 60;
        if (currentSec < 10) timeStringBuilder.append("0");
        timeStringBuilder.append(currentSec).append("  /  ");
        timeStringBuilder.append((int) total / 60).append(":");
        final int totalSec = (int) total % 60;
        if (totalSec < 10) timeStringBuilder.append("0");
        timeStringBuilder.append(totalSec);
        g2d.drawString(timeStringBuilder.toString(), UIScale.scale(10), barY - UIScale.scale(7));

        final String songInfo = songTitle + "  -  " + songArtist;
        final int infoW = g2d.getFontMetrics().stringWidth(songInfo);
        g2d.setColor(RenderCache.whiteWithAlpha(130));
        g2d.drawString(songInfo, width - infoW - UIScale.scale(10), barY - UIScale.scale(7));
    }

    /**
     * Renders a simulated button region.
     */
    private void drawButton(Graphics2D g2d, String label, int x, int y, int width, UIAction action) {
        final int btnHeight = UIScale.scale(55);
        final int btnY = y - UIScale.scale(40);
        final SimulatedButton button = getButtonFromPool(label, x, btnY, width, btnHeight, action);
        button.draw(g2d, mouseX, mouseY, currentTranslateY);

        SimulatedButton absoluteRegion = new SimulatedButton(label, x, btnY + currentTranslateY, width, btnHeight, action);
        activeButtons.add(absoluteRegion);
    }

    /**
     * Retrieves a button from the pool or creates a new one if the pool is exhausted.
     *
     * @param label  the button label
     * @param x      the x-coordinate
     * @param y      the y-coordinate
     * @param width  the button width
     * @param height the button height
     * @param action the action associated with the button
     * @return a configured {@link SimulatedButton}
     */
    private SimulatedButton getButtonFromPool(String label, int x, int y, int width, int height, UIAction action) {
        final SimulatedButton btn;
        if (buttonPoolIndex < buttonPool.size()) {
            btn = buttonPool.get(buttonPoolIndex++);
            btn.setup(label, x, y, width, height, action);
        } else {
            btn = new SimulatedButton(label, x, y, width, height, action);
            buttonPool.add(btn);
            buttonPoolIndex++;
        }
        return btn;
    }

    /**
     * Draws the revive selection screen overlay.
     *
     * @param g2d    the graphics context
     * @param width  the width of the rendering area
     * @param height the height of the rendering area
     */
    public void drawReviveScreen(Graphics2D g2d, int width, int height) {
        final float pulse = getPulse(600.0);

        g2d.setColor(GAMEOVER_BG);
        g2d.fillRect(0, 0, width, height);

        final int cardW = UIScale.scale(680);
        final int cardH = UIScale.scale(500);

        final boolean canRevive = gameEngine.canRevive();
        final Color cardColor = canRevive ? ORBS_COLOR : GAMEOVER_RED;

        final int cardY = setupScreenCard(g2d, width, height, cardW, cardH, cardColor, pulse);

        final String text = "YOU FELL";
        g2d.setFont(UIScale.scaleFont(RenderCache.AUDIOWIDE_64));
        RenderUtils.drawText(g2d, text, (width - g2d.getFontMetrics().stringWidth(text)) / 2, cardY + UIScale.scale(110), cardColor);

        drawCardLine(g2d, width, cardW, cardY + UIScale.scale(135), cardColor, pulse);

        int currentY = cardY + UIScale.scale(185);
        g2d.setFont(UIScale.scaleFont(RenderCache.MONTSERRAT_BLACK_18));
        g2d.setColor(SCORE_TEXT_COLOR);
        final String scoreText = "Score so far: " + String.format("%,d", gameEngine.getScore());
        g2d.drawString(scoreText, (width - g2d.getFontMetrics().stringWidth(scoreText)) / 2, currentY);

        currentY += UIScale.scale(35);
        g2d.setColor(ORBS_COLOR);
        final String orbsText = "Orbs collected: " + gameEngine.getCollectedOrbs();
        g2d.drawString(orbsText, (width - g2d.getFontMetrics().stringWidth(orbsText)) / 2, currentY);

        currentY += UIScale.scale(55);
        g2d.setFont(UIScale.scaleFont(RenderCache.MONTSERRAT_BLACK_28));
        if (canRevive) {
            g2d.setColor(ORBS_TEXT_COLOR);
            final String reviveText = "REVIVE Cost: " + gameEngine.getReviveCost() + " orbs";
            g2d.drawString(reviveText, (width - g2d.getFontMetrics().stringWidth(reviveText)) / 2, currentY);
        } else {
            g2d.setColor(GAMEOVER_RED_LIGHT);
            final String reason = gameEngine.getRevivesUsed() >= ReviveManager.MAX_REVIVES ? "No revives left" : "Not enough orbs";
            final String reviveText = "CANNOT REVIVE (" + reason + ")";
            g2d.drawString(reviveText, (width - g2d.getFontMetrics().stringWidth(reviveText)) / 2, currentY);
        }

        currentY += UIScale.scale(35);
        g2d.setFont(UIScale.scaleFont(RenderCache.MONTSERRAT_BLACK_14));
        g2d.setColor(TOTAL_ORBS_COLOR);
        final String totalOrbs = "Your orbs: " + ScoreManager.getCurrency();
        g2d.drawString(totalOrbs, (width - g2d.getFontMetrics().stringWidth(totalOrbs)) / 2, currentY);

        final int hintY = cardY + cardH - UIScale.scale(80);
        if (canRevive) {
            drawEndScreenButtons(g2d, width, hintY, "Revive", UIAction.REVIVE, "Quit", UIAction.DECLINE_REVIVE);
        } else {
            drawButton(g2d, "Quit", width / 2 - UIScale.scale(110), hintY, UIScale.scale(220), UIAction.DECLINE_REVIVE);
        }

        teardownScreenCard(g2d);
    }

    /**
     * Entry point for rendering the game UI based on current state.
     *
     * @param g2d    the graphics context
     * @param width  rendering area width
     * @param height rendering area height
     * @param state  the current game state
     */
    public void renderGameState(Graphics2D g2d, int width, int height, GameState state) {
        activeButtons.clear();
        buttonPoolIndex = 0;
        currentTranslateY = 0;
        switch (state) {
            case PLAYING -> drawTutorial(g2d, width, height);
            case COUNTDOWN -> {
                drawCountdown(g2d, width, height);
                drawTutorial(g2d, width, height);
            }
            case PAUSED -> drawPauseScreen(g2d, width, height);
            case FINISHED -> drawFinishedScreen(g2d, width, height);
            case GAME_OVER -> {
                if (gameEngine.getRevivesUsed() < ReviveManager.MAX_REVIVES && !gameEngine.isReviveDeclined()) {
                    drawReviveScreen(g2d, width, height);
                } else {
                    drawGameOverScreen(g2d, width, height);
                }
            }
        }
        this.renderedButtons = new ArrayList<>(activeButtons);
    }
}
