package cz.matysekxx.beatbounce.model.game;

import cz.matysekxx.beatbounce.model.entity.LongTile;
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

            final var tiles = gameEngine.getLevel().tiles();
            if (tiles.isEmpty()) return false;

            int lastTileIdx = gameEngine.getCurrentTileIndex();
            if (lastTileIdx < 0) lastTileIdx = 0;
            if (lastTileIdx >= tiles.size()) lastTileIdx = tiles.size() - 1;
            
            final var lastTile = tiles.get(lastTileIdx);
            double reviveZ = lastTile.getZ();

            if (lastTile instanceof LongTile lt) {
                final double fallZ = gameEngine.getFallStartZ();
                if (fallZ > lt.getZ()) {
                    reviveZ = Math.max(lt.getZ(), Math.min(fallZ - 150, lt.getZ() + lt.getLengthInZ() - 50));
                }
            }

            this.sphere.revive();
            gameEngine.setGameState(GameState.COUNTDOWN);
            gameEngine.setCountdownTime(3.0);
            gameEngine.setGameZProgress(reviveZ);

            final double reviveTime = reviveZ / gameEngine.getZUnitsPerSecond();
            final long microPos = (long) (reviveTime * 1_000_000);
            if (gameEngine.getClip() != null) {
                gameEngine.getClip().setMicrosecondPosition(microPos);
            }
            gameEngine.setSmoothedAudioTime(reviveTime);
            gameEngine.setLastClipMicroPos(microPos);
            gameEngine.setLastSyncNano(System.nanoTime());

            gameEngine.setFallStartZ(0);
            gameEngine.resetCCD();

            this.sphere.setZ(reviveZ);
            double tileX = lastTile.getXAt(reviveTime);
            this.sphere.setCurrentX(tileX);
            this.sphere.setCurrentY(150);
            
            gameEngine.getCam().setZ(reviveZ - 500);
            gameEngine.getCam().setX(tileX * 0.2);
            gameEngine.getCam().setY(0);

            this.sphere.update(reviveTime, 0);
            lastTile.onLanding();
            gameEngine.setCurrentTileIndex(lastTileIdx);
            
            if (lastTile instanceof LongTile) {
                gameEngine.setOnLongTile(true);
                gameEngine.setLongTileScoreAccum(0);
            } else {
                gameEngine.setOnLongTile(false);
                gameEngine.startNextJump(gameEngine.getSmoothedAudioTime());
            }
            return true;
        }
        return false;
    }

    /**
     * Calculates the cost of the next revive. Cost increases by 5 with each use.
     *
     * @return the cost in orbs
     */
    public int getReviveCost() {
        return 5 + (5 * revivesUsed);
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
