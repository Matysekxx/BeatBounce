package cz.matysekxx.beatbounce.model.game;

import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.BreakableTile;
import cz.matysekxx.beatbounce.model.entity.MovingTile;

public class TileManager {
    private final GameEngine gameEngine;

    public TileManager(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public void update(double deltaTime) {
        for (AbstractTile tile : gameEngine.getUpdatableTiles()) {
            switch (tile) {
                case MovingTile movingTile -> {
                    final double distance = gameEngine.getCam().getDistanceTo(tile.getZ());
                    if (distance <= 0 || distance > 3000) continue;

                    movingTile.update(deltaTime);
                    int newX = movingTile.getX();

                    if (newX < -RenderUtils.ROAD_WIDTH) newX = -RenderUtils.ROAD_WIDTH;
                    else if (newX > RenderUtils.ROAD_WIDTH) newX = RenderUtils.ROAD_WIDTH;

                    movingTile.setLocation(newX, movingTile.getY());
                }
                case BreakableTile bt when bt.isBroken() -> bt.updateBreakAnimation(deltaTime);
                default -> {
                }
            }
        }
    }
}
