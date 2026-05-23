package cz.matysekxx.beatbounce.model.game.collision;

import cz.matysekxx.beatbounce.model.entity.*;
import cz.matysekxx.beatbounce.model.game.GameEngine;

import java.util.HashMap;

/**
 * Orchestrates collision detection between the player and the level tiles.
 * It uses specialized {@link CollisionHandler}s for different tile types and
 * implements Continuous Collision Detection (CCD) to prevent skipping tiles at high speeds.
 */
public class CollisionEngine {
    /**
     * Standard width of a single lane in world units.
     */
    private static final int LANE_WIDTH = 120;

    /**
     * Half-width of a normal tile for collision checks.
     */
    private static final double NORMAL_HALF_WIDTH = LANE_WIDTH / 2.0;

    /**
     * Half-width of a small tile for collision checks.
     */
    private static final double SMALL_HALF_WIDTH = 25.0;

    /**
     * Mapping of tile classes to their respective collision handlers.
     */
    private final HashMap<Class<? extends AbstractTile>, CollisionHandler> collisionHandlers = new HashMap<>();

    /**
     * The game engine instance.
     */
    private final GameEngine gameEngine;

    /**
     * The cumulative Z-progress from the previous frame, used for CCD.
     */
    private double lastZProgress = -1;


    /**
     * Constructs a new CollisionEngine and registers handlers for all tile types.
     *
     * @param gameEngine the game engine
     */
    public CollisionEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        collisionHandlers.put(BreakableTile.class, new BreakableCollisionHandler(gameEngine));
        collisionHandlers.put(LongTile.class, new LongCollisionHandler(gameEngine));
        collisionHandlers.put(SmallTile.class, new SmallCollisionHandler(gameEngine));
        collisionHandlers.put(NormalTile.class, new NormalCollisionHandler(gameEngine));
        collisionHandlers.put(MovingTile.class, new MovingCollisionHandler(gameEngine));
    }

    /**
     * Returns the next tile the player is expected to land on.
     */
    public AbstractTile getNextTile() {
        return gameEngine.getLevel().tiles().get(gameEngine.getCurrentTileIndex() + 1);
    }

    /**
     * Returns the tile the player is currently on.
     */
    public AbstractTile getCurrentTile() {
        return gameEngine.getLevel().tiles().get(gameEngine.getCurrentTileIndex());
    }

    /**
     * Returns the total number of tiles in the level.
     */
    public int getTilesSize() {
        return gameEngine.getLevel().tiles().size();
    }

    /**
     * Resets the CCD state, ensuring the next collision check initializes its Z-interval.
     */
    public void resetCCD() {
        this.lastZProgress = -1;
    }

    /**
     * Sets whether the player is currently on a long tile.
     *
     * @param onLongTile true if the player should be in the long-tile state
     */
    public void setOnLongTile(boolean onLongTile) {
        final LongCollisionHandler handler = (LongCollisionHandler) collisionHandlers.get(LongTile.class);
        if (handler != null) {
            handler.setOnLongTile(onLongTile);
        }
    }

    /**
     * Performs collision checks for the current frame.
     * Uses the Z-interval since the last update to ensure no tiles are missed.
     */
    public void handleCollisions() {
        if (isLastTile()) return;
        final AbstractTile nextTile = getNextTile();
        if (nextTile == null) return;

        final double currentZ = gameEngine.getGameZProgress();
        if (lastZProgress < 0) {
            lastZProgress = currentZ;
            return;
        }

        final LongCollisionHandler longCollisionHandler = (LongCollisionHandler) collisionHandlers.get(LongTile.class);
        if (longCollisionHandler.isOnLongTile()) {
            final AbstractTile currentTile = getCurrentTile();
            if (currentTile == null) {
                longCollisionHandler.onLongTile = false;
                lastZProgress = currentZ;
                return;
            }
            if (longCollisionHandler.processContinuous(currentTile, nextTile)) {
                lastZProgress = currentZ;
                return;
            }
        }
        if (nextTile.getZ() >= lastZProgress && nextTile.getZ() <= currentZ) {
            if (isPlayerFalling(nextTile)) gameEngine.startFalling();
            else processTileCollision(nextTile);
        }
        lastZProgress = currentZ;
    }

    /**
     * Checks if the player has reached the final tile of the level.
     */
    private boolean isLastTile() {
        return gameEngine.getCurrentTileIndex() + 1 >= getTilesSize();
    }

    /**
     * Determines if the player's horizontal position is outside the bounds of the specified tile.
     *
     * @param nextTile the tile to check against
     * @return true if the player is falling (missed the tile)
     */
    private boolean isPlayerFalling(AbstractTile nextTile) {
        final double halfWidth = getTileHalfWidth(nextTile);
        final double tileMinX = nextTile.getX() - halfWidth;
        final double tileMaxX = nextTile.getX() + halfWidth;

        final double playerX = gameEngine.getSphere().getX();
        return playerX < tileMinX || playerX > tileMaxX;
    }

    /**
     * Calculates the effective collision half-width of a tile, accounting for player radius.
     */
    private double getTileHalfWidth(AbstractTile tile) {
        return (tile instanceof SmallTile)
                ? SMALL_HALF_WIDTH + gameEngine.getSphere().getRadius()
                : NORMAL_HALF_WIDTH + gameEngine.getSphere().getRadius();
    }

    /**
     * Delegates specific collision logic to the registered handler for the tile type.
     */
    private void processTileCollision(AbstractTile nextTile) {
        if (collisionHandlers.containsKey(nextTile.getClass())) {
            collisionHandlers.get(nextTile.getClass()).handle(nextTile);
        }
    }
}
