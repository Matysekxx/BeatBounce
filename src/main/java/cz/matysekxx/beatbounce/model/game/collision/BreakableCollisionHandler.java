package cz.matysekxx.beatbounce.model.game.collision;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.BreakableTile;
import cz.matysekxx.beatbounce.model.game.GameEngine;

/**
 * Handles collisions with {@link BreakableTile}s.
 * If the tile is already broken, the player falls. Otherwise, the tile is broken
 * and the player is awarded extra points.
 */
public class BreakableCollisionHandler extends CollisionHandler {

    /**
     * Constructs a new BreakableCollisionHandler.
     *
     * @param gameEngine the game engine
     */
    protected BreakableCollisionHandler(GameEngine gameEngine) {
        super(gameEngine);
    }

    /**
     * Processes the collision logic for a breakable tile.
     *
     * @param tile the tile being collided with
     */
    @Override
    public void handle(AbstractTile tile) {
        if (tile instanceof BreakableTile bt) {
            if (bt.isBroken()) {
                gameEngine.startFalling();
                return;
            }
            bt.breakTile();
            advanceTile(tile, 12);
        }
    }
}
