package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.level.Level;
import cz.matysekxx.beatbounce.util.Utility;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {
    private Level level;
    private boolean running;
    private double cameraX = 0;

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
        while (running) {
            cameraX += 5;
            repaint();
            Utility.sleep(16);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g;
        
        g2d.translate(-cameraX, 0);
        for (AbstractTile tile : level.getTiles()) {
            tile.paint(g2d);
        }
        g2d.translate(cameraX, 0);
    }
}
