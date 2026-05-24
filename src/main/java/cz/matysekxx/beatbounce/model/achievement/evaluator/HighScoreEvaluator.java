package cz.matysekxx.beatbounce.model.achievement.evaluator;

import cz.matysekxx.beatbounce.model.achievement.AchievementSaveData;
import cz.matysekxx.beatbounce.model.score.ScoreManager;

public class HighScoreEvaluator implements AchievementEvaluator {
    @Override
    public int evaluate(AchievementSaveData saveData) {
        return ScoreManager.getGlobalHighScore();
    }
}
