package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.WindowData;

import java.awt.*;

/**
 * The {@code Sphere} class represents the player character in the game.
 * It extends {@link Entity}.
 * The sphere can jump, fall, and has various visual properties like alpha, vibration, and stretch.
 */
public class Sphere extends Entity {
    /**
     * Physical radius of the sphere in world units.
     */
    private final int radius;

    /**
     * Current depth position.
     */
    private double z;

    /**
     * The horizontal world coordinate the sphere is moving towards.
     */
    private double targetX;

    /**
     * Exact current horizontal world coordinate.
     */
    private double currentX;

    /**
     * Exact current vertical world coordinate.
     */
    private double currentY;

    /**
     * World time when the current jump started.
     */
    private double jumpStartTime;

    /**
     * Total expected duration of the current jump.
     */
    private double jumpDuration;

    /**
     * The maximum height reached during the current jump.
     */
    private double peakHeight;

    /**
     * Whether the sphere is currently in the air performing a jump.
     */
    private boolean isJumping;

    /**
     * Whether the sphere has missed a tile and is falling into the void.
     */
    private boolean isFalling = false;

    public boolean isFalling() {
        return isFalling;
    }

    /**
     * Current transparency alpha level (0.0 to 1.0).
     */
    private float alpha = 1.0f;

    /**
     * Current visual scale multiplier for animations.
     */
    private float scaleMultiplier = 1.0f;

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
    }

    /**
     * Cancels the current jump and snaps the sphere back to the ground.
     */
    public void cancelJump() {
        this.isJumping = false;
        this.currentY = 150;
        this.y = 150;
    }

    /**
     * Updates the sphere's position and state based on the current time and delta time.
     *
     * @param currentTime the current game time in seconds
     * @param deltaTime   the time elapsed since the last update in seconds
     */
    public void update(double currentTime, double deltaTime) {
        final double lerpFactor = 1.0 - Math.exp(-25 * deltaTime);
        currentX += (targetX - currentX) * lerpFactor;
        this.x = (int) currentX;
        if (isFalling) {
            final double constantFallVelocity = 600.0;
            currentY += constantFallVelocity * deltaTime;
            this.y = (int) currentY;
        } else if (isJumping) {
            final double elapsed = currentTime - jumpStartTime;
            final double progress = elapsed / jumpDuration;
            if (progress >= 1.0) {
                isJumping = false;
                currentY = 150;
            } else {
                final double jumpYOffset = 4 * peakHeight * progress * (1 - progress);
                currentY = 150 - jumpYOffset;
            }
            this.y = (int) currentY;
        } else {
            currentY = 150;
            this.y = (int) currentY;
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
     * Sets the current horizontal position of the sphere.
     *
     * @param x the new X coordinate
     */
    public void setCurrentX(double x) {
        this.currentX = x;
        this.targetX = x;
        this.x = (int) x;
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
        alpha = 1.0f;
        scaleMultiplier = 1.0f;
    }

    /**
     * Prepares the sphere for continuation after a revive.
     */
    public void revive() {
        this.isFalling = false;
        this.isJumping = false;
        this.currentX = 0;
        this.targetX = 0;
        this.currentY = 150;
        this.y = 150;
        this.alpha = 1.0f;
        this.scaleMultiplier = 1.0f;
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
     * Returns the base radius of the sphere.
     *
     * @return the {@code radius} value
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Renders the sphere and its shadow in 3D space.
     *
     * @param g2d        the graphics context to paint on
     * @param cam        the {@link Camera3D} used for perspective calculations
     * @param windowData the {@link WindowData} containing screen dimensions
     */
    public void render(Graphics2D g2d, Camera3D cam, WindowData windowData) {
        final double scale = cam.getScale(z);
        if (scale <= 0) return;
        final double vx = 0;
        final double vy = 0;

        drawShadow(g2d, cam, windowData, vx);

        final int screenX = (int) (windowData.width() / 2. + (currentX + vx - cam.getX()) * scale);
        final int screenY = (int) (windowData.height() / 3. + (currentY + vy - radius - cam.getY()) * scale);
        final int scaledRadiusX = (int) (radius * scale * scaleMultiplier);
        final int scaledRadiusY = (int) (radius * scale * scaleMultiplier);

        final int a = (int) (255 * Math.clamp(alpha, 0, 1.0f));
        if (a <= 0 || scaledRadiusX <= 0 || scaledRadiusY <= 0) return;

        g2d.setColor(RenderCache.magentaWithAlpha(a));
        g2d.fillOval(screenX - scaledRadiusX, screenY - scaledRadiusY, scaledRadiusX * 2, scaledRadiusY * 2);
    }

    /**
     * Renders the drop shadow of the sphere on the ground plane.
     *
     * @param g2d        the graphics context
     * @param cam        the camera used for projection
     * @param windowData screen metadata
     * @param vx         additional horizontal vibration offset
     */
    public void drawShadow(Graphics2D g2d, Camera3D cam, WindowData windowData, double vx) {
        final double scale = cam.getScale(z);
        final double groundY = 150;
        final int shadowScreenX = (int) (windowData.width() / 2. + (currentX + vx - cam.getX()) * scale);
        final int shadowScreenY = (int) (windowData.height() / 3. + (groundY - cam.getY()) * scale);
        final double heightFactor = Math.max(0, (groundY - currentY) / 300.0);
        final float shadowAlpha = (float) Math.max(0, 0.4 - heightFactor * 0.3);
        final int shadowSizeX = (int) (radius * scale * (1.2 - heightFactor * 0.4));
        final int shadowSizeY = (int) (shadowSizeX * 0.4);
        if (shadowAlpha > 0 && shadowSizeX > 0 && shadowSizeY > 0) {
            g2d.setColor(RenderCache.blackWithAlpha((int) (255 * shadowAlpha)));
            g2d.fillOval(shadowScreenX - shadowSizeX, shadowScreenY - shadowSizeY / 2, shadowSizeX * 2, shadowSizeY);
        }
    }
}
