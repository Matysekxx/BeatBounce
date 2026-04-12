package cz.matysekxx.beatbounce.gui;

public class Camera3D {
    private double cameraX;
    private double cameraY;
    private double cameraZ;

    private double cameraHeight;
    private double cameraWidth;
    private double fieldOfView;

    public Camera3D(
            double cameraX, double cameraY, double cameraZ,
            double fieldOfView, double cameraHeight, double cameraWidth
    ) {
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.cameraZ = cameraZ;
        this.fieldOfView = fieldOfView;
        this.cameraHeight = cameraHeight;
        this.cameraWidth = cameraWidth;
    }

    public double getCameraHeight() {
        return cameraHeight;
    }

    public double getCameraWidth() {
        return cameraWidth;
    }

    public double getCameraX() {
        return cameraX;
    }

    public double getCameraY() {
        return cameraY;
    }

    public double getCameraZ() {
        return cameraZ;
    }

    public double getFieldOfView() {
        return fieldOfView;
    }
}
