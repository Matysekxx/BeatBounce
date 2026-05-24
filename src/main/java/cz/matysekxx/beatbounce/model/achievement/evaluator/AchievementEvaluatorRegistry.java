package cz.matysekxx.beatbounce.model.achievement.evaluator;

import cz.matysekxx.beatbounce.model.achievement.AchievementType;

import java.util.EnumMap;
import java.util.Map;

public class AchievementEvaluatorRegistry {
    private static final Map<AchievementType, AchievementEvaluator> evaluators = new EnumMap<>(AchievementType.class);

    static {
        evaluators.put(AchievementType.TOTAL_PLAYS, new TotalPlaysEvaluator());
        evaluators.put(AchievementType.UNIQUE_SONGS, new UniqueSongsEvaluator());
        evaluators.put(AchievementType.ORBS_HELD, new OrbsHeldEvaluator());
        evaluators.put(AchievementType.HIGH_SCORE, new HighScoreEvaluator());
    }

    public static AchievementEvaluator getEvaluator(AchievementType type) {
        return evaluators.getOrDefault(type, saveData -> 0);
    }
}
