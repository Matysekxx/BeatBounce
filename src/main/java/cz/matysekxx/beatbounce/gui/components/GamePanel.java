package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.controller.GameController;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.WindowData;
import cz.matysekxx.beatbounce.model.GameModel;
import cz.matysekxx.beatbounce.model.GameState;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.Orb;
import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.level.Level;

import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * The main panel for the game, handling rendering, user input, and the game loop.
 */
public class GamePanel extends JPanel implements Runnable {
    private static final Color FPS_COLOR = Color.YELLOW;
    private static final Color GRID_MAGENTA_90 = new Color(255, 0, 255, 90);
    private static final Color GRID_CYAN_110 = new Color(0, 255, 255, 110);
    private static final Color GRID_CYAN_120 = new Color(0, 255, 255, 120);

    /**
     * The 3D camera used for projecting game coordinates to the screen.
     */
    private final Camera3D cam;

    /**
     * Callback invoked when the game session is closed or exited.
     */
    private final Runnable onExit;

    /**
     * Transparent cursor used to hide the mouse during gameplay.
     */
    private final Cursor blankCursor;

    /**
     * The primary game loop thread.
     */
    private Thread gameThread;

    /**
     * The player-controlled sphere entity.
     */
    private Sphere sphere;

    /**
     * The current level being played.
     */
    private Level level;

    /**
     * The audio clip for the current level's song.
     */
    private Clip clip;

    /**
     * The core game logic model.
     */
    private GameModel gameModel;

    /**
     * Flag indicating if the game loop is active.
     */
    private boolean running;

    /**
     * Timestamp of the previous frame for delta time calculation.
     */
    private long lastFrameTime;

    /**
     * Current state of cursor visibility.
     */
    private boolean isCursorHidden = false;

    /**
     * The score from the previous update, used to trigger animations.
     */
    private int lastScore = 0;

    /**
     * Alpha value for the score "pop" animation.
     */
    private float scorePopAlpha = 0f;

    /**
     * Helper for rendering game-specific UI elements.
     */
    private GameUIRenderer uiRenderer;

    /**
     * Cached background image to optimize rendering performance.
     */
    private BufferedImage bgCache;

    /**
     * Cached width of the panel for background re-generation.
     */
    private int cachedW = -1;

    /**
     * Cached height of the panel for background re-generation.
     */
    private int cachedH = -1;

    /**
     * Frame counter for FPS calculation.
     */
    private int frames = 0;

    /**
     * Timestamp of the last FPS update.
     */
    private long lastFpsTime = 0;

    /**
     * The most recently calculated FPS value.
     */
    private int currentUpdateFps = 0;

    /**
     * Dimensions of the panel used for coordinate projections.
     */
    private WindowData frameWindowData;

    /**
     * Off-screen buffer for double-buffered rendering.
     */
    private BufferedImage backBuffer;

    /**
     * Constructs a new GamePanel.
     *
     * @param onExit a callback executed when the game is exited
     */
    public GamePanel(Runnable onExit) {
        this.onExit = onExit;
        this.running = false;
        this.setLayout(new BorderLayout());
        this.setFocusable(true);
        this.setBackground(Color.BLACK);
        this.cam = new Camera3D(0, 0, -500, 500.0);
        this.setDoubleBuffered(false);
        this.setIgnoreRepaint(true);
        this.setOpaque(true);
        this.blankCursor = RenderUtils.blankCursor;
    }

    /**
     * Initializes the game panel with the specified level.
     *
     * @param level the level to play
     */
    public void init(Level level) {
        this.level = level;
        this.clip = level.audioData().clip();
        this.sphere = new Sphere(0, 150, 0, 25);
        this.gameModel = new GameModel(level, sphere, cam, clip);
        this.lastScore = 0;
        this.scorePopAlpha = 0f;
        this.uiRenderer = new GameUIRenderer(gameModel, clip);
        this.addMouseMotionListener(new GameController(cam, sphere));
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameModel == null) return;
                final GameState state = gameModel.getGameState();
                switch (state) {
                    case PLAYING, COUNTDOWN, PAUSED -> {
                        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) gameModel.togglePause();
                        else if (state == GameState.PAUSED && e.getKeyCode() == KeyEvent.VK_ENTER) {
                            stopGame();
                            if (onExit != null) onExit.run();
                        }
                    }
                    case GAME_OVER, FINISHED -> {
                        if (e.getKeyCode() == KeyEvent.VK_R) {
                            gameModel.init();
                        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                            stopGame();
                            if (onExit != null) onExit.run();
                        }
                    }
                }
            }
        });
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                final int w = e.getComponent().getWidth();
                final int h = e.getComponent().getHeight();
                if (w != cachedW) cachedW = w;
                if (h != cachedH) cachedH = h;
                if (backBuffer == null || backBuffer.getWidth(null) != w || backBuffer.getHeight(null) != h) {
                    final GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .getDefaultScreenDevice().getDefaultConfiguration();
                    backBuffer = gc.createCompatibleImage(w, h, Transparency.OPAQUE);

                }
            }
        });
    }

    /**
     * Starts the game loop.
     */
    public void startGame() {
        if (!this.running) {
            this.running = true;
            gameModel.init();
            this.lastFrameTime = System.nanoTime();
            this.lastFpsTime = System.currentTimeMillis();
            this.frames = 0;
            this.currentUpdateFps = 0;
            this.gameThread = new Thread(this);
            this.gameThread.setPriority(Thread.MAX_PRIORITY);
            this.gameThread.start();
            this.requestFocusInWindow();
        }
    }

    /**
     * Stops the game loop and clean up resources.
     */
    public void stopGame() {
        if (!this.running) return;
        this.running = false;

        if (getCursor() == blankCursor) {
            setCursor(Cursor.getDefaultCursor());
        }

        if (gameModel != null) {
            gameModel.stop();
        }
        if (gameThread != null) {
            gameThread.interrupt();
            try {
                gameThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Updates the mouse cursor visibility based on the current game state.
     */
    private void updateCursorVisibility() {
        if (gameModel == null) return;
        final GameState state = gameModel.getGameState();
        final boolean shouldHide = (state == GameState.PLAYING || state == GameState.FALLING);
        if (shouldHide && !isCursorHidden) {
            setCursor(blankCursor);
            isCursorHidden = true;
        } else if (!shouldHide && isCursorHidden) {
            setCursor(Cursor.getDefaultCursor());
            isCursorHidden = false;
        }
    }

    /**
     * Orchestrates the rendering process, including double buffering and UI drawing.
     */
    private void renderGame() {
        final Graphics g = getGraphics();
        if (g == null) return;

        final int w = cachedW;
        final int h = cachedH;
        if (w <= 0 || h <= 0) return;
        if (backBuffer == null || backBuffer.getWidth(null) != w || backBuffer.getHeight(null) != h) {
            final GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            backBuffer = gc.createCompatibleImage(w, h, Transparency.OPAQUE);
        }

        final Graphics2D g2d = backBuffer.createGraphics();
        RenderUtils.initGraphics2D(g2d);
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, w, h);

        final int horizonY = h / 3;
        final long time = System.currentTimeMillis();
        frameWindowData = WindowData.of(w, h);

        if (gameModel.getGameState() != GameState.FINISHED) {
            drawEnvironment(g2d, w, h, horizonY, time);
            drawGameObjects(g2d);
            uiRenderer.drawProgressBar(g2d, w, h);
            uiRenderer.drawScore(g2d, w, scorePopAlpha);
        }

        if (gameModel != null && gameModel.getNeonFlashAlpha() > 0) {
            final float alpha = Math.min(1f, gameModel.getNeonFlashAlpha());
            g2d.setColor(new Color(0f, 0f, 0f, alpha));
            g2d.fillRect(0, 0, w, h);
        }
        drawByGameState(g2d, w, h);

        if (Settings.showFps) {
            g2d.setColor(FPS_COLOR);
            g2d.setFont(RenderCache.MONO_BOLD_16);
            g2d.drawString("FPS: " + currentUpdateFps, 10, 20);
        }

        g2d.dispose();

        final Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2);
        g2.drawImage(backBuffer, 0, 0, null);
        g2.dispose();
        if (Settings.vsync) Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Draws state-specific UI screens (e.g., Pause, Game Over).
     *
     * @param g2d The Graphics2D context.
     * @param w   Panel width.
     * @param h   Panel height.
     */
    private void drawByGameState(Graphics2D g2d, int w, int h) {
        switch (gameModel.getGameState()) {
            case COUNTDOWN -> uiRenderer.drawCountdown(g2d, w, h);
            case PAUSED -> uiRenderer.drawPauseScreen(g2d, w, h);
            case FINISHED -> uiRenderer.drawFinishedScreen(g2d, w, h);
            case GAME_OVER -> uiRenderer.drawGameOverScreen(g2d, w, h);
        }
    }

    /**
     * Renders the game environment, including the floor and background.
     *
     * @param g2d      The Graphics2D context.
     * @param width    Panel width.
     * @param height   Panel height.
     * @param horizonY Vertical coordinate of the horizon.
     * @param time     Current system time for animations.
     */
    private void drawEnvironment(Graphics2D g2d, int width, int height, int horizonY, long time) {
        if (bgCache == null || cachedW != width || cachedH != height) {
            this.cachedW = width;
            this.cachedH = height;
            this.bgCache = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration()
                    .createCompatibleImage(width, height, Transparency.OPAQUE);
            final Graphics2D cg = bgCache.createGraphics();
            RenderUtils.initGraphics2D(cg);
            RenderUtils.drawBackground(cg, width, height);
            RenderUtils.drawFloor(cg, width, height, horizonY);
            cg.dispose();
        }
        g2d.drawImage(bgCache, 0, 0, null);

        drawPlanet(g2d, width, horizonY, time);
        RenderUtils.drawHorizonLine(g2d, width, horizonY);
        drawNeonGrid(g2d, width, height, horizonY);
    }

    /**
     * Renders a stylized planet with rings in the background.
     *
     * @param g2d      The Graphics2D context.
     * @param width    Panel width.
     * @param horizonY Vertical coordinate of the horizon.
     * @param time     Current system time for animations.
     */
    private void drawPlanet(Graphics2D g2d, int width, int horizonY, long time) {
        final int cx = width / 2;
        final int cy = horizonY - 150;
        final int r = 100;
        final float t = time / 1000.0f;
        final float pulse = (float) ((Math.sin(t * 1.5) + 1.0) / 2.0);

        final int glowR = (int) (r * (2.f + pulse * 0.15f));
        g2d.setPaint(new RadialGradientPaint(cx, cy, glowR, new float[]{0f, 1f}, new Color[]{new Color(0, 200, 255, (int) (15 + pulse * 30)), new Color(0, 200, 255, 0)}));
        g2d.fillOval(cx - glowR, cy - glowR, glowR * 2, glowR * 2);

        final int ry = cy + (int) (Math.sin(t * 0.4) * 8);

        drawRing(g2d, cx, ry, r * 1.8f, 28, 0, 180, new Color(200, 0, 255, 60), 1f);
        drawRing(g2d, cx, ry, r * 1.4f, 18, 0, 180, new Color(0, 255, 255, 40), 1f);

        g2d.setPaint(new RadialGradientPaint(cx - r / 2.5f, cy - r / 2.5f, r * 1.5f, new float[]{0f, 1f}, new Color[]{new Color(45, 15, 80), new Color(10, 0, 25)}));
        g2d.fillOval(cx - r, cy - r, r * 2, r * 2);

        if (!Settings.graphicsQuality.equals("LOW")) {
            g2d.setColor(GRID_CYAN_120);
            g2d.setStroke(RenderCache.STROKE_2_5);
            g2d.drawOval(cx - r, cy - r, r * 2, r * 2);

            drawRing(g2d, cx, ry, r * 1.4f, 18, 180, 180, RenderCache.cyanWithAlpha((int) (180 + 75 * pulse)), 2f);
            drawRing(g2d, cx, ry, r * 1.8f, 28, 180, 180, RenderCache.magentaWithAlpha((int) (140 + 60 * pulse)), 2.5f);
            drawRing(g2d, cx, ry, r * 1.8f, 28, 180, 180, new Color(255, 200, 255, 200), 1f);
        }

        g2d.setStroke(RenderCache.STROKE_1);
    }

    /**
     * Helper to draw a stylized arc/ring segment.
     *
     * @param g2d        The Graphics2D context.
     * @param cx         Center X.
     * @param cy         Center Y.
     * @param rx         Horizontal radius.
     * @param ry         Vertical radius.
     * @param startAngle Arc start angle.
     * @param arcAngle   Arc angular extent.
     * @param color      Color of the ring.
     * @param stroke     Thickness of the line.
     */
    private void drawRing(
            Graphics2D g2d, int cx, int cy, float rx, int ry, int startAngle, int arcAngle, Color color, float stroke
    ) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(stroke));
        g2d.drawArc(cx - (int) rx, cy - ry, (int) (rx * 2), ry * 2, startAngle, arcAngle);
    }

    /**
     * Renders all active game objects (tiles, orbs, sphere) in 3D.
     *
     * @param g2d The Graphics2D context.
     */
    private void drawGameObjects(Graphics2D g2d) {
        if (gameModel == null || gameModel.getGameState() != GameState.FINISHED) {
            final List<AbstractTile> tiles = level.tiles();
            for (int i = tiles.size() - 1; i >= 0; i--) {
                final AbstractTile tile = tiles.get(i);
                final double distance = cam.getDistanceTo(tile.getZ());
                final double tileDepth = distance + tile.getLengthInZ();
                if (tileDepth <= 0 || distance > 3000) continue;
                tile.paint3D(g2d, cam, frameWindowData);
            }

            if (gameModel != null) {
                for (Orb orb : gameModel.getOrbs()) {
                    final double distance = cam.getDistanceTo(orb.getZ());
                    if (distance > 0 && distance < 3000) {
                        orb.paint3D(g2d, cam, frameWindowData);
                    }
                }
            }
        }
        sphere.paint3D(g2d, cam, frameWindowData);
    }

    /**
     * Projects 3D world coordinates to 2D screen coordinates.
     *
     * @param x        World X.
     * @param y        World Y.
     * @param z        World Z.
     * @param width    Screen width.
     * @param horizonY Screen vertical center.
     * @return A 2D Point, or null if the coordinate is behind the camera.
     */
    private Point projectPoint(double x, double y, double z, int width, int horizonY) {
        final double scale = cam.getScale(z);
        if (scale <= 0) return null;
        final int px = (int) (width / 2.0 + (x - cam.getX()) * scale);
        final int py = (int) (horizonY + ((y - cam.getY()) * scale));
        return new Point(px, py);
    }

    /**
     * Renders the perspective neon grid on the floor.
     *
     * @param g2d      The Graphics2D context.
     * @param width    Panel width.
     * @param height   Panel height.
     * @param horizonY Vertical coordinate of the horizon.
     */
    private void drawNeonGrid(Graphics2D g2d, int width, int height, int horizonY) {
        for (int z = 0; z < 3000; z += 150) {
            final double distance = z - (cam.getZ() % 150);
            if (distance <= 0) continue;
            final Point p = projectPoint(0, 150, cam.getZ() + distance, width, horizonY);

            if (p != null && p.y >= horizonY && p.y <= height) {
                final int alpha = (int) Math.max(0, Math.min(120, 255 - (distance / 3000.0 * 255)));
                g2d.setColor(RenderCache.cyanWithAlpha(alpha));
                g2d.drawLine(0, p.y, width, p.y);
            }
        }

        final int[] laneXs = {-300, -180, -60, 60, 180, 300};
        g2d.setStroke(RenderCache.STROKE_2);
        for (int lx : laneXs) {

            final Point start = projectPoint(lx, 150, cam.getZ() + 100, width, horizonY);
            final Point end = projectPoint(lx, 150, cam.getZ() + 3000, width, horizonY);

            if (start == null || end == null) continue;

            if (Math.abs(lx) > 180) g2d.setColor(GRID_MAGENTA_90);
            else g2d.setColor(GRID_CYAN_110);
            g2d.drawLine(start.x, start.y, end.x, end.y);
        }
        g2d.setStroke(RenderCache.STROKE_1);
    }
}