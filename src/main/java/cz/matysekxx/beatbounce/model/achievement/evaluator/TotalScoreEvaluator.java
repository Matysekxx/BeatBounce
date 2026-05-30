package cz.matysekxx.beatbounce.model.achievement.evaluator;

import cz.matysekxx.beatbounce.model.achievement.AchievementSaveData;
import cz.matysekxx.beatbounce.model.score.ScoreManager;

/**
 * Evaluator for achievements based on the cumulative score across all plays.
 * It retrieves the total score from the global ScoreManager.
 *
 * @author Matysekxx
 */
public class TotalScoreEvaluator implements AchievementEvaluator {
    /**
     * Returns the total cumulative score achieved by the player.
     *
     * @param saveData the achievement save data (unused)
     * @return the total global score
     */
    @Override
    public int evaluate(AchievementSaveData saveData) {
        return ScoreManager.getTotalScore();
    }
}
