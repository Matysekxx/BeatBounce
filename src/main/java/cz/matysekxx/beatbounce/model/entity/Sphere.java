package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.Paintable;
import cz.matysekxx.beatbounce.gui.WindowData;

import java.awt.*;

public class Sphere extends Entity implements Paintable {
    private final int radius;
    private double z;
    private double targetX;
    private double currentX;
    private double currentY;
    private double jumpStartTime;
    private double jumpDuration;
    private double peakHeight;
    private boolean isJumping;
    private boolean isFalling = false;
    private double fallSpeed = 0;

    public Sphere(int x, int y, int z, int radius) {
        super(x, y);
        this.z = z;
        this.radius = radius;
        this.targetX = x;
        this.currentX = x;
        this.currentY = y;
        this.isJumping = false;
    }

    public void startJump(double startTime, double duration, double height) {
        this.jumpStartTime = startTime;
        this.jumpDuration = duration;
        this.peakHeight = height;
        this.isJumping = true;
        this.isFalling = false;
        this.fallSpeed = 0;
    }

    public void update(double currentTime) {
        currentX += (targetX - currentX) * 0.7;
        this.x = (int) currentX;

        if (isFalling) {
            fallSpeed += 0.5;
            currentY += fallSpeed;
            this.y = (int) currentY;
        } else if (isJumping) {
            final double elapsed = currentTime - jumpStartTime;
            double progress = elapsed / jumpDuration;

            if (progress >= 1.0) {
                isJumping = false;
                currentY = 150;
                this.y = 150;
            } else {
                final double jumpYOffset = 4 * peakHeight * progress * (1 - progress);
                currentY = 150 - jumpYOffset;
                this.y = (int) currentY;
            }
        } else {
            currentY = 150;
            this.y = 150;
        }
    }

    public double getTargetX() {
        return targetX;
    }

    public double getCurrentY() {
        return currentY;
    }

    public void startFalling() {
        isFalling = true;
        isJumping = false;
        fallSpeed = 0;
    }

    public void reset() {
        currentX = 0; targetX = 0;
        currentY = 150; this.y = 150;
        this.z = 0;
        isJumping = false; isFalling = false; fallSpeed = 0;
    }

    public void setTargetX(double targetX) {
        this.targetX = targetX;
    }

    public boolean isJumping() {
        return isJumping;
    }

    public double getJumpEndTime() {
        return jumpStartTime + jumpDuration;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public int getRadius() {
        return radius;
    }

    @Override
    public void paint3D(Graphics2D g2d, Camera3D cam, WindowData windowData) {
        final double scale = cam.getScale(z);
        if (scale <= 0) return;

        final int screenX = (int) (windowData.width() / 2. + (currentX - cam.getX()) * scale);
        final int screenY = (int) (windowData.height() / 3. + (currentY - cam.getY()) * scale);
        final int scaledRadius = (int) (radius * scale);

        g2d.setColor(Color.MAGENTA);
        g2d.fillOval(screenX - scaledRadius, screenY - scaledRadius, scaledRadius * 2, scaledRadius * 2);
    }

    @Override
    public void paint3D(Graphics2D g2d, Polygon polygon) {
    }
}
