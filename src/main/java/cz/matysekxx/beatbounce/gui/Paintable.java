package cz.matysekxx.beatbounce.gui;

import java.awt.*;

public interface Paintable {
    void paint(Graphics2D g2d);

    default void paint(Graphics g) {
        paint((Graphics2D) g);
    }
}
