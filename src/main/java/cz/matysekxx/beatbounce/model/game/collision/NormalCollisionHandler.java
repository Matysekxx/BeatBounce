package cz.matysekxx.beatbounce.model.game.collision;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.NormalTile;
import cz.matysekxx.beatbounce.model.game.GameEngine;

/**
 * Handles collisions with {@link NormalTile}s.
 * Awards standard points and advances the game state.
 *
 * @author Matysekxx
 */
public class NormalCollisionHandler extends CollisionHandler {
    /**
     * Constructs a new NormalCollisionHandler.
     *
     * @param gameEngine the game engine
     */
    protected NormalCollisionHandler(GameEngine gameEngine) {
        super(gameEngine);
    }

    /**
     * Processes the collision logic for a normal tile.
     *
     * @param tile the tile being collided with
     */
    @Override
    public void handle(AbstractTile tile) {
        final int scoreIncrease = 10;
        advanceTile(tile, scoreIncrease);
    }
}
