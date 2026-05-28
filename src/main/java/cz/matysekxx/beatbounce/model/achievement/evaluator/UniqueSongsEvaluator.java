package cz.matysekxx.beatbounce.model.achievement.evaluator;

import cz.matysekxx.beatbounce.model.achievement.AchievementSaveData;
import cz.matysekxx.beatbounce.model.score.ScoreManager;

/**
 * Evaluator for achievements based on the number of unique songs played.
 * It tracks how many different tracks the player has attempted.
 */
public class UniqueSongsEvaluator implements AchievementEvaluator {
    /**
     * Returns the count of unique songs that have been played.
     *
     * @param saveData the achievement save data (unused)
     * @return the number of unique songs played
     */
    @Override
    public int evaluate(AchievementSaveData saveData) {
        return ScoreManager.getSongsPlayedCount();
    }
}
