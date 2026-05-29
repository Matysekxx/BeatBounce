package cz.matysekxx.beatbounce.model.game.state;

import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.game.GameEngine;
import cz.matysekxx.beatbounce.model.score.ScoreManager;
import cz.matysekxx.beatbounce.util.LevelUtil;

/**
 * Handles the logic for the {@link GameState#FALLING} state.
 * Manages the sphere's downward movement after a miss and transitions the game
 * to the {@link GameState#GAME_OVER} state once the sphere falls out of view.
 */
public class FallingHandler implements GameStateHandler {
    /**
     * The game engine providing state data.
     */
    private final GameEngine gameEngine;

    /**
     * The player character.
     */
    private final Sphere sphere;

    /**
     * Constructs a new FallingHandler.
     *
     * @param gameEngine the game engine
     * @param sphere     the player character
     */
    public FallingHandler(GameEngine gameEngine, Sphere sphere) {
        this.gameEngine = gameEngine;
        this.sphere = sphere;
    }

    /**
     * Updates the sphere's falling animation and checks for the game over condition.
     *
     * @param currentTime the current world time
     * @param deltaTime   time since last frame in seconds
     */
    @Override
    public void handle(double currentTime, double deltaTime) {
        sphere.update(currentTime, deltaTime);
        sphere.setZ(gameEngine.getFallStartZ());
        gameEngine.getCam().setZ(gameEngine.getGameZProgress() - 500);
        if (sphere.getCurrentY() > 500) {
            gameEngine.setGameState(GameState.GAME_OVER);
            final String songName = LevelUtil.getCleanSongName(gameEngine.getLevel());
            gameEngine.setNewHighScore(ScoreManager.isHighScore(songName, gameEngine.getScore()));
            ScoreManager.updateScore(songName, gameEngine.getScore());
        }
    }
}
