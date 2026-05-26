package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.WindowData;

import java.awt.*;

/**
 * The {@code Sphere} class represents the player character in the game world.
 * It extends {@link Entity} and manages the player's physical state,
 * including movement, jumping, falling, and visual representation.
 * <p>
 * Key features:
 * <ul>
 *   <li><b>Horizontal Movement:</b> Uses exponential smoothing (LERP) for fluid transitions between lanes.</li>
 *   <li><b>Jumping:</b> Implements a parabolic trajectory based on world time and beat duration.</li>
 *   <li><b>Visuals:</b> Handles transparency (alpha), scaling, and dynamic shadow rendering.</li>
 * </ul>
 */
public class Sphere extends Entity {
    /**
     * Physical radius of the sphere in world units.
     * Used for collision detection and base rendering size.
     */
    private final int radius;

    /**
     * Current depth position along the Z-axis.
     * This value increases as the player progresses through the level.
     */
    private double z;

    /**
     * The horizontal world coordinate (X) that the sphere is currently moving towards.
     * Target position is usually the center of a lane.
     */
    private double targetX;

    /**
     * Exact current horizontal world coordinate (X).
     * This value is interpolated towards {@code targetX} in the {@link #update(double, double)} method.
     */
    private double currentX;

    /**
     * Exact current vertical world coordinate (Y).
     * Managed by jump or fall logic; ground level is typically at Y=150.
     */
    private double currentY;

    /**
     * World time (in seconds) when the current jump started.
     * Used as a baseline for parabolic progress calculation.
     */
    private double jumpStartTime;

    /**
     * Total expected duration of the current jump in seconds.
     * Usually matches the time between two beats.
     */
    private double jumpDuration;

    /**
     * The maximum height (vertical offset) reached during the current jump.
     */
    private double peakHeight;

    /**
     * Flag indicating if the sphere is currently performing a jump animation.
     */
    private boolean isJumping;

    /**
     * Flag indicating if the sphere has missed a tile and is falling into the void.
     */
    private boolean isFalling = false;
    /**
     * Current transparency alpha level of the sphere (0.0 for fully transparent to 1.0 for fully opaque).
     */
    private float alpha = 1.0f;
    /**
     * Current visual scale multiplier for animations.
     * Values > 1.0 make the sphere appear larger, < 1.0 smaller.
     */
    private float scaleMultiplier = 1.0f;

    /**
     * Constructs a new {@code Sphere} with specified coordinates and radius.
     *
     * @param x      the initial horizontal position
     * @param y      the initial vertical position
     * @param z      the initial depth position
     * @param radius the base radius of the sphere
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
     * Returns whether the sphere is currently falling.
     *
     * @return {@code true} if falling, {@code false} otherwise
     */
    public boolean isFalling() {
        return isFalling;
    }

    /**
     * Initiates a jump animation.
     * Calculates the trajectory based on a parabolic arc over the specified duration.
     *
     * @param startTime the game world time when the jump starts
     * @param duration  the time it takes to complete the jump arc
     * @param height    the maximum vertical displacement from the ground
     */
    public void startJump(double startTime, double duration, double height) {
        this.jumpStartTime = startTime;
        this.jumpDuration = duration;
        this.peakHeight = height;
        this.isJumping = true;
        this.isFalling = false;
    }

    /**
     * Immediately cancels the current jump and resets the sphere to ground level (Y=150).
     */
    public void cancelJump() {
        this.isJumping = false;
        this.currentY = 150;
        this.y = 150;
    }

    /**
     * Updates the sphere's position and internal state.
     * <p>
     * Horizontal movement uses exponential decay for a smooth "organic" feel:
     * {@code currentX += (targetX - currentX) * (1 - e^(-25 * deltaTime))}.
     * <p>
     * Vertical movement follows a parabolic arc if jumping:
     * {@code y = 4 * peakHeight * progress * (1 - progress)}.
     *
     * @param currentTime the current game time in seconds
     * @param deltaTime   the time elapsed since the last frame in seconds
     */
    public void update(double currentTime, double deltaTime) {
        final double lerpFactor = 1.0 - Math.exp(-25 * deltaTime);
        currentX += (targetX - currentX) * lerpFactor;
        this.x = (int) currentX;

        if (isFalling) {
            final double constantFallVelocity = 600.0;
            currentY += constantFallVelocity * deltaTime;
        } else if (isJumping) {
            final double elapsed = currentTime - jumpStartTime;
            final double progress = elapsed / jumpDuration;
            if (progress >= 1.0) {
                isJumping = false;
                currentY = 150;
            } else if (progress > 0) {
                final double jumpYOffset = 4 * peakHeight * progress * (1 - progress);
                currentY = 150 - jumpYOffset;
            } else {
                currentY = 150;
            }
        } else {
            currentY = 150;
        }
        this.y = (int) currentY;
    }

    /**
     * Returns the target horizontal world coordinate.
     *
     * @return the {@code targetX} value
     */
    public double getTargetX() {
        return targetX;
    }

    /**
     * Sets a new target horizontal coordinate. The sphere will smoothly glide towards this value.
     *
     * @param targetX the new target X world coordinate
     */
    public void setTargetX(double targetX) {
        this.targetX = targetX;
    }

    /**
     * Snaps the horizontal position to a specific value, bypassing interpolation.
     *
     * @param x the new X world coordinate
     */
    public void setCurrentX(double x) {
        this.currentX = x;
        this.targetX = x;
        this.x = (int) x;
    }

    /**
     * Returns the current exact vertical world coordinate.
     *
     * @return the {@code currentY} value
     */
    public double getCurrentY() {
        return currentY;
    }

    /**
     * Directly sets the current vertical position.
     *
     * @param y the new Y world coordinate
     */
    public void setCurrentY(double y) {
        this.currentY = y;
        this.y = (int) y;
    }

    /**
     * Triggers the falling state. The sphere will accelerate downwards indefinitely.
     */
    public void startFalling() {
        isFalling = true;
        isJumping = false;
    }

    /**
     * Resets the sphere to its default starting position and state.
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
        jumpStartTime = 0;
    }

    /**
     * Restores the sphere to a playable state after a revive event.
     */
    public void revive() {
        this.isFalling = false;
        this.isJumping = false;
        this.currentY = 150;
        this.y = 150;
        this.alpha = 1.0f;
        this.scaleMultiplier = 1.0f;
        this.jumpStartTime = 0;
        this.jumpDuration = 1.0;
        this.peakHeight = 0;
    }

    /**
     * Returns whether the sphere is currently in the air.
     *
     * @return {@code true} if jumping, {@code false} otherwise
     */
    public boolean isJumping() {
        return isJumping;
    }

    /**
     * Returns the world time when the current jump arc is scheduled to complete.
     *
     * @return the jump end time in seconds
     */
    public double getJumpEndTime() {
        return jumpStartTime + jumpDuration;
    }

    /**
     * Returns the current depth position.
     *
     * @return the {@code z} coordinate
     */
    public double getZ() {
        return z;
    }

    /**
     * Updates the depth position.
     *
     * @param z the new Z world coordinate
     */
    public void setZ(double z) {
        this.z = z;
    }

    /**
     * Returns the current visual transparency.
     *
     * @return the {@code alpha} value (0.0 to 1.0)
     */
    public float getAlpha() {
        return alpha;
    }

    /**
     * Sets the visual transparency of the sphere.
     *
     * @param alpha the new alpha value (0.0 to 1.0)
     */
    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    /**
     * Returns the current visual scale multiplier.
     *
     * @return the {@code scaleMultiplier} value
     */
    public float getScaleMultiplier() {
        return scaleMultiplier;
    }

    /**
     * Sets a scale multiplier for visual effects (e.g., squashing on landing).
     *
     * @param scaleMultiplier the new scale multiplier
     */
    public void setScaleMultiplier(float scaleMultiplier) {
        this.scaleMultiplier = scaleMultiplier;
    }

    /**
     * Returns the base physical radius of the sphere.
     *
     * @return the {@code radius} value
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Renders the sphere and its drop shadow in 3D perspective.
     *
     * @param g2d        the graphics context to paint on
     * @param cam        the {@link Camera3D} used for perspective projection
     * @param windowData metadata about the rendering window dimensions
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
     * Renders a drop shadow on the ground plane (Y=150).
     * The shadow's size and transparency are dynamically calculated based on the sphere's current altitude.
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
