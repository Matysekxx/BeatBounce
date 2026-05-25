package cz.matysekxx.beatbounce.model.achievement.evaluator;

import cz.matysekxx.beatbounce.model.achievement.AchievementSaveData;
import cz.matysekxx.beatbounce.model.score.ScoreManager;

public class TotalScoreEvaluator implements AchievementEvaluator {
    @Override
    public int evaluate(AchievementSaveData saveData) {
        return ScoreManager.getTotalScore();
    }
}
