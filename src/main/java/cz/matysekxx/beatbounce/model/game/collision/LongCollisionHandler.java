package cz.matysekxx.beatbounce.model.game.collision;

import cz.matysekxx.beatbounce.gui.components.ScorePopup;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.LongTile;
import cz.matysekxx.beatbounce.model.game.GameEngine;

/**
 * Handles complex collision logic for {@link LongTile}s.
 * Manages the state when the player is rolling over a long tile, awards continuous
 * score, and handles the transition to the next jump at the tile's end.
 */
public class LongCollisionHandler extends CollisionHandler {
    /**
     * Whether the player is currently positioned on top of a long tile.
     */
    boolean onLongTile = false;

    /**
     * Constructs a new LongCollisionHandler.
     *
     * @param gameEngine the game engine
     */
    public LongCollisionHandler(GameEngine gameEngine) {
        super(gameEngine);
    }

    /**
     * Returns whether the player is currently on a long tile.
     */
    public boolean isOnLongTile() {
        return onLongTile;
    }

    /**
     * Sets whether the player is currently on a long tile.
     *
     * @param onLongTile true if the player should be in the long-tile state
     */
    public void setOnLongTile(boolean onLongTile) {
        this.onLongTile = onLongTile;
    }

    /**
     * Initiates the long-tile state when the player lands on its front edge.
     *
     * @param tile the long tile landed on
     */
    @Override
    public void handle(AbstractTile tile) {
        if (tile instanceof LongTile) {
            tile.onLanding();
            this.onLongTile = true;
            gameEngine.setCurrentTileIndex(gameEngine.getCurrentTileIndex() + 1);
            gameEngine.setLongTileScoreAccum(0);
        }
    }

    /**
     * Processes the continuous frame-by-frame collision logic while on a long tile.
     * Checks if the player stays within the tile's width and calculates score increments.
     *
     * @param currentTile the long tile currently being rolled over
     * @param nextTile    the upcoming tile in the level
     * @return true if the player is still on the tile and no jump was triggered
     */
    public boolean processContinuous(AbstractTile currentTile, AbstractTile nextTile) {
        if (!onLongTile || !(currentTile instanceof LongTile lt)) {
            this.onLongTile = false;
            return false;
        }

        final double timeToNextTile = (nextTile.getZ() - gameEngine.getGameZProgress()) / gameEngine.getzUnitsPerSecond();
        final boolean shouldJumpEarly = timeToNextTile <= 0.25;

        final double sphereX = gameEngine.getSphere().getX();
        final double tileX = lt.getX();
        if (Math.abs(sphereX - tileX) > 60) {
            this.onLongTile = false;
            gameEngine.startFalling();
            return false;
        }

        if (gameEngine.getGameZProgress() <= lt.getZ() + lt.getLengthInZ() && !shouldJumpEarly) {
            gameEngine.setLongTileScoreAccum(gameEngine.getLongTileScoreAccum() + 1);
            if (gameEngine.getLongTileScoreAccum() % 6 == 0) {
                gameEngine.setScore(gameEngine.getScore() + 1);
                gameEngine.addScorePopup(ScorePopup.createRandom(1, 0, 120));
            }
            return true;
        } else {
            this.onLongTile = false;
            gameEngine.startNextJump(gameEngine.getSmoothedAudioTime());
            return false;
        }
    }
}
