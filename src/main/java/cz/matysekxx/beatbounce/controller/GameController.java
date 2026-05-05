package cz.matysekxx.beatbounce.controller;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.model.entity.Sphere;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * Handles user input for controlling the game, specifically managing the movement
 * of the player's sphere based on mouse input.
 * <p>
 * This controller listens for mouse motion events and updates the sphere's
 * target position based on the mouse's horizontal coordinates and the 3D camera projection.
 * </p>
 */
public class GameController implements MouseMotionListener {
    /**
     * The width of a single lane in the game world.
     */
    private final static int LANE_WIDTH = 120;

    /**
     * The maximum absolute X-coordinate the sphere can move to.
     */
    private final static int MAX_LANE_ABS = 2 * LANE_WIDTH;

    /**
     * The 3D camera used to project screen coordinates to world coordinates.
     */
    private final Camera3D cam;

    /**
     * The player's sphere controlled by this class.
     */
    private final Sphere sphere;

    /**
     * Initializes a new GameController with the specified camera and sphere.
     *
     * @param cam    The {@link Camera3D} used for coordinate projections.
     * @param sphere The {@link Sphere} representing the player.
     */
    public GameController(Camera3D cam, Sphere sphere) {
        this.cam = cam;
        this.sphere = sphere;
    }

    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     * <p>
     * Delegates to {@link #mouseMoved(MouseEvent)}.
     * </p>
     *
     * @param e The mouse event.
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        this.mouseMoved(e);
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component but no buttons have been pushed.
     * <p>
     * Updates the player's sphere target X-coordinate based on the mouse position.
     * </p>
     *
     * @param e The mouse event.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        final int mouseX = e.getX();
        final int width = e.getComponent().getWidth();
        final double scale = cam.getScale(sphere.getZ());
        if (scale <= 0) return;
        final double rawTargetX = cam.getX() + (mouseX - width / 2.0) / scale;
        sphere.setTargetX(Math.max(-MAX_LANE_ABS, Math.min(MAX_LANE_ABS, rawTargetX)));
    }
}
