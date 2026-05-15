package cz.matysekxx.beatbounce.model.game;

import cz.matysekxx.beatbounce.model.entity.Orb;

public class OrbCollisionEngine {
    private final GameEngine gameEngine;

    public OrbCollisionEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public void checkOrbCollisions() {
        for (Orb orb : gameEngine.getOrbs()) {
            if (!orb.isCollected()) {
                final double dz = orb.getZ() - gameEngine.getSphere().getZ();
                final double dx = orb.getX() - gameEngine.getSphere().getX();
                final double dy = orb.getY() - gameEngine.getSphere().getCurrentY();
                if (dz * dz + dx * dx + dy * dy < 6400) {
                    orb.setCollected(true);
                    gameEngine.incrementCollectedOrbs();
                }
            }
        }
    }
}
