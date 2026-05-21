package cz.matysekxx.beatbounce.model.game.collision;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.SmallTile;
import cz.matysekxx.beatbounce.model.game.GameEngine;

/**
 * Handles collisions with {@link SmallTile}s.
 * Awards higher points due to increased difficulty and advances the game state.
 */
public class SmallCollisionHandler extends CollisionHandler {
    /**
     * Constructs a new SmallCollisionHandler.
     *
     * @param gameEngine the game engine
     */
    protected SmallCollisionHandler(GameEngine gameEngine) {
        super(gameEngine);
    }

    /**
     * Processes the collision logic for a small tile.
     *
     * @param tile the tile being collided with
     */
    @Override
    public void handle(AbstractTile tile) {
        final int scoreIncrease = 15;
        advanceTile(tile, scoreIncrease);
    }
}
