package cz.matysekxx.beatbounce.model.game.collision;

import cz.matysekxx.beatbounce.gui.components.ScorePopup;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.LongTile;
import cz.matysekxx.beatbounce.model.game.GameEngine;

public class LongCollisionHandler extends CollisionHandler {
    boolean onLongTile = false;

    public LongCollisionHandler(GameEngine gameEngine) {
        super(gameEngine);
    }

    public boolean isOnLongTile() {
        return onLongTile;
    }

    @Override
    public void handle(AbstractTile tile) {
        if (tile instanceof LongTile) {
            tile.onLanding();
            this.onLongTile = true;
            gameEngine.setCurrentTileIndex(gameEngine.getCurrentTileIndex() + 1);
            gameEngine.setLongTileScoreAccum(0);
        }
    }

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