package cz.matysekxx.beatbounce.model.achievement.evaluator;

import cz.matysekxx.beatbounce.model.achievement.AchievementSaveData;

/**
 * Evaluator for achievements based on the total number of games played.
 * It retrieves the play count directly from the achievement save data.
 *
 * @author Matysekxx
 */
public class TotalPlaysEvaluator implements AchievementEvaluator {
    /**
     * Returns the total number of plays recorded in the save data.
     *
     * @param saveData the persistent achievement save data
     * @return the total number of games played
     */
    @Override
    public int evaluate(AchievementSaveData saveData) {
        return saveData.getTotalPlays();
    }
}
