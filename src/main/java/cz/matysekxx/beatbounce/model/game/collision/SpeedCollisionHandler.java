package cz.matysekxx.beatbounce.model.game.collision;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.SpeedTile;
import cz.matysekxx.beatbounce.model.game.GameEngine;

public class SpeedCollisionHandler extends CollisionHandler {
    private static final double SPEED_EFFECT_DURATION = 3.0;

    protected SpeedCollisionHandler(GameEngine gameEngine) {
        super(gameEngine);
    }

    @Override
    public void handle(AbstractTile tile) {
        if (tile instanceof SpeedTile st) {
            advanceTile(10);
            if (!st.isActivated()) {
                st.activate();
                gameEngine.setSpeedEffectActive(true);
                gameEngine.setSpeedEffectTimeRemaining(SPEED_EFFECT_DURATION);
                gameEngine.setActiveSpeedMultiplier(st.getSpeedMultiplier());
            }
        }
    }
}
