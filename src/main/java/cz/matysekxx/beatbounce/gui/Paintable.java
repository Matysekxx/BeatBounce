package cz.matysekxx.beatbounce.gui;

import java.awt.*;

/**
 * Interface for objects that can be painted in a 3D context.
 */
public interface Paintable {
    /**
     * Paints the object in 3D using the provided graphics context, camera, and window data.
     *
     * @param g2d the graphics context to paint with
     * @param camera the {@link Camera3D} used for projection
     * @param windowData the {@link WindowData} containing screen dimensions
     */
    void paint3D(Graphics2D g2d, Camera3D camera, WindowData windowData);

    /**
     * Paints a 3D polygon using the provided graphics context.
     *
     * @param g2d the graphics context to paint with
     * @param polygon the polygon to paint
     */
    void paint3D(Graphics2D g2d, Polygon polygon);
}