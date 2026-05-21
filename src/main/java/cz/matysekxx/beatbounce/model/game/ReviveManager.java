package cz.matysekxx.beatbounce.model.game;

import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.game.state.GameState;
import cz.matysekxx.beatbounce.model.score.ScoreManager;

/**
 * Manages the logic for reviving the player after a fall.
 * Handles cost calculation, currency deduction, and game state restoration.
 */
public class ReviveManager {
    /**
     * Maximum number of times a player can revive in a single run.
     */
    public static final int MAX_REVIVES = 3;

    /**
     * The game engine providing state and clip data.
     */
    private final GameEngine gameEngine;

    /**
     * The player character to be revived.
     */
    private final Sphere sphere;

    /**
     * Counter for how many revives have been used in the current run.
     */
    private int revivesUsed = 0;

    /**
     * Constructs a new ReviveManager.
     *
     * @param gameEngine the game engine
     * @param sphere     the player character
     */
    public ReviveManager(GameEngine gameEngine, Sphere sphere) {
        this.gameEngine = gameEngine;
        this.sphere = sphere;
    }

    /**
     * Attempts to revive the player by deducting currency and resetting state.
     *
     * @return true if revival was successful, false otherwise
     */
    public boolean revive() {
        if (revivesUsed >= MAX_REVIVES) return false;
        final int cost = getReviveCost();
        if (ScoreManager.getCurrency() >= cost) {
            ScoreManager.addCurrency(-cost);
            revivesUsed++;
            gameEngine.setGameState(GameState.COUNTDOWN);
            gameEngine.setCountdownTime(2.99);
            gameEngine.setFallStartZ(0);
            this.sphere.revive();
            final long microPos = gameEngine.getClip().getMicrosecondPosition();
            gameEngine.getClip().setMicrosecondPosition(Math.max(0, microPos - 1_500_000));
            gameEngine.setSmoothedAudioTime(gameEngine.getClip().getMicrosecondPosition() / 1_000_000.0);
            gameEngine.setGameZProgress(gameEngine.getSmoothedAudioTime() * gameEngine.getZUnitsPerSecond());
            return true;
        }
        return false;
    }

    /**
     * Calculates the cost of the next revive. Cost doubles with each use.
     *
     * @return the cost in orbs
     */
    public int getReviveCost() {
        return 10 * (int) Math.pow(2, revivesUsed);
    }

    /**
     * Checks if the player has enough currency and haven't exceeded the revive limit.
     *
     * @return true if eligible for revive
     */
    public boolean canRevive() {
        return revivesUsed < MAX_REVIVES && ScoreManager.getCurrency() >= getReviveCost();
    }

    /**
     * Returns the count of revives used.
     */
    public int getRevivesUsed() {
        return revivesUsed;
    }

    /**
     * Sets the count of revives used.
     */
    public void setRevivesUsed(int revivesUsed) {
        this.revivesUsed = revivesUsed;
    }
}
