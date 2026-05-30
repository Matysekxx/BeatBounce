package cz.matysekxx.beatbounce.model.achievement.evaluator;

import cz.matysekxx.beatbounce.model.achievement.AchievementSaveData;

/**
 * Interface for achievement progress evaluators.
 * Each implementation defines how to calculate the current progress value
 * for a specific achievement type.
 *
 * @author Matysekxx
 */
public interface AchievementEvaluator {
    /**
     * Evaluates the current progress for an achievement.
     *
     * @param saveData the persistent achievement save data
     * @return the calculated progress value as an integer
     */
    int evaluate(AchievementSaveData saveData);
}
