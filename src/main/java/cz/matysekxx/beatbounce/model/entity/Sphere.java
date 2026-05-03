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
    private float alpha = 1.0f;
    private float scaleMultiplier = 1.0f;
    private float vibration = 0.0f;
    private float stretch = 1.0f;

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

    public void setTargetX(double targetX) {
        this.targetX = targetX;
    }

    public double getCurrentY() {
        return currentY;
    }

    public void setCurrentY(double y) {
        this.currentY = y;
        this.y = (int) y;
    }

    public void startFalling() {
        isFalling = true;
        isJumping = false;
        fallSpeed = 0;
    }

    public void reset() {
        currentX = 0;
        targetX = 0;
        currentY = 150;
        this.y = 150;
        this.z = 0;
        isJumping = false;
        isFalling = false;
        fallSpeed = 0;
        alpha = 1.0f;
        scaleMultiplier = 1.0f;
        vibration = 0.0f;
        stretch = 1.0f;
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

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getScaleMultiplier() {
        return scaleMultiplier;
    }

    public void setScaleMultiplier(float scaleMultiplier) {
        this.scaleMultiplier = scaleMultiplier;
    }

    public float getVibration() {
        return vibration;
    }

    public void setVibration(float vibration) {
        this.vibration = vibration;
    }

    public float getStretch() {
        return stretch;
    }

    public void setStretch(float stretch) {
        this.stretch = stretch;
    }

    public int getRadius() {
        return radius;
    }

    @Override
    public void paint3D(Graphics2D g2d, Camera3D cam, WindowData windowData) {
        final double scale = cam.getScale(z);
        if (scale <= 0) return;

        final double vx;
        final double vy;
        if (vibration > 0) {
            vx = (Math.random() - 0.5) * vibration * 15;
            vy = (Math.random() - 0.5) * vibration * 15;
        } else {
            vx = 0;
            vy = 0;
        }

        final int screenX = (int) (windowData.width() / 2. + (currentX + vx - cam.getX()) * scale);
        final int screenY = (int) (windowData.height() / 3. + (currentY + vy - radius - cam.getY()) * scale);
        final int scaledRadiusX = (int) (radius * scale * scaleMultiplier);
        final int scaledRadiusY = (int) (radius * scale * scaleMultiplier * stretch);

        final int a = (int) (255 * Math.max(0, Math.min(1.0f, alpha)));
        if (a <= 0 || scaledRadiusX <= 0 || scaledRadiusY <= 0) return;

        g2d.setColor(new Color(255, 0, 255, a));
        g2d.fillOval(screenX - scaledRadiusX, screenY - scaledRadiusY, scaledRadiusX * 2, scaledRadiusY * 2);

        if (stretch > 1.2) {
            g2d.setColor(new Color(255, 200, 255, (int) (a * 0.6)));
            final int innerW = (int) (scaledRadiusX * 0.6);
            final int innerH = (int) (scaledRadiusY * 0.9);
            g2d.fillOval(screenX - innerW, screenY - innerH, innerW * 2, innerH * 2);
        }
    }

    @Override
    public void paint3D(Graphics2D g2d, Polygon polygon) {
    }
}