package cz.matysekxx.beatbounce.model.achievement.evaluator;

import cz.matysekxx.beatbounce.model.achievement.AchievementSaveData;
import cz.matysekxx.beatbounce.model.score.ScoreManager;

/**
 * Evaluator for the orbs held achievement.
 * Returns the current currency (orbs) from the ScoreManager.
 *
 * @author Matysekxx
 */
public class OrbsHeldEvaluator implements AchievementEvaluator {
    /**
     * Evaluates the current number of orbs held.
     *
     * @param saveData The achievement save data (unused in this evaluator).
     * @return The current amount of currency.
     */
    @Override
    public int evaluate(AchievementSaveData saveData) {
        return ScoreManager.getCurrency();
    }
}
