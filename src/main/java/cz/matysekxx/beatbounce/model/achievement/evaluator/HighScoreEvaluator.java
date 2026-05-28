package cz.matysekxx.beatbounce.model.achievement.evaluator;

import cz.matysekxx.beatbounce.model.achievement.AchievementSaveData;
import cz.matysekxx.beatbounce.model.score.ScoreManager;

/**
 * Evaluator for the high score achievement.
 * Returns the global high score from the ScoreManager.
 */
public class HighScoreEvaluator implements AchievementEvaluator {
    /**
     * Evaluates the current high score.
     *
     * @param saveData The achievement save data (unused in this evaluator).
     * @return The current global high score.
     */
    @Override
    public int evaluate(AchievementSaveData saveData) {
        return ScoreManager.getGlobalHighScore();
    }
}
