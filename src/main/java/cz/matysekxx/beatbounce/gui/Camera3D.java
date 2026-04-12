package cz.matysekxx.beatbounce.gui;

public class Camera3D {
    private double x;
    private double y;
    private double z;

    private final double fieldOfView;

    public Camera3D(
            double x, double y, double z,
            double fieldOfView
    ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.fieldOfView = fieldOfView;
    }

    public void addToZ(double deltaZ) {
        this.z += deltaZ;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void addToX(double deltaX) {
        this.x += deltaX;
    }

    public  void addToY(double deltaY) {
        this.y += deltaY;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
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
