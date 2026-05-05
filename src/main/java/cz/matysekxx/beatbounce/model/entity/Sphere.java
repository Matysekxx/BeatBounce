package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.Paintable;
import cz.matysekxx.beatbounce.gui.WindowData;

import java.awt.*;

/**
 * The {@code Sphere} class represents the player character in the game.
 * It extends {@link Entity} and implements {@link Paintable}.
 * The sphere can jump, fall, and has various visual properties like alpha, vibration, and stretch.
 */
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

    /**
     * Constructs a new {@code Sphere} with specified coordinates and radius.
     *
     * @param x      the horizontal position
     * @param y      the vertical position
     * @param z      the depth position
     * @param radius the radius of the sphere
     */
    public Sphere(int x, int y, int z, int radius) {
        super(x, y);
        this.z = z;
        this.radius = radius;
        this.targetX = x;
        this.currentX = x;
        this.currentY = y;
        this.isJumping = false;
    }

    /**
     * Starts a jump animation for the sphere.
     *
     * @param startTime the time when the jump starts
     * @param duration  the duration of the jump
     * @param height    the peak height of the jump
     */
    public void startJump(double startTime, double duration, double height) {
        this.jumpStartTime = startTime;
        this.jumpDuration = duration;
        this.peakHeight = height;
        this.isJumping = true;
        this.isFalling = false;
        this.fallSpeed = 0;
    }

    /**
     * Updates the sphere's position and state based on the current time.
     *
     * @param currentTime the current game time
     */
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

    /**
     * Returns the target horizontal position.
     *
     * @return the {@code targetX} value
     */
    public double getTargetX() {
        return targetX;
    }

    /**
     * Sets the target horizontal position for the sphere to move towards.
     *
     * @param targetX the new target X coordinate
     */
    public void setTargetX(double targetX) {
        this.targetX = targetX;
    }

    /**
     * Returns the current vertical position.
     *
     * @return the {@code currentY} value
     */
    public double getCurrentY() {
        return currentY;
    }

    /**
     * Sets the current vertical position of the sphere.
     *
     * @param y the new Y coordinate
     */
    public void setCurrentY(double y) {
        this.currentY = y;
        this.y = (int) y;
    }

    /**
     * Starts the falling state for the sphere.
     */
    public void startFalling() {
        isFalling = true;
        isJumping = false;
        fallSpeed = 0;
    }

    /**
     * Resets the sphere to its initial state.
     */
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

    /**
     * Returns whether the sphere is currently jumping.
     *
     * @return {@code true} if jumping, {@code false} otherwise
     */
    public boolean isJumping() {
        return isJumping;
    }

    /**
     * Returns the time when the current jump is expected to end.
     *
     * @return the jump end time
     */
    public double getJumpEndTime() {
        return jumpStartTime + jumpDuration;
    }

    /**
     * Returns the depth position of the sphere.
     *
     * @return the {@code z} coordinate
     */
    public double getZ() {
        return z;
    }

    /**
     * Sets the depth position of the sphere.
     *
     * @param z the new Z coordinate
     */
    public void setZ(double z) {
        this.z = z;
    }

    /**
     * Returns the transparency alpha value of the sphere.
     *
     * @return the {@code alpha} value
     */
    public float getAlpha() {
        return alpha;
    }

    /**
     * Sets the transparency alpha value of the sphere.
     *
     * @param alpha the new alpha value (0.0 to 1.0)
     */
    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    /**
     * Returns the scale multiplier of the sphere.
     *
     * @return the {@code scaleMultiplier} value
     */
    public float getScaleMultiplier() {
        return scaleMultiplier;
    }

    /**
     * Sets the scale multiplier for rendering the sphere.
     *
     * @param scaleMultiplier the new scale multiplier
     */
    public void setScaleMultiplier(float scaleMultiplier) {
        this.scaleMultiplier = scaleMultiplier;
    }

    /**
     * Returns the vibration intensity of the sphere.
     *
     * @return the {@code vibration} value
     */
    public float getVibration() {
        return vibration;
    }

    /**
     * Sets the vibration intensity for the sphere's rendering.
     *
     * @param vibration the new vibration intensity
     */
    public void setVibration(float vibration) {
        this.vibration = vibration;
    }

    /**
     * Returns the vertical stretch factor of the sphere.
     *
     * @return the {@code stretch} value
     */
    public float getStretch() {
        return stretch;
    }

    /**
     * Sets the vertical stretch factor for the sphere's rendering.
     *
     * @param stretch the new stretch factor
     */
    public void setStretch(float stretch) {
        this.stretch = stretch;
    }

    /**
     * Returns the base radius of the sphere.
     *
     * @return the {@code radius} value
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Renders the sphere in 3D space.
     *
     * @param g2d        the graphics context to paint on
     * @param cam        the {@link Camera3D} used for perspective calculations
     * @param windowData the {@link WindowData} containing screen dimensions
     */
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

    /**
     * Implementation of {@link Paintable#paint3D(Graphics2D, Polygon)}.
     * Currently does nothing for {@code Sphere}.
     *
     * @param g2d     the graphics context
     * @param polygon the polygon to paint
     */
    @Override
    public void paint3D(Graphics2D g2d, Polygon polygon) {
    }
}
