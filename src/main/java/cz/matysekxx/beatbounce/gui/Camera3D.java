package cz.matysekxx.beatbounce.gui;

import cz.matysekxx.beatbounce.util.UIScale;

/**
 * Represents a 3D camera in the game world, used for projecting 3D coordinates to 2D screen space.
 */
public class Camera3D {
    /**
     * The field of view of the camera, affecting the perspective projection scale.
     */
    private final double fieldOfView;

    /**
     * The x-coordinate of the camera in the game world.
     */
    private double x;

    /**
     * The y-coordinate of the camera in the game world.
     */
    private double y;

    /**
     * The z-coordinate (depth) of the camera in the game world.
     */
    private double z;

    /**
     * Constructs a new {@code Camera3D} with the specified position and field of view.
     *
     * @param x           the x-coordinate of the camera
     * @param y           the y-coordinate of the camera
     * @param z           the z-coordinate (depth) of the camera
     * @param fieldOfView the field of view of the camera, affecting the perspective projection scale
     */
    public Camera3D(
            double x, double y, double z,
            double fieldOfView
    ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.fieldOfView = fieldOfView;
    }

    /**
     * Returns the x-coordinate of the camera.
     *
     * @return the x-coordinate of the camera
     */
    public double getX() {
        return x;
    }

    /**
     * Sets the x-coordinate of the camera.
     *
     * @param x the new x-coordinate
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Returns the y-coordinate of the camera.
     *
     * @return the y-coordinate of the camera
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the y-coordinate of the camera.
     *
     * @param y the new y-coordinate
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Returns the z-coordinate (depth) of the camera.
     *
     * @return the z-coordinate (depth) of the camera
     */
    public double getZ() {
        return z;
    }

    /**
     * Sets the z-coordinate (depth) of the camera.
     *
     * @param z the new z-coordinate
     */
    public void setZ(double z) {
        this.z = z;
    }

    /**
     * Returns the field of view of the camera.
     *
     * @return the field of view of the camera
     */
    public double getFieldOfView() {
        return fieldOfView;
    }

    /**
     * Calculates the distance from the camera to an object at the specified z-coordinate.
     *
     * @param objectZ the z-coordinate of the object
     * @return the distance to the object
     */
    public double getDistanceTo(double objectZ) {
        return objectZ - this.z;
    }

    /**
     * Calculates the scale factor for an object at the specified z-coordinate based on camera distance and field of view.
     * It also incorporates dynamic UI scaling to maintain consistent world proportions across resolutions.
     *
     * @param objectZ the z-coordinate of the object
     * @return the scale factor for projection
     */
    public double getScale(double objectZ) {
        final double distance = Math.max(0.0, this.getDistanceTo(objectZ));
        return (fieldOfView * UIScale.getScale()) / (distance + 1);
    }
}