package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.controller.GameController;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.WindowData;
import cz.matysekxx.beatbounce.model.GameModel;
import cz.matysekxx.beatbounce.model.GameState;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.level.Level;
import cz.matysekxx.beatbounce.util.Time;

import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel implements Runnable {
    private final Camera3D cam;
    private final Thread gameThread;
    private final Sphere sphere;
    private Level level;
    private Clip clip;
    private short[] audioSamples;
    private float sampleRate;
    private GameModel gameModel;
    private boolean running;
    private int currentFps = 0;
    private int frameCount = 0;

    public GamePanel() {
        this.running = false;
        this.setLayout(new BorderLayout());
        this.setFocusable(true);
        this.setBackground(Color.BLACK);
        this.cam = new Camera3D(0, 0, -500, 500.0);
        this.sphere = new Sphere(0, 150, 0, 25);
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.setDoubleBuffered(true);
        this.setOpaque(true);
        gameThread = new Thread(this);

        final BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImg, new Point(0, 0), "blank cursor");
        this.setCursor(blankCursor);
        final GameController gameController = new GameController(cam, sphere);
        this.addKeyListener(gameController);
        this.addMouseMotionListener(gameController);
    }

    public void init(Level level) {
        this.level = level;
        this.clip = level.audioData().clip();
        this.audioSamples = level.audioData().samples();
        this.sampleRate = level.audioData().format().getSampleRate();
        this.gameModel = new GameModel(level, sphere, cam, clip);
    }

    public void startGame() {
        if (!this.running) {
            this.running = true;
            gameModel.init();
            this.gameThread.start();
        }
    }

    public void stopGame() {
        if (!this.running) return;
        this.running = false;
        gameModel.stop();
        gameThread.interrupt();
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Game thread interrupted while stopping: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        long lastFpsTime = System.currentTimeMillis();
        while (running) {
            final double currentTime = clip.getMicrosecondPosition() / 1_000_000.0;
            gameModel.update(currentTime);
            repaint();

            frameCount++;
            if (System.currentTimeMillis() - lastFpsTime >= 1000) {
                currentFps = frameCount;
                frameCount = 0;
                lastFpsTime = System.currentTimeMillis();
            }

            Time.sleep(16);
            if (gameModel.getGameState() == GameState.GAME_OVER) {
                Time.sleep(500);
                if (running) gameModel.init();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g;
        RenderUtils.initGraphic2D(g2d);


        final int width = getWidth();
        final int height = getHeight();
        final int horizonY = height / 3;

        drawEnvironment(g2d, width, height, horizonY);
        drawGameObjects(g2d, width, height);
        //drawHUD(g2d, width, height);
        drawScore(g2d, width);
        drawFPS(g2d);
    }

    private void drawFPS(Graphics2D g2d) {
        g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("FPS: " + currentFps, 10, 20);
    }

    private void drawEnvironment(Graphics2D g2d, int width, int height, int horizonY) {
        RenderUtils.drawBackground(g2d, width, height);
        RenderUtils.drawFloor(g2d, width, height, horizonY);
        drawHorizonEqualizer(g2d, width, horizonY);
        RenderUtils.drawHorizonLine(g2d, width, horizonY);
        drawNeonGrid(g2d, width, height, horizonY);
    }

    private void drawScore(Graphics2D g2d, int width) {
        final Integer score = gameModel.getScore();
        final String text = Integer.toString(score);
        g2d.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 60));

        final FontMetrics fm = g2d.getFontMetrics();
        final int x = (width - fm.stringWidth(text)) / 2;
        final int y = 70;
        final Color c = switch (score) {
            case Integer i when i < 50 -> RenderUtils.cyan;
            case Integer i when i < 75 -> RenderUtils.green;
            case Integer i when i < 100 -> RenderUtils.blue;
            case Integer i when i < 150 -> RenderUtils.purple;
            default -> RenderUtils.yellow;
        };
        RenderUtils.drawText(g2d, text, x, y, c);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(x - 20, y + 10, x + fm.stringWidth(text) + 20, y + 10);
    }

    private void drawGameObjects(Graphics2D g2d, int width, int height) {
        g2d.setColor(Color.GREEN);
        final var tiles = level.tiles();
        for (int i = tiles.size() - 1; i >= 0; i--) {
            final AbstractTile tile = tiles.get(i);
            final double distance = cam.getDistanceTo(tile.getZ());
            final double tileDepth = distance + tile.getLengthInZ();
            if (tileDepth <= 0 || distance > 3000) continue;
            tile.paint3D(g2d, cam, WindowData.of(width, height));
        }

        g2d.setColor(Color.MAGENTA);
        sphere.paint3D(g2d, cam, WindowData.of(width, height));

    }

    private void drawHUD(Graphics2D g2d, int width, int height) {
        if (gameModel.getGameState() == GameState.GAME_OVER)
            drawGameOver(g2d, width, height);
    }

    private void drawGameOver(Graphics2D g2d, int width, int height) {
        final String text = "GAME OVER";
        g2d.setFont(new Font("Monospaced", Font.BOLD | Font.ITALIC, 90));
        final FontMetrics fm = g2d.getFontMetrics();
        final int x = (width - fm.stringWidth(text)) >> 1;
        final int y = height >> 1;
        RenderUtils.drawText(g2d, text, x, y, new Color(250, 96, 241));
    }

    private void drawHorizonEqualizer(Graphics2D g2d, int width, int horizonY) {
        final int numBars = 64;
        final int barWidth = width / numBars;
        final long currentSample = (long) (clip.getMicrosecondPosition() / 1_000_000.0 * sampleRate);

        int windowSize = 512;
        final int startSample = Math.max(0, (int) currentSample);
        if (startSample + windowSize > audioSamples.length) windowSize = audioSamples.length - startSample;
        if (windowSize <= 0) return;

        final int samplesPerBar = Math.max(1, windowSize / numBars);

        for (int i = 0; i < numBars; i++) {
            final double normalized = calculateBarAmplitude(startSample, samplesPerBar, i);

            final double normalizedX = (double) (i - (numBars >> 1)) / (numBars >> 1);
            final double bellCurve = Math.exp(-Math.pow(normalizedX, 2) * 3);

            final int maxBarHeight = getHeight() / 4;
            final int barHeight = (int) (5 + (maxBarHeight * normalized * 2.5 * bellCurve));
            final int barX = i * barWidth;
            final int barY = horizonY - barHeight;

            final int r = (int) Math.min(255, Math.max(0, 255 * bellCurve));
            final int g = (int) Math.min(255, Math.max(0, 255 * (1 - bellCurve)));

            g2d.setColor(new Color(r, g, 255, 180));
            g2d.fillRect(barX + 2, barY, barWidth - 4, barHeight);

            g2d.setColor(new Color(255, 255, 255, 220));
            g2d.fillRect(barX + 2, barY, barWidth - 4, 3);

            g2d.setColor(new Color(r, g, 255, 30));
            g2d.fillRect(barX + 2, horizonY, barWidth - 4, barHeight / 2);
        }
    }

    private double calculateBarAmplitude(int startSample, int samplesPerBar, int barIndex) {
        long sum = 0;
        for (int j = 0; j < samplesPerBar; j++) {
            final int idx = startSample + barIndex * samplesPerBar + j;
            if (idx < audioSamples.length) {
                sum += Math.abs(audioSamples[idx]);
            }
        }
        return ((double) sum / samplesPerBar) / 32768.0;
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
            Point p = projectPoint(0, 150, cam.getZ() + distance, width, horizonY);

            if (p != null && p.y >= horizonY && p.y <= height) {
                final int alpha = (int) Math.max(0, Math.min(150, 255 - (distance / 3000.0 * 255)));
                g2d.setColor(new Color(0, 255, 255, alpha));
                g2d.drawLine(0, p.y, width, p.y);
            }
        }

        final int[] laneXs = {-300, -180, -60, 60, 180, 300};
        g2d.setStroke(new BasicStroke(2));
        for (int lx : laneXs) {
            Point start = projectPoint(lx, 150, cam.getZ() + 100, width, horizonY);
            Point end = projectPoint(lx, 150, cam.getZ() + 3000, width, horizonY);

            if (start == null || end == null) continue;

            g2d.setColor(Math.abs(lx) > 180 ? new Color(255, 0, 255, 100) : new Color(0, 255, 255, 120));
            g2d.drawLine(start.x, start.y, end.x, end.y);
        }
        g2d.setStroke(new BasicStroke(1));
    }
}