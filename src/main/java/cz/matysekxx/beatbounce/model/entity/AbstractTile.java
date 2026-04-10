package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.gui.Paintable;

import java.awt.*;

public abstract class AbstractTile extends Entity implements Paintable {
    private final Rectangle rectangle;

    public AbstractTile(Rectangle rectangle) {
        super(rectangle.x,  rectangle.y);
        this.rectangle = rectangle;
    }

    public Rectangle rectangle() {
        return rectangle;
    }
}
