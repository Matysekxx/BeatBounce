package cz.matysekxx.beatbounce.model.achievement.evaluator;

import cz.matysekxx.beatbounce.model.achievement.AchievementType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Registry for achievement evaluators.
 * Maps AchievementType to its corresponding AchievementEvaluator.
 *
 * @author Matysekxx
 */
public class AchievementEvaluatorRegistry {
    /**
     * Map storing the evaluators for each AchievementType.
     */
    private static final Map<AchievementType, AchievementEvaluator> evaluators = new EnumMap<>(AchievementType.class);

    static {
        evaluators.put(AchievementType.TOTAL_PLAYS, new TotalPlaysEvaluator());
        evaluators.put(AchievementType.UNIQUE_SONGS, new UniqueSongsEvaluator());
        evaluators.put(AchievementType.ORBS_HELD, new OrbsHeldEvaluator());
        evaluators.put(AchievementType.HIGH_SCORE, new HighScoreEvaluator());
        evaluators.put(AchievementType.TOTAL_SCORE, new TotalScoreEvaluator());
    }

    /**
     * Retrieves the evaluator for the specified AchievementType.
     *
     * @param type The AchievementType to get the evaluator for.
     * @return The AchievementEvaluator for the type, or a default evaluator returning 0 if not found.
     */
    public static AchievementEvaluator getEvaluator(AchievementType type) {
        return evaluators.getOrDefault(type, saveData -> 0);
    }
}
