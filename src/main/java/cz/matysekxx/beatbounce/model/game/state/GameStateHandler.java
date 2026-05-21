package cz.matysekxx.beatbounce.model.game.state;

/**
 * Interface for components that handle logic for a specific {@link GameState}.
 * This follows the Strategy pattern to separate logic for different game phases.
 */
public interface GameStateHandler {
    /**
     * Executes the logic for the current state.
     *
     * @param currentTime the current world time in seconds
     * @param deltaTime   the time elapsed since the last frame in seconds
     */
    void handle(double currentTime, double deltaTime);
}
