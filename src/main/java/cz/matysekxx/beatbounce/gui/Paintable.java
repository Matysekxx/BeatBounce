package cz.matysekxx.beatbounce.gui;

import java.awt.*;

public interface Paintable {
    void paint3D(Graphics2D g2d, Camera3D camera, WindowData windowData);

    void paint3D(Graphics2D g2d, Polygon polygon);
}
