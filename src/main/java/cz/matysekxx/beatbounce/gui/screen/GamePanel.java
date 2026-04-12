package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.level.Level;
import cz.matysekxx.beatbounce.util.Utility;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {
    private Level level;
    private boolean running;
    private double cameraZ = 0;

    public GamePanel(Level level) {
        this.level = level;
        this.running = false;
        this.setBackground(Color.DARK_GRAY);
    }

    public void startGame() {
        if (!this.running) {
            this.running = true;
            Thread.ofVirtual().start(this);
        }
    }

    public void stopGame() {
        this.running = false;
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();
        while (running) {
            long now = System.currentTimeMillis();
            double deltaTime = (now - lastTime) / 1000.0;
            lastTime = now;
            cameraZ += deltaTime * 1000.0;
            repaint();
            Utility.sleep(16);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g;
        
        final int width = getWidth();
        final int height = getHeight();
        final int horizonY = height / 3;

        var xPoints = new int[]{100, width - 100, (width / 2) + 150, (width / 2) - 150};
        var yPoints = new int[]{height, height, horizonY, horizonY};
        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(xPoints, yPoints, 4);

        for (int z = 0; z < 2000; z += 200) {
            final double distance = z - (cameraZ % 200);
            if (distance <= 0) continue;
            final double scale = 250.0 / (distance + 1);
            final int screenY = horizonY + (int)(150 * scale);
            if (screenY > horizonY && screenY < height) {
                g2d.drawLine(0, screenY, width, screenY);
            }
        }

        g2d.setColor(Color.GREEN);

        final var tiles = level.getTiles();
        for (int i = tiles.size() - 1; i >= 0; i--) {
            final AbstractTile tile = tiles.get(i);
            final double distanceFront = tile.getZ() - cameraZ;
            if (distanceFront > 0 && distanceFront < 3000) {
                final double scaleFront = 250.0 / (distanceFront + 1);
                final int screenYFront = horizonY + (int)(150 * scaleFront);

                final int scaledWidth = (int)(80 * scaleFront);
                final int screenX = (width / 2) - (scaledWidth / 2);

                final int screenYBack;
                if (tile.getLengthInZ() > 0) {
                    final double scaleBack = 250.0 / (distanceFront + tile.getLengthInZ() + 1);
                    screenYBack = horizonY + (int)(150 * scaleBack);
                } else {
                    screenYBack = screenYFront - (int)(20 * scaleFront);
                }
                final int scaledHeight = screenYFront - screenYBack;
                tile.paint3D(g2d, screenX, screenYBack, scaledWidth, scaledHeight);
            }
        }
    }
}
