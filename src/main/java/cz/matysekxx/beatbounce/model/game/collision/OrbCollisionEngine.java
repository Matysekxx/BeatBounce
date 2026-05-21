package cz.matysekxx.beatbounce.model.game.collision;

import cz.matysekxx.beatbounce.model.entity.Orb;
import cz.matysekxx.beatbounce.model.game.GameEngine;

/**
 * Handles the detection of collisions between the player sphere and collectible orbs.
 * Implements Continuous Collision Detection (CCD) to ensure no orbs are skipped at high speeds.
 */
public class OrbCollisionEngine {
    /**
     * The game engine providing state and entity data.
     */
    private final GameEngine gameEngine;

    /**
     * The Z-coordinate of the sphere in the previous frame, used for CCD.
     */
    private double lastSphereZ = -1;

    /**
     * Constructs a new OrbCollisionEngine.
     *
     * @param gameEngine the game engine
     */
    public OrbCollisionEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    /**
     * Checks for collisions between the sphere and all active orbs.
     * Uses the Z-interval between the last and current frame to prevent tunneling.
     */
    public void checkOrbCollisions() {
        final double currentZ = gameEngine.getSphere().getZ();
        final double currentX = gameEngine.getSphere().getX();
        final double currentY = gameEngine.getSphere().getCurrentY();

        if (lastSphereZ < 0) {
            lastSphereZ = currentZ;
            return;
        }

        for (Orb orb : gameEngine.getOrbs()) {
            if (!orb.isCollected()) {
                if (orb.getZ() >= lastSphereZ && orb.getZ() <= currentZ) {
                    final double dx = orb.getX() - currentX;
                    final double dy = orb.getY() - currentY;
                    if (dx * dx + dy * dy < 6400) {
                        orb.setCollected(true);
                        gameEngine.incrementCollectedOrbs();
                    }
                }
            }
        }
        lastSphereZ = currentZ;
    }
}
