package cz.matysekxx.beatbounce.model.game.collision;

import cz.matysekxx.beatbounce.gui.components.ScorePopup;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.game.GameEngine;

import java.awt.*;

public abstract class CollisionHandler {
    protected final GameEngine gameEngine;

    protected CollisionHandler(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public abstract void handle(AbstractTile tile);

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
