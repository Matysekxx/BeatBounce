package cz.matysekxx.beatbounce.gui;

public class Camera3D {
    private final double fieldOfView;
    private double x;
    private double y;
    private double z;

    public Camera3D(
            double x, double y, double z,
            double fieldOfView
    ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.fieldOfView = fieldOfView;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getFieldOfView() {
        return fieldOfView;
    }

    public double getDistanceTo(double objectZ) {
        return objectZ - this.z;
    }

    public double getScale(double objectZ) {
        final double distance = Math.max(0.0, this.getDistanceTo(objectZ));
        return fieldOfView / (distance + 1);
    }
}