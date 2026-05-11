package cz.matysekxx.beatbounce.model.game.state;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.model.game.GameEngine;

public class CountdownHandler implements GameStateHandler {
    private final GameEngine gameEngine;

    public CountdownHandler(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    @Override
    public void handle(double currentTime, double deltaTime) {
        gameEngine.setCountdownTime(gameEngine.getCountdownTime() - deltaTime);
        if (gameEngine.getCountdownTime() <= 0) {
            gameEngine.setGameState(GameState.PLAYING);
            Settings.applyMusicVolume(gameEngine.getClip());
            gameEngine.startClip();
        }
    }
}