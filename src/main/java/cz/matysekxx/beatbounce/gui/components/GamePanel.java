package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.controller.GameController;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.WindowData;
import cz.matysekxx.beatbounce.model.GameModel;
import cz.matysekxx.beatbounce.model.GameState;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.Orb;
import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.level.Level;
import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.util.Time;

import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;

public class GamePanel extends JPanel implements Runnable {
    private final Camera3D cam;
    private final Runnable onExit;
    private final Cursor blankCursor;
    private Thread gameThread;
    private Sphere sphere;
    private Level level;
    private Clip clip;
    private GameModel gameModel;
    private boolean running;
    private long lastFrameTime;
    private float flashAlpha = 0f;
    private boolean isCursorHidden = false;
    private int lastScore = 0;
    private float scorePopAlpha = 0f;
    private GameUIRenderer uiRenderer;
    private BufferedImage bgCache;
    private int cachedW = -1;
    private int cachedH = -1;
    private int frames = 0;
    private long lastFpsTime = 0;
    private int currentFps = 0;

    public GamePanel(Runnable onExit) {
        this.onExit = onExit;
        this.running = false;
        this.setLayout(new BorderLayout());
        this.setFocusable(true);
        this.setBackground(Color.BLACK);
        this.cam = new Camera3D(0, 0, -500, 500.0);
        this.setDoubleBuffered(true);
        this.setOpaque(true);
        this.blankCursor = RenderUtils.blankCursor;
    }

    public void init(Level level) {
        this.level = level;
        this.clip = level.audioData().clip();
        this.sphere = new Sphere(0, 150, 0, 25);
        this.gameModel = new GameModel(level, sphere, cam, clip);
        this.flashAlpha = 0f;
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
                            flashAlpha = 0f;
                        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                            stopGame();
                            if (onExit != null) onExit.run();
                        }
                    }
                }
            }
        });
    }

    public void startGame() {
        if (!this.running) {
            this.running = true;
            gameModel.init();
            this.lastFrameTime = System.nanoTime();
            this.gameThread = new Thread(this);
            this.gameThread.start();
            this.requestFocusInWindow();
        }
    }

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

    @Override
    public void run() {
        while (running) {
            final long now = System.nanoTime();
            final double deltaTime = (now - lastFrameTime) / 1_000_000_000.0;
            lastFrameTime = now;

            final double currentTime = (clip != null && clip.isRunning()) ? clip.getMicrosecondPosition() / 1_000_000.0 : 0;
            final GameState oldState = gameModel.getGameState();
            gameModel.update(currentTime, deltaTime);
            updateCursorVisibility();

            if (oldState == GameState.PLAYING && gameModel.getGameState() == GameState.FALLING) {
                flashAlpha = 0.5f;
            }

            if (flashAlpha > 0) {
                flashAlpha -= (float) (deltaTime * 2.0);
                if (flashAlpha < 0) flashAlpha = 0;
            }

            final int currentScore = gameModel.getScore();
            if (currentScore != lastScore) {
                scorePopAlpha = 1.0f;
                lastScore = currentScore;
            }
            if (scorePopAlpha > 0) {
                scorePopAlpha -= (float) (deltaTime * 3.0);
                if (scorePopAlpha < 0) scorePopAlpha = 0;
            }

            repaint();
            long frameTimeMs = (long) (1000.0 / Settings.targetFps);
            Time.sleep(frameTimeMs);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        RenderUtils.initGraphics2D(g2d);
        final int w = getWidth();
        final int h = getHeight();
        final int horizonY = h / 3;
        final long time = System.currentTimeMillis();

        if (gameModel.getGameState() != GameState.FINISHED) {
            drawEnvironment(g2d, w, h, horizonY, time);
            drawGameObjects(g2d, w, h);
            uiRenderer.drawProgressBar(g2d, w, h);
            uiRenderer.drawScore(g2d, w, scorePopAlpha);
        }
        if (flashAlpha > 0) {
            final RadialGradientPaint flashVignette = new RadialGradientPaint(
                    w / 2f, h / 2f, w * 0.7f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(1f, 0f, 0f, flashAlpha * 0.3f),
                            new Color(1f, 0f, 0f, flashAlpha)}
            );
            g2d.setPaint(flashVignette);
            g2d.fillRect(0, 0, w, h);
        }

        if (gameModel != null && gameModel.getNeonFlashAlpha() > 0) {
            float alpha = Math.min(1f, gameModel.getNeonFlashAlpha());
            g2d.setColor(new Color(0f, 0f, 0f, alpha));
            g2d.fillRect(0, 0, w, h);
        }
        drawByGameState(g2d, w, h);
        
        if (Settings.showFps) {
            frames++;
            final long nowTime = System.currentTimeMillis();
            if (nowTime - lastFpsTime >= 1000) {
                currentFps = frames;
                frames = 0;
                lastFpsTime = nowTime;
            }
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
            g2d.drawString("FPS: " + currentFps, 10, 20);
        }

        g2d.dispose();
    }

    private void drawByGameState(Graphics2D g2d, int w, int h) {
        switch (gameModel.getGameState()) {
            case COUNTDOWN -> uiRenderer.drawCountdown(g2d, w, h);
            case PAUSED -> uiRenderer.drawPauseScreen(g2d, w, h);
            case FINISHED -> uiRenderer.drawFinishedScreen(g2d, w, h);
            case GAME_OVER -> uiRenderer.drawGameOverScreen(g2d, w, h);
        }
    }

    private void drawEnvironment(Graphics2D g2d, int width, int height, int horizonY, long time) {
        if (bgCache == null || cachedW != width || cachedH != height) {
            this.cachedW = width;
            this.cachedH = height;
            this.bgCache = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
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

        g2d.setColor(new Color(0, 255, 255, 120));
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawOval(cx - r, cy - r, r * 2, r * 2);

        drawRing(g2d, cx, ry, r * 1.4f, 18, 180, 180, new Color(0, 255, 255, (int) (180 + 75 * pulse)), 2f);
        drawRing(g2d, cx, ry, r * 1.8f, 28, 180, 180, new Color(255, 50, 255, (int) (140 + 60 * pulse)), 2.5f);
        drawRing(g2d, cx, ry, r * 1.8f, 28, 180, 180, new Color(255, 200, 255, 200), 1f);

        g2d.setStroke(new BasicStroke(1f));
    }

    private void drawRing(
            Graphics2D g2d, int cx, int cy, float rx, int ry, int startAngle, int arcAngle, Color color, float stroke
    ) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(stroke));
        g2d.drawArc(cx - (int) rx, cy - ry, (int) (rx * 2), ry * 2, startAngle, arcAngle);
    }

    private void drawGameObjects(Graphics2D g2d, int width, int height) {
        if (gameModel == null || gameModel.getGameState() != GameState.FINISHED) {
            final List<AbstractTile> tiles = level.tiles();
            for (int i = tiles.size() - 1; i >= 0; i--) {
                final AbstractTile tile = tiles.get(i);
                final double distance = cam.getDistanceTo(tile.getZ());
                final double tileDepth = distance + tile.getLengthInZ();
                if (tileDepth <= 0 || distance > 3000) continue;
                tile.paint3D(g2d, cam, WindowData.of(width, height));
            }

            if (gameModel != null) {
                for (Orb orb : gameModel.getOrbs()) {
                    final double distance = cam.getDistanceTo(orb.getZ());
                    if (distance > 0 && distance < 3000) {
                        orb.paint3D(g2d, cam, WindowData.of(width, height));
                    }
                }
            }
        }
        sphere.paint3D(g2d, cam, WindowData.of(width, height));
    }

    private Point projectPoint(double x, double y, double z, int width, int horizonY) {
        final double scale = cam.getScale(z);
        if (scale <= 0) return null;
        final int px = (int) (width / 2.0 + (x - cam.getX()) * scale);
        final int py = (int) (horizonY + ((y - cam.getY()) * scale));
        return new Point(px, py);
    }

    private void drawNeonGrid(Graphics2D g2d, int width, int height, int horizonY) {
        for (int z = 0; z < 3000; z += 150) {
            final double distance = z - (cam.getZ() % 150);
            if (distance <= 0) continue;
            final Point p = projectPoint(0, 150, cam.getZ() + distance, width, horizonY);

            if (p != null && p.y >= horizonY && p.y <= height) {
                final int alpha = (int) Math.max(0, Math.min(120, 255 - (distance / 3000.0 * 255)));
                g2d.setColor(new Color(0, 255, 255, alpha));
                g2d.drawLine(0, p.y, width, p.y);
            }
        }

        final int[] laneXs = {-300, -180, -60, 60, 180, 300};
        g2d.setStroke(new BasicStroke(2));
        for (int lx : laneXs) {

            final Point start = projectPoint(lx, 150, cam.getZ() + 100, width, horizonY);
            final Point end = projectPoint(lx, 150, cam.getZ() + 3000, width, horizonY);

            if (start == null || end == null) continue;

            if (Math.abs(lx) > 180) g2d.setColor(new Color(255, 0, 255, 90));
            else g2d.setColor(new Color(0, 255, 255, 110));
            g2d.drawLine(start.x, start.y, end.x, end.y);
        }
        g2d.setStroke(new BasicStroke(1f));
    }
}