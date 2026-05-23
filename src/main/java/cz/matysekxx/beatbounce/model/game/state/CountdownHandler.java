package cz.matysekxx.beatbounce.model.game.state;

import cz.matysekxx.beatbounce.model.audio.AudioManager;
import cz.matysekxx.beatbounce.model.game.GameEngine;

/**
 * Handles the logic for the {@link GameState#COUNTDOWN} state.
 * Decrements the countdown timer and transitions the game to the {@link GameState#PLAYING} state
 * when the timer reaches zero, starting the music playback.
 */
public class CountdownHandler implements GameStateHandler {
    /**
     * The game engine providing state data.
     */
    private final GameEngine gameEngine;

    /**
     * Constructs a new CountdownHandler.
     *
     * @param gameEngine the game engine
     */
    public CountdownHandler(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    /**
     * Updates the countdown timer and checks for state transition.
     *
     * @param currentTime the current world time (unused in this state)
     * @param deltaTime   time since last frame in seconds
     */
    @Override
    public void handle(double currentTime, double deltaTime) {
        gameEngine.getSphere().update(currentTime, deltaTime);
        gameEngine.getCam().setZ(gameEngine.getGameZProgress() - 500);
        final double targetCamX = gameEngine.getSphere().getX() * 0.2;
        gameEngine.getCam().setX(gameEngine.getCam().getX() + (targetCamX - gameEngine.getCam().getX()) * 0.05);

        gameEngine.setCountdownTime(gameEngine.getCountdownTime() - deltaTime);
        if (gameEngine.getCountdownTime() <= 0) {
            gameEngine.setGameState(GameState.PLAYING);
            AudioManager.applyMusicVolume(gameEngine.getClip());
            gameEngine.startClip();
        }
    }
}
