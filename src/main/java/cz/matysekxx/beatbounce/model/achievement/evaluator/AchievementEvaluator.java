package cz.matysekxx.beatbounce.model.achievement.evaluator;

import cz.matysekxx.beatbounce.model.achievement.AchievementSaveData;

public interface AchievementEvaluator {
    int evaluate(AchievementSaveData saveData);
}
