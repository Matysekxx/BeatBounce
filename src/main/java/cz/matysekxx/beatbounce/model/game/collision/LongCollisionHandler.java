package cz.matysekxx.beatbounce.model.game.collision;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.LongTile;
import cz.matysekxx.beatbounce.model.game.GameEngine;

public class LongCollisionHandler extends  CollisionHandler {
    protected LongCollisionHandler(GameEngine gameEngine) {
        super(gameEngine);
    }

    @Override
    public void handle(AbstractTile tile) {
        if (tile instanceof LongTile) {
            gameEngine.setCurrentTileIndex(gameEngine.getCurrentTileIndex() + 1);
            gameEngine.setOnLongTile(true);
            gameEngine.setLongTileScoreAccum(0);
            gameEngine.setScore(gameEngine.getScore() + 5);
            gameEngine.getSphere().cancelJump();
        }
    }
}
