package cz.matysekxx.beatbounce.model.game;

import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.BreakableTile;
import cz.matysekxx.beatbounce.model.entity.MovingTile;

/**
 * Manages the state updates for all tiles in the level.
 * This includes advancing animations for impact effects, moving tiles, and shattering tiles.
 *
 * @author Matysekxx
 */
public class TileManager {
    /**
     * The game engine providing level and camera data.
     */
    private final GameEngine gameEngine;

    /**
     * Constructs a new TileManager.
     *
     * @param gameEngine the game engine
     */
    public TileManager(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    /**
     * Updates all tiles in the level for the current frame.
     * Only updates moving tiles that are within a reasonable distance from the camera.
     *
     * @param deltaTime time since last frame in seconds
     */
    public void update(double deltaTime) {
        for (AbstractTile tile : gameEngine.getLevel().tiles()) {
            tile.updateImpact(deltaTime);
        }

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
