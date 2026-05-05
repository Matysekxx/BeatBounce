package cz.matysekxx.beatbounce.model;

/**
 * Represents the various states the game can be in during its lifecycle.
 */
public enum GameState {
    /**
     * The game is in a countdown phase before gameplay starts.
     */
    COUNTDOWN,
    /**
     * The game is actively being played.
     */
    PLAYING,
    /**
     * The game is currently paused.
     */
    PAUSED,
    /**
     * The player has missed a tile and is currently falling.
     */
    FALLING,
    /**
     * The player has fallen and the game is over.
     */
    GAME_OVER,
    /**
     * The level has been completed and a final animation is playing.
     */
    LEVEL_END_ANIMATION,
    /**
     * The game has successfully finished.
     */
    FINISHED
}