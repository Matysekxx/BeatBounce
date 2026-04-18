package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.WindowData;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.level.Level;
import cz.matysekxx.beatbounce.util.Time;

import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements Runnable {
    private final Level level;
    private final Clip clip;
    private final Camera3D cam;
    private final short[] audioSamples;
    private final float sampleRate;
    private boolean running;
    private final Thread gameThread;
    private final Sphere sphere;
    private int currentTileIndex = -1;
    private boolean lastInputWasMouse = false;

    private static final int LANE_WIDTH = 120;

    public GamePanel(Level level, Clip clip, short[] audioSamples, float sampleRate) {
        this.level = level;
        this.clip = clip;
        this.audioSamples = audioSamples;
        this.sampleRate = sampleRate;
        this.running = false;
        this.setBackground(Color.DARK_GRAY);
        this.cam = new Camera3D(0, 0, -500, 500.0);
        this.sphere = new Sphere(0, 150, 0, 20);
        this.setFocusable(true);
        this.requestFocusInWindow();
        gameThread = new Thread(this);

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (lastInputWasMouse) {
                    sphere.setTargetX(snapToNearestLane(sphere.getTargetX()));
                    lastInputWasMouse = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_LEFT)
                    sphere.setTargetX(sphere.getTargetX() - LANE_WIDTH);
                else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
                    sphere.setTargetX(sphere.getTargetX() + LANE_WIDTH);
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                lastInputWasMouse = true;
                final int mouseX = e.getX();
                final int width = getWidth();
                final double scale = cam.getScale(sphere.getZ());
                if (scale <= 0) return;
                final double newTargetX = cam.getX() + (mouseX - width / 2.0) / scale;
                sphere.setTargetX(newTargetX);
            }
        });
    }

    public void startGame() {
        if (!this.running) {
            this.running = true;
            clip.start();
            gameThread.start();
        }
    }

    public void stopGame() {
        this.running = false;
        clip.stop();
        gameThread.interrupt();
    }

    @Override
    public void run() {
        while (running) {
            final double currentTime = clip.getMicrosecondPosition() / 1_000_000.0;
            double targetZ = currentTime * 1000.0;
            
            sphere.setZ(targetZ);
            cam.setZ(targetZ - 500);

            cam.addToX((sphere.getX() - cam.getX()) * 0.2);

            updateSphere(currentTime);

            repaint();
            Time.sleep(16);
        }
    }

    private void updateSphere(double currentTime) {
        if (!sphere.isJumping())
            startNextJump();
        else if (currentTime >= sphere.getJumpEndTime())
            startNextJump();
        
        sphere.update(currentTime);
    }

    //TODO: presunout metody na update kulicky do samostatne tridy Model

    private void startNextJump() {
        final var tiles = level.getTiles();
        
        final int nextIndex = currentTileIndex + 1;
        if (nextIndex >= tiles.size()) return;

        final AbstractTile currentTile = nextIndex > 0 ? tiles.get(nextIndex - 1) : null;
        final AbstractTile nextTile = tiles.get(nextIndex);

        final double startZ = (currentTile != null) ? currentTile.getZ() : 0;
        final double endZ = nextTile.getZ();
        final double distanceZ = endZ - startZ;
        
        double duration = distanceZ / 1000.0;
        if (duration <= 0) duration = 0.2;

        final double height = 50 + (distanceZ * 0.15);
        final double startTime = startZ / 1000.0;
        
        sphere.startJump(startTime, duration, height);
        currentTileIndex = nextIndex;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);

        final int width = getWidth();
        final int height = getHeight();
        final int horizonY = height / 3;

        this.drawTrack(g2d, width, height, horizonY);
        this.drawLines(g2d, horizonY, width, height);

        g2d.setColor(Color.GREEN);
        final var tiles = level.getTiles();
        for (int i = tiles.size() - 1; i >= 0; i--) {
            final AbstractTile tile = tiles.get(i);
            final double distance = cam.getDistanceTo(tile.getZ());
            final double tileDepth = distance + tile.getLengthInZ();
            if (tileDepth <= 0 || distance > 3000) continue;
            tile.paint3D(g2d, cam, WindowData.of(width, height));
        }

        g2d.setColor(Color.MAGENTA);
        sphere.paint3D(g2d, cam, WindowData.of(width, height));

        g2d.setColor(Color.CYAN);
        drawWaveform(g2d, width);
    }

    private void drawLines(Graphics2D g2d, int horizonY, int w, int h) {
        for (int z = 0; z < 2000; z += 200) {
            final double distance = z - (cam.getZ() % 200);
            if (distance <= 0) continue;
            final double scale = cam.getScale(cam.getZ() + distance);
            final int screenY = (int) (horizonY + ((150 - cam.getY()) * scale));
            if (screenY >= horizonY && screenY <= h) {
                g2d.drawLine(0, screenY, w, screenY);
            }
        }
    }

    private void drawTrack(Graphics2D g2d, int width, int height, int horizonY) {
        final var xPoints = new int[]{
                (int) (100 - cam.getX()),
                (int) (width - 100 - cam.getX()),
                (int) (((double) width / 2) + 150 - (cam.getX() / 4)),
                (int) (((double) width / 2) - 150 - (cam.getX() / 4))
        };
        final var yPoints = new int[]{
                (int) (height - cam.getY()),
                (int) (height - cam.getY()),
                (int) (horizonY - (cam.getY() / 4)),
                (int) (horizonY - (cam.getY() / 4))
        };
        g2d.fillPolygon(xPoints, yPoints, 4);
    }


    //TODO: pouzit SwingWorker, ThreadPool, Future nebo Completable Future pro predpripreveni Waveform
    private void drawWaveform(Graphics2D g2d, int width) {
        final int waveformHeight = 100;

        final long currentMicroseconds = clip.getMicrosecondPosition();
        final long currentSamples = (long) (currentMicroseconds / 1_000_000.0 * sampleRate);

        final int samplesToDisplay = (int)sampleRate << 1;

        final int startIndex = Math.max(0, (int) (currentSamples - (samplesToDisplay >> 1)));
        final int endIndex = Math.min(startIndex + samplesToDisplay, audioSamples.length);

        if (startIndex >= endIndex) return;

        final double samplesPerPixel = (double) (endIndex - startIndex) / width;

        for (int x = 0; x < width; x++) {
            final int sampleStart = (int) (startIndex + x * samplesPerPixel);
            int sampleEnd = (int) (startIndex + (x + 1) * samplesPerPixel);

            if (sampleEnd > endIndex) sampleEnd = endIndex;

            if (sampleStart >= sampleEnd) continue;

            short minSample = Short.MAX_VALUE;
            short maxSample = Short.MIN_VALUE;

            for (int i = sampleStart; i < sampleEnd; i++) {
                final short sample = audioSamples[i];
                if (sample < minSample) minSample = sample;
                if (sample > maxSample) maxSample = sample;
            }

            final int yMax = (waveformHeight / 2) - (maxSample * (waveformHeight / 2) / 32768);
            final int yMin = (waveformHeight / 2) - (minSample * (waveformHeight / 2) / 32768);

            g2d.drawLine(x, yMin, x, yMax);
        }

        g2d.setColor(Color.RED);
        final int currentX = (int) ((currentSamples - startIndex) / samplesPerPixel);
        if (currentX >= 0 && currentX < width) {
            g2d.drawLine(currentX, waveformHeight, currentX, 0);
        }
    }

    private double snapToNearestLane(double x) {
        return Math.round(x / LANE_WIDTH) * LANE_WIDTH;
    }
}