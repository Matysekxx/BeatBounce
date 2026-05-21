package cz.matysekxx.beatbounce.model.game.collision;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.MovingTile;
import cz.matysekxx.beatbounce.model.game.GameEngine;

/**
 * Handles collisions with {@link MovingTile}s.
 * Awards standard points and advances the game state.
 */
public class MovingCollisionHandler extends CollisionHandler {
    /**
     * Constructs a new MovingCollisionHandler.
     *
     * @param gameEngine the game engine
     */
    protected MovingCollisionHandler(GameEngine gameEngine) {
        super(gameEngine);
    }

    /**
     * Processes the collision logic for a moving tile.
     *
     * @param tile the tile being collided with
     */
    @Override
    public void handle(AbstractTile tile) {
        final int scoreIncrease = 10;
        advanceTile(tile, scoreIncrease);
    }
}
