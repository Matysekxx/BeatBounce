package cz.matysekxx.beatbounce.model.achievement.evaluator;

import cz.matysekxx.beatbounce.model.achievement.AchievementSaveData;

public class TotalPlaysEvaluator implements AchievementEvaluator {
    @Override
    public int evaluate(AchievementSaveData saveData) {
        return saveData.getTotalPlays();
    }
}
