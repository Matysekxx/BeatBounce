package cz.matysekxx.beatbounce.model.game.collision;

import cz.matysekxx.beatbounce.model.entity.*;
import cz.matysekxx.beatbounce.model.game.GameEngine;

import java.util.HashMap;

public class CollisionEngine {
    private static final int LANE_WIDTH = 120;
    private static final double NORMAL_HALF_WIDTH = LANE_WIDTH / 2.0;
    private static final double SMALL_HALF_WIDTH = 30.0;
    private final HashMap<Class<? extends AbstractTile>, CollisionHandler> collisionHandlers = new HashMap<>();
    private final GameEngine gameEngine;

    public CollisionEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        collisionHandlers.put(BreakableTile.class, new BreakableCollisionHandler(gameEngine));
        collisionHandlers.put(SpeedTile.class, new SpeedCollisionHandler(gameEngine));
        collisionHandlers.put(LongTile.class, new LongCollisionHandler(gameEngine));
        collisionHandlers.put(SmallTile.class, new SmallCollisionHandler(gameEngine));
        collisionHandlers.put(NormalTile.class, new NormalCollisionHandler(gameEngine));
        collisionHandlers.put(MovingTile.class, new MovingCollisionHandler(gameEngine));
    }

    public void handleCollisions() {
        if (isLastTile()) return;

        final AbstractTile nextTile = gameEngine.getNextTile();

        if (handleLongTile(nextTile)) return;

        if (gameEngine.getGameZProgress() < nextTile.getZ()) return;

        if (isPlayerFalling(nextTile)) {
            gameEngine.startFalling();
            return;
        }

        processTileCollision(nextTile);
    }

    private boolean isLastTile() {
        return gameEngine.getCurrentTileIndex() + 1 >= gameEngine.getTilesSize();
    }

    private boolean handleLongTile(AbstractTile nextTile) {
        if (!gameEngine.isOnLongTile() || gameEngine.getCurrentTileIndex() < 0) {
            gameEngine.setOnLongTile(false);
            return false;
        }

        final AbstractTile curTile = gameEngine.getCurrentTile();
        if (!(curTile instanceof LongTile lt)) {
            gameEngine.setOnLongTile(false);
            return false;
        }

        final double timeToNextTile = (nextTile.getZ() - gameEngine.getGameZProgress()) / gameEngine.getzUnitsPerSecond();
        final boolean shouldJumpEarly = timeToNextTile <= 0.25;

        if (gameEngine.getGameZProgress() <= lt.getZ() + lt.getLengthInZ() && !shouldJumpEarly) {
            gameEngine.setLongTileScoreAccum(gameEngine.getLongTileScoreAccum() + 1);
            if (gameEngine.getLongTileScoreAccum() % 6 == 0) {
                gameEngine.setScore(gameEngine.getScore() + 1);
            }
            return true;
        }

        gameEngine.setOnLongTile(false);
        gameEngine.startNextJump(gameEngine.getSmoothedAudioTime());
        return false;
    }

    private boolean isPlayerFalling(AbstractTile nextTile) {
        final double halfWidth = getTileHalfWidth(nextTile);
        final double tileMinX = nextTile.getX() - halfWidth;
        final double tileMaxX = nextTile.getX() + halfWidth;

        final double playerX = gameEngine.getSphere().getX();
        return playerX < tileMinX || playerX > tileMaxX;
    }

    private double getTileHalfWidth(AbstractTile tile) {
        return (tile instanceof SmallTile)
                ? SMALL_HALF_WIDTH + gameEngine.getSphere().getRadius()
                : NORMAL_HALF_WIDTH + gameEngine.getSphere().getRadius();
    }

    private void processTileCollision(AbstractTile nextTile) {
        if (collisionHandlers.containsKey(nextTile.getClass())) {
            collisionHandlers.get(nextTile.getClass()).handle(nextTile);
        }
    }
}