package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.action.ActionQueue;
import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.controller.GameController;
import cz.matysekxx.beatbounce.controller.GameKeyController;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.WindowData;
import cz.matysekxx.beatbounce.model.achievement.AchievementManager;
import cz.matysekxx.beatbounce.model.audio.AudioManager;
import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.game.GameEngine;
import cz.matysekxx.beatbounce.model.game.state.GameState;
import cz.matysekxx.beatbounce.model.level.Level;
import cz.matysekxx.beatbounce.util.Time;
import cz.matysekxx.beatbounce.util.UIScale;

import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The main panel for the game, handling rendering, user input, and the game loop.
 *
 * @author Matysekxx
 */
public class GamePanel extends JPanel implements Runnable {
    /**
     * Number of particles for game background effects.
     */
    private static final int MAX_PARTICLES = 20;

    /**
     * Graphics configuration for creating compatible images.
     */
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
     * Queue for synchronized processing of UI actions.
     */
    private final ActionQueue actionQueue;
    /**
     * Active achievement toast notifications.
     */
    private final List<ToastNotification> activeToasts = new ArrayList<>();
    /**
     * Listener to spawn new toasts on achievement unlock.
     */
    private final AchievementManager.AchievementListener toastListener = ach -> activeToasts.add(new ToastNotification(ach));
    /**
     * The primary game loop thread.
     */
    private Thread gameThread;
    /**
     * The core game logic model.
     */
    private volatile GameEngine gameEngine;
    /**
     * Flag indicating if the game loop is active.
     */
    private volatile boolean running;
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
     * Mouse motion listener for game control.
     */
    private GameController gameController;
    /**
     * Mouse listener for UI interaction.
     */
    private MouseAdapter uiMouseAdapter;
    /**
     * Key listener for game keyboard controls.
     */
    private GameKeyController gameKeyController;
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
        this.actionQueue = ActionQueue.getSingleton();
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

        this.addComponentListener(new ComponentAdapter() {
            /**
             * Handles the component resized event to update scaling and buffers.
             * @param e the component event
             */
            @Override
            public void componentResized(ComponentEvent e) {
                final int w = e.getComponent().getWidth();
                final int h = e.getComponent().getHeight();
                cachedW = w;
                cachedH = h;
                UIScale.update(w, h);
                if (backBuffer == null || backBuffer.getWidth(null) != w || backBuffer.getHeight(null) != h) {
                    backBuffer = gc.createCompatibleImage(w, h, Transparency.OPAQUE);
                }
            }
        });
    }

    /**
     * Updates the particle count based on the current graphics quality settings.
     */
    private void updateParticleCount() {
        this.particleCount = switch (Settings.graphicsQuality) {
            case "LOW" -> 0;
            case "MEDIUM" -> 10;
            default -> MAX_PARTICLES;
        };
    }

    /**
     * Initializes the game panel with the specified level and song metadata.
     *
     * @param level      the level to play
     * @param songTitle  the title of the song
     * @param songArtist the artist of the song
     */
    public void init(Level level, String songTitle, String songArtist) {
        if (gameController != null) this.removeMouseMotionListener(gameController);
        if (uiMouseAdapter != null) {
            this.removeMouseListener(uiMouseAdapter);
            this.removeMouseMotionListener(uiMouseAdapter);
        }
        if (gameKeyController != null) this.removeKeyListener(gameKeyController);

        this.actionQueue.clear();
        final Clip clip = level.audioData().clip();
        final Sphere sphere = new Sphere(0, 150, 0, 25);
        this.gameEngine = new GameEngine(level, sphere, cam, clip);
        this.lastScore = 0;
        this.scorePopAlpha = 0f;
        this.activeToasts.clear();
        AchievementManager.removeListener(toastListener);
        AchievementManager.addListener(toastListener);
        this.uiRenderer = new GameUIRenderer(gameEngine, clip, songTitle, songArtist);
        this.worldRenderer = new GameWorldRenderer(cam, gameEngine, level, sphere);

        this.gameController = new GameController(cam, sphere, gameEngine);
        this.addMouseMotionListener(gameController);

        this.uiMouseAdapter = new MouseAdapter() {
            /**
             * Handles the mouse moved event to update UI mouse position.
             * @param e the mouse event
             */
            @Override
            public void mouseMoved(MouseEvent e) {
                if (uiRenderer == null) return;
                uiRenderer.setMousePosition(e.getX(), e.getY());
            }

            /**
             * Handles the mouse dragged event to update UI mouse position.
             * @param e the mouse event
             */
            @Override
            public void mouseDragged(MouseEvent e) {
                if (uiRenderer == null) return;
                uiRenderer.setMousePosition(e.getX(), e.getY());
            }

            /**
             * Handles the mouse pressed event to process UI button clicks.
             * @param e the mouse event
             */
            @Override
            public void mousePressed(MouseEvent e) {
                if (uiRenderer == null || gameEngine == null) return;
                final UIAction action = uiRenderer.handleClick(e.getX(), e.getY());
                if (action != UIAction.NONE) {
                    AudioManager.playSFX("/click-sound.mp3");
                    actionQueue.add(() -> {
                        switch (action) {
                            case RESUME -> gameEngine.togglePause();
                            case RESTART -> init(gameEngine.getLevel(), songTitle, songArtist);
                            case QUIT -> {
                                stopGame();
                                if (onExit != null) onExit.run();
                            }
                            case REVIVE -> gameEngine.revive();
                            case DECLINE_REVIVE -> gameEngine.declineRevive();
                        }
                    });
                }
            }
        };
        this.addMouseListener(uiMouseAdapter);
        this.addMouseMotionListener(uiMouseAdapter);

        final Runnable quitAction = () -> {
            stopGame();
            if (onExit != null) onExit.run();
        };
        this.gameKeyController = new GameKeyController(gameEngine, actionQueue, quitAction);
        this.addKeyListener(gameKeyController);

        if (running) {
            gameEngine.init();
            lastFrameTime = System.nanoTime();
        }
    }

    /**
     * The main entry point for the game loop thread.
     * Processes input, updates game state, and triggers rendering.
     */
    @Override
    public void run() {
        final long optimalTimeNanos = 1_000_000_000L / Settings.targetFps;
        while (running) {
            actionQueue.processActions();
            final long loopStartTime = System.nanoTime();
            final float dt = (float) ((loopStartTime - lastFrameTime) / 1_000_000_000.0);
            lastFrameTime = loopStartTime;
            animTime += dt;

            updateParticleCount();
            gameEngine.update(dt);
            updateCursorVisibility();
            if (uiRenderer != null) uiRenderer.update(dt);

            synchronized (activeToasts) {
                final Iterator<ToastNotification> it = activeToasts.iterator();
                while (it.hasNext()) {
                    final ToastNotification toast = it.next();
                    toast.update(dt);
                    if (toast.isFinished()) {
                        it.remove();
                    }
                }
            }

            if (Settings.particlesEnabled) {
                final int w = (cachedW > 0) ? cachedW : 1920;
                final int h = (cachedH > 0) ? cachedH : 1080;
                Particle.updateAll(particles, particleCount, dt, w, h);
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
            AudioManager.stopMenuMusic();
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
        AchievementManager.removeListener(toastListener);

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

        if (gameEngine.getGameState() != GameState.FINISHED) {
            worldRenderer.drawPlanetAndGrid(g2d, w, h, horizonY, time, globalHue);
            if (Settings.particlesEnabled) {
                Particle.drawAll(g2d, particles, particleCount);
            }
            final WindowData windowData = WindowData.of(w, h);
            worldRenderer.drawGameObjects(g2d, windowData);
            uiRenderer.drawProgressBar(g2d, w, h);
            uiRenderer.drawScore(g2d, w, scorePopAlpha);
        }

        if (gameEngine != null && gameEngine.getNeonFlashAlpha() > 0) {
            final int flashAlpha = Math.min(255, (int) (gameEngine.getNeonFlashAlpha() * 255));
            g2d.setColor(RenderCache.blackWithAlpha(flashAlpha));
            g2d.fillRect(0, 0, w, h);
        }
        uiRenderer.renderGameState(g2d, w, h, gameEngine.getGameState());

        synchronized (activeToasts) {
            int index = 0;
            for (ToastNotification toast : activeToasts) toast.draw(g2d, w, index++);
        }

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

    /**
     * Pauses the game if it is currently playing.
     */
    public void pause() {
        if (gameEngine != null) {
            gameEngine.pause();
        }
    }
}
