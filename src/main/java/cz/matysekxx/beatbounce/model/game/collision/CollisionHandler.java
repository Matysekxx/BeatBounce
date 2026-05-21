package cz.matysekxx.beatbounce.model.game.collision;

import cz.matysekxx.beatbounce.gui.components.ScorePopup;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.game.GameEngine;

/**
 * Abstract base class for tile-specific collision handlers.
 * Provides shared utility methods for advancing the game state upon successful landing.
 */
public abstract class CollisionHandler {
    /**
     * The game engine instance for state updates.
     */
    protected final GameEngine gameEngine;

    /**
     * Constructs a new CollisionHandler.
     *
     * @param gameEngine the game engine
     */
    protected CollisionHandler(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    /**
     * Defines the specific collision logic for a tile variant.
     *
     * @param tile the tile being collided with
     */
    public abstract void handle(AbstractTile tile);

    /**
     * Updates the game state after a successful landing on a tile.
     * Marks the tile as activated, increments the score, and initiates the next jump.
     *
     * @param tile          the tile landed on
     * @param scoreIncrease the amount to add to the total score
     */
    protected void advanceTile(AbstractTile tile, int scoreIncrease) {
        tile.onLanding();
        gameEngine.setCurrentTileIndex(gameEngine.getCurrentTileIndex() + 1);
        gameEngine.setScore(gameEngine.getScore() + scoreIncrease);
        gameEngine.startNextJump(gameEngine.getSmoothedAudioTime());

        gameEngine.addScorePopup(ScorePopup.createRandom(
                scoreIncrease, 0, 120
        ));
    }
}
