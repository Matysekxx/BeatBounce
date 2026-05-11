package cz.matysekxx.beatbounce.model.game.collision;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.BreakableTile;
import cz.matysekxx.beatbounce.model.game.GameEngine;

public class BreakableCollisionHandler extends  CollisionHandler {

    protected BreakableCollisionHandler(GameEngine gameEngine) {
        super(gameEngine);
    }

    @Override
    public void handle(AbstractTile tile) {
        if (tile instanceof BreakableTile bt) {
            if (bt.isBroken()) {
                gameEngine.startFalling();
                return;
            }
            bt.breakTile();
            advanceTile(12);
        }
    }
}
