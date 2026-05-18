package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.controller.GameController;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.WindowData;
import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.game.GameEngine;
import cz.matysekxx.beatbounce.model.game.ReviveManager;
import cz.matysekxx.beatbounce.model.game.state.GameState;
import cz.matysekxx.beatbounce.model.level.Level;
import cz.matysekxx.beatbounce.util.Time;

import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * The main panel for the game, handling rendering, user input, and the game loop.
 */
public class GamePanel extends JPanel implements Runnable {
    /**
     * Number of particles for game background effects.
     */
    private static final int MAX_PARTICLES = 20;
    private static final GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice().getDefaultConfiguration();
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
     * Particle array for ambient background animation.
     */
    private final Particle[] particles;
    /**
     * The primary game loop thread.
     */
    private Thread gameThread;
    /**
     * The audio clip for the current level's song.
     */
    private Clip clip;
    /**
     * The core game logic model.
     */
    private GameEngine gameEngine;
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
     * Helper for rendering the 3D world environment.
     */
    private GameWorldRenderer worldRenderer;
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
     * Off-screen buffer for double-buffered rendering.
     */
    private BufferedImage backBuffer;
    /**
     * Current particle count based on quality settings.
     */
    private int particleCount;
    /**
     * Accumulated time for animations (seconds).
     */
    private float animTime = 0f;


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
        this.particles = new Particle[MAX_PARTICLES];
        for (int i = 0; i < particles.length; i++)
            particles[i] = new Particle(1920, 540);
        updateParticleCount();
    }

    private void updateParticleCount() {
        this.particleCount = switch (Settings.graphicsQuality) {
            case "LOW" -> 0;
            case "MEDIUM" -> 10;
            default -> MAX_PARTICLES;
        };
    }

    /**
     * Initializes the game panel with the specified level.
     *
     * @param level the level to play
     */
    public void init(Level level) {
        this.clip = level.audioData().clip();
        final Sphere sphere = new Sphere(0, 150, 0, 25);
        this.gameEngine = new GameEngine(level, sphere, cam, clip);
        this.lastScore = 0;
        this.scorePopAlpha = 0f;
        this.uiRenderer = new GameUIRenderer(gameEngine, clip);
        this.worldRenderer = new GameWorldRenderer(cam, gameEngine, level, sphere);
        this.addMouseMotionListener(new GameController(cam, sphere));
        final MouseAdapter uiMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                if (uiRenderer == null) return;
                final double REFERENCE_HEIGHT = 1440.0;
                final double uiScale = getHeight() / REFERENCE_HEIGHT;
                final int virtualX = (int) (e.getX() / uiScale);
                final int virtualY = (int) (e.getY() / uiScale);
                uiRenderer.setMousePosition(virtualX, virtualY);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (uiRenderer == null || gameEngine == null) return;
                final double REFERENCE_HEIGHT = 1440.0;
                final double uiScale = getHeight() / REFERENCE_HEIGHT;
                final int virtualX = (int) (e.getX() / uiScale);
                final int virtualY = (int) (e.getY() / uiScale);

                final UIAction action = uiRenderer.handleClick(virtualX, virtualY);
                if (action != UIAction.NONE) {
                    new Thread(() -> {
                        switch (action) {
                            case RESUME -> gameEngine.togglePause();
                            case RESTART -> gameEngine.init();
                            case QUIT -> {
                                stopGame();
                                if (onExit != null) onExit.run();
                            }
                            case REVIVE -> gameEngine.revive();
                            case DECLINE_REVIVE -> gameEngine.declineRevive();
                            default -> {
                            }
                        }
                    }).start();
                }
            }
        };
        this.addMouseListener(uiMouseAdapter);
        this.addMouseMotionListener(uiMouseAdapter);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameEngine == null) return;
                final GameState state = gameEngine.getGameState();
                switch (state) {
                    case PLAYING, COUNTDOWN, PAUSED -> {
                        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) gameEngine.togglePause();
                        else if (state == GameState.PAUSED && e.getKeyCode() == KeyEvent.VK_ENTER) {
                            stopGame();
                            if (onExit != null) onExit.run();
                        }
                    }
                    case GAME_OVER -> {
                        if (gameEngine.getRevivesUsed() < ReviveManager.MAX_REVIVES && !gameEngine.isReviveDeclined()) {
                            if (e.getKeyCode() == KeyEvent.VK_V) {
                                gameEngine.revive();
                            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                                gameEngine.declineRevive();
                            }
                        } else {
                            if (e.getKeyCode() == KeyEvent.VK_R) {
                                gameEngine.init();
                            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                                stopGame();
                                if (onExit != null) onExit.run();
                            }
                        }
                    }
                    case FINISHED -> {
                        if (e.getKeyCode() == KeyEvent.VK_R) {
                            gameEngine.init();
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
                    backBuffer = gc.createCompatibleImage(w, h, Transparency.OPAQUE);
                }
            }
        });
    }

    @Override
    public void run() {
        final long optimalTimeNanos = 1_000_000_000L / Settings.targetFps;
        while (running) {
            final long loopStartTime = System.nanoTime();
            final float dt = (float) ((loopStartTime - lastFrameTime) / 1_000_000_000.0);
            lastFrameTime = loopStartTime;
            animTime += dt;

            updateParticleCount();
            final double currentTime = (clip != null && clip.isRunning()) ? clip.getMicrosecondPosition() / 1_000_000.0 : 0;
            gameEngine.update(currentTime, dt);
            updateCursorVisibility();
            if (uiRenderer != null) uiRenderer.update(dt);

            if (Settings.particlesEnabled) {
                final int w = (cachedW > 0) ? cachedW : 1920;
                final int h = (cachedH > 0) ? cachedH : 1080;
                final double REFERENCE_HEIGHT = 1440.0;
                final double scale = h / REFERENCE_HEIGHT;
                final int virtualW = (int) (w / scale);
                final int virtualH = (int) (h / scale);
                Particle.updateAll(particles, particleCount, dt, virtualW, virtualH);
            }

            final int currentScore = gameEngine.getScore();
            if (currentScore != lastScore) {
                scorePopAlpha = 1.0f;
                lastScore = currentScore;
            }

            if (scorePopAlpha > 0) {
                scorePopAlpha -= (float) (dt * 3.0);
                if (scorePopAlpha < 0) scorePopAlpha = 0;
            }

            if (Settings.showFps) {
                frames++;
                final long nowTime = System.currentTimeMillis();
                if (nowTime - lastFpsTime >= 1000) {
                    currentUpdateFps = frames;
                    frames = 0;
                    lastFpsTime = nowTime;
                }
            }
            renderGame();

            Time.delay(optimalTimeNanos, loopStartTime);
        }
    }

    /**
     * Starts the game loop.
     */
    public void startGame() {
        if (!this.running) {
            this.running = true;
            gameEngine.init();
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

        if (gameEngine != null) {
            gameEngine.stop();
            if (gameEngine.getLevel() != null && gameEngine.getLevel().audioData() != null) {
                gameEngine.getLevel().audioData().close();
            }
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
        if (gameEngine == null) return;
        final GameState state = gameEngine.getGameState();
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
            backBuffer = gc.createCompatibleImage(w, h, Transparency.OPAQUE);
        }

        final Graphics2D g2d = backBuffer.createGraphics();
        RenderUtils.initGraphics2D(g2d);
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, w, h);

        final int horizonY = h / 3;
        final long time = System.currentTimeMillis();
        final float globalHue = (animTime * 0.02f) % 1.0f;
        if (gameEngine.getGameState() != GameState.FINISHED) {
            worldRenderer.drawBackground(g2d, w, h, horizonY);
        }
        final double REFERENCE_HEIGHT = 1440.0;
        final double uiScale = h / REFERENCE_HEIGHT;
        final int virtualW = (int) (w / uiScale);
        final int virtualH = (int) (h / uiScale);
        final int virtualHorizonY = virtualH / 3;

        final AffineTransform oldTransform = g2d.getTransform();
        g2d.scale(uiScale, uiScale);
        if (gameEngine.getGameState() != GameState.FINISHED) {
            worldRenderer.drawPlanetAndGrid(g2d, virtualW, virtualH, virtualHorizonY, time, globalHue);
            if (Settings.particlesEnabled) {
                Particle.drawAll(g2d, particles, particleCount);
            }
            final WindowData virtualWindowData = WindowData.of(virtualW, virtualH);
            worldRenderer.drawGameObjects(g2d, virtualWindowData);
            uiRenderer.drawProgressBar(g2d, virtualW, virtualH);
            uiRenderer.drawScore(g2d, virtualW, scorePopAlpha);
        }

        if (gameEngine != null && gameEngine.getNeonFlashAlpha() > 0) {
            final int flashAlpha = Math.min(255, (int) (gameEngine.getNeonFlashAlpha() * 255));
            g2d.setColor(RenderCache.blackWithAlpha(flashAlpha));
            g2d.fillRect(0, 0, virtualW, virtualH);
        }
        assert gameEngine != null;
        uiRenderer.renderGameState(g2d, virtualW, virtualH, gameEngine.getGameState());

        g2d.setTransform(oldTransform);

        if (Settings.showFps) {
            g2d.setColor(Color.YELLOW);
            g2d.setFont(RenderCache.MONO_BOLD_16);
            g2d.drawString("FPS: " + currentUpdateFps, 10, 20);
        }

        g2d.dispose();
        g.drawImage(backBuffer, 0, 0, null);
        g.dispose();
        if (Settings.vsync) Toolkit.getDefaultToolkit().sync();
    }
}
