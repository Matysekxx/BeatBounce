package cz.matysekxx.beatbounce.model.game.collision;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.game.GameEngine;

public class NormalCollisionHandler extends CollisionHandler {
    protected NormalCollisionHandler(GameEngine gameEngine) {
        super(gameEngine);
    }

    @Override
    public void handle(AbstractTile tile) {
        final int scoreIncrease = 10;
        advanceTile(scoreIncrease);
    }
}
