package cz.matysekxx.beatbounce.model.game.state;

import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.game.GameEngine;
import cz.matysekxx.beatbounce.model.score.ScoreManager;
import cz.matysekxx.beatbounce.util.LevelUtil;

public class FallingHandler implements GameStateHandler {
    private final GameEngine gameEngine;
    private final Sphere sphere;

    public FallingHandler(GameEngine gameEngine, Sphere sphere) {
        this.gameEngine = gameEngine;
        this.sphere = sphere;
    }

    @Override
    public void handle(double currentTime, double deltaTime) {
        sphere.update(currentTime, deltaTime);
        sphere.setZ(gameEngine.getFallStartZ());
        gameEngine.getCam().setZ(gameEngine.getGameZProgress() - 500);
        if (sphere.getCurrentY() > 500) {
            gameEngine.setGameState(GameState.GAME_OVER);
            ScoreManager.updateScore(LevelUtil.getCleanSongName(gameEngine.getLevel()), gameEngine.getScore());
        }
    }
}