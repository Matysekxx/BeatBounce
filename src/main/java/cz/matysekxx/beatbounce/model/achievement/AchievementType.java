package cz.matysekxx.beatbounce.model.achievement;

/**
 * Defines the various types of progress that can be tracked for achievements.
 * Each type corresponds to a specific evaluator that calculates progress from save data.
 */
public enum AchievementType {
    /**
     * Total number of game sessions started.
     */
    TOTAL_PLAYS,
    /**
     * Number of different songs played at least once.
     */
    UNIQUE_SONGS,
    /**
     * Current number of orbs held in the player's inventory.
     */
    ORBS_HELD,
    /**
     * The highest score ever achieved in a single song.
     */
    HIGH_SCORE,
    /**
     * The sum of best scores across all played songs.
     */
    TOTAL_SCORE
}
