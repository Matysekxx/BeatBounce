package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.event.BeatEvent;

import java.awt.*;

public class NormalTile extends AbstractTile {
    public NormalTile(BeatEvent beatEvent, Point point) {
        super(beatEvent, point);
    }

    @Override
    public void paint(Graphics2D g2d) {
        g2d.setColor(Color.GREEN);
        g2d.fillRect(x, y, 50, 20);
    }
}
