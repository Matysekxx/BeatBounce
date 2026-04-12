package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.event.BeatEvent;

import java.awt.*;

public class NormalTile extends AbstractTile {
    public NormalTile(BeatEvent beatEvent, Point point, double z) {
        super(beatEvent, point, z);
    }

    @Override
    public void paint(Graphics2D g2d) {
    }

    @Override
    public void paint3D(Graphics2D g2d, int screenX, int screenY, int scaledWidth, int scaledHeight) {
        g2d.setColor(Color.GREEN);
        g2d.fillRect(screenX, screenY, scaledWidth, scaledHeight);
    }
}
