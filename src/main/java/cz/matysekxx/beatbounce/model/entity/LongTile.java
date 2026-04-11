package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.event.BeatEvent;

import java.awt.*;

public class LongTile extends AbstractTile {
    
    private final int width;
    
    public LongTile(BeatEvent beatEvent, Point point, int width) {
        super(beatEvent, point);
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public void paint(Graphics2D g2d) {
        g2d.setColor(Color.RED);
        g2d.fillRect(x, y, width, 20);
    }
}
