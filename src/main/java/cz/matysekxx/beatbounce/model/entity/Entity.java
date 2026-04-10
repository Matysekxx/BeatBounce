package cz.matysekxx.beatbounce.model.entity;

import java.awt.*;

public abstract class Entity {
    private int x;
    private int y;

    public Entity(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void addToX(int dx) {
        this.x += dx;
    }

    public void addToY(int dy) {
        this.y += dy;
    }

    public Point getPoint() {
        return new Point(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
