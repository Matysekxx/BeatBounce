package cz.matysekxx.beatbounce.model.game.collision;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.game.GameEngine;

public abstract class CollisionHandler {
    protected final GameEngine gameEngine;

    protected CollisionHandler(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public abstract void handle(AbstractTile tile);

    protected void advanceTile(int scoreIncrease) {
        gameEngine.setCurrentTileIndex(gameEngine.getCurrentTileIndex() + 1);
        gameEngine.setScore(gameEngine.getScore() + scoreIncrease);
        gameEngine.startNextJump(gameEngine.getSmoothedAudioTime());
    }
}
