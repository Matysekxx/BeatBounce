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
        final double fieldOfView = 500.0;

        var xPoints = new int[]{100, width - 100, (width / 2) + 150, (width / 2) - 150};
        var yPoints = new int[]{height, height, horizonY, horizonY};
        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(xPoints, yPoints, 4);

        for (int z = 0; z < 2000; z += 200) {
            final double distance = z - (cameraZ % 200);
            if (distance <= 0) continue;
            final double scale = fieldOfView / (distance + 1);
            final int screenY = horizonY + (int)(150 * scale);
            if (screenY >= horizonY && screenY <= height) {
                g2d.drawLine(0, screenY, width, screenY);
            }
        }

        g2d.setColor(Color.GREEN);

        final var tiles = level.getTiles();
        for (int i = tiles.size() - 1; i >= 0; i--) {
            final AbstractTile tile = tiles.get(i);
            final double distance = tile.getZ() - cameraZ;
            final double length = tile.getLengthInZ() > 0 ? tile.getLengthInZ() : 50;
            final double tileDepth = distance + length;

            if (tileDepth <= 0 || distance > 3000) continue;

            final double clippedDistance = Math.max(distance, 0.0);

            final double scaleFront = fieldOfView / (clippedDistance + 1);
            final double scaleBack = fieldOfView / (tileDepth + 1);

            final int screenYFront = horizonY + (int)(150 * scaleFront);
            final int screenYBack = horizonY + (int)(150 * scaleBack);

            final double centerScreenFront = calculateCenterScreen(tile, width, scaleFront);
            final double centerScreenBack = calculateCenterScreen(tile, width, scaleBack);

            final double frontWidth = 100*scaleFront;
            final double backWidth = 100*scaleBack;

            final int[] pointsX = {
                    (int) (centerScreenFront - frontWidth / 2),
                    (int) (centerScreenFront + frontWidth / 2),
                    (int) (centerScreenBack + backWidth / 2),
                    (int) (centerScreenBack - backWidth / 2)
            };
            final int[] pointsY = {
                    screenYFront, screenYFront, screenYBack, screenYBack
            };
            tile.paint3D(g2d, new Polygon(pointsX, pointsY, 4));
        }
    }

    private static double calculateCenterScreen(AbstractTile tile, int width ,double scale) {
        return ((double) width / 2) + (tile.getX() * scale);
    }
}
