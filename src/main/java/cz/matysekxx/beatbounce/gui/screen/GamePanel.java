package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.WindowData;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.level.Level;
import cz.matysekxx.beatbounce.util.Utility;

import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel implements Runnable {
    private final Level level;
    private final Clip clip;
    
    private final Camera3D cam;
    private boolean running;

    public GamePanel(Level level, Clip clip) {
        this.level = level;
        this.clip = clip;
        this.running = false;
        this.setBackground(Color.DARK_GRAY);
        this.cam = new Camera3D(0, 0, 0, 500.0);
        this.setFocusable(true);
        this.requestFocusInWindow();

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) cam.addToX(-100);
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) cam.addToX(100);
                if (e.getKeyCode() == KeyEvent.VK_UP) cam.addToY(-100);
                if (e.getKeyCode() == KeyEvent.VK_DOWN) cam.addToY(100);
            }
        });
    }

    public void startGame() {
        if (!this.running) {
            this.running = true;
            clip.start();
            Thread.ofVirtual().start(this);
        }
    }

    public void stopGame() {
        this.running = false;
    }

    @Override
    public void run() {
        while (running) {
            final double currentAudioTimeSeconds = clip.getMicrosecondPosition() / 1_000_000.0;
            cam.setZ(currentAudioTimeSeconds * 1000.0);
            repaint();
            Utility.sleep(16);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);
        
        final int width = getWidth();
        final int height = getHeight();
        final int horizonY = height / 3;

        this.drawTrack(g2d, width, height, horizonY);

        for (int z = 0; z < 2000; z += 200) {
            final double distance = z - (cam.getZ() % 200);
            if (distance <= 0) continue;
            final double scale = cam.getScale(cam.getZ() + distance);
            final int screenY = (int) (horizonY + ((150 - cam.getY()) * scale));
            if (screenY >= horizonY && screenY <= height) {
                g2d.drawLine(0, screenY, width, screenY);
            }
        }

        g2d.setColor(Color.GREEN);
        final var tiles = level.getTiles();
        for (int i = tiles.size() - 1; i >= 0; i--) {
            final AbstractTile tile = tiles.get(i);
            final double distance = cam.getDistanceTo(tile.getZ());
            final double tileDepth = distance + tile.getLengthInZ();
            if (tileDepth <= 0 || distance > 3000) continue;

            tile.paint3D(g2d, cam, new WindowData(width, height));
        }
    }

    public void drawTrack(Graphics2D g2d, int width, int height, int horizonY) {
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
}
