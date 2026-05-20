package cz.matysekxx.beatbounce.model.game;

import cz.matysekxx.beatbounce.model.entity.Orb;

public class OrbCollisionEngine {
    private final GameEngine gameEngine;
    private double lastSphereZ = -1;

    public OrbCollisionEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

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
