package cz.matysekxx.beatbounce.model.achievement;

import cz.matysekxx.beatbounce.model.achievement.evaluator.AchievementEvaluatorRegistry;
import cz.matysekxx.beatbounce.model.score.ScoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AchievementManager {
    private static final Logger LOG = LoggerFactory.getLogger(AchievementManager.class);

    private static final AchievementRepository repository = new AchievementRepository();
    private static List<Achievement> achievements = new ArrayList<>();
    private static AchievementSaveData saveData = new AchievementSaveData();

    private static boolean checking = false;

    static {
        loadData();
        checkAchievements();
    }

    public static void loadData() {
        achievements = repository.loadDefinitions();
        saveData = repository.loadSaveData();
        LOG.info("Loaded {} achievements. Total plays: {}", achievements.size(), saveData.getTotalPlays());
    }

    public static synchronized void saveData() {
        repository.saveSaveData(saveData);
    }

    public static void onLevelEnded() {
        saveData.setTotalPlays(saveData.getTotalPlays() + 1);
        saveData();
        checkAchievements();
    }

    public static List<Achievement> getAchievements() {
        checkAchievements();
        return new ArrayList<>(achievements);
    }

    public static synchronized void checkAchievements() {
        if (checking) return;
        checking = true;

        try {
            boolean stateChanged = false;

            for (Achievement ach : achievements) {
                int progress = AchievementEvaluatorRegistry.getEvaluator(ach.getType()).evaluate(saveData);
                ach.setCurrentProgress(progress);

                final boolean isCompleted = progress >= ach.getTarget();
                ach.setCompleted(isCompleted || saveData.getCompletedIds().contains(ach.getId()));
                ach.setRewarded(saveData.getRewardedIds().contains(ach.getId()));

                if (isCompleted && !saveData.getCompletedIds().contains(ach.getId())) {
                    saveData.getCompletedIds().add(ach.getId());
                    stateChanged = true;
                    LOG.info("Unlocked achievement: {} - {}", ach.getId(), ach.getTitle());
                    ach.setCompleted(true);
                }
            }
            if (stateChanged) saveData();
        } finally {
            checking = false;
        }
    }

    public static void reset() {
        saveData = new AchievementSaveData();
        saveData();
        checkAchievements();
    }

    public static boolean claimReward(Achievement ach) {
        if (ach.isCompleted() && !saveData.getRewardedIds().contains(ach.getId())) {
            ach.setRewarded(true);
            saveData.getRewardedIds().add(ach.getId());
            ScoreManager.addCurrency(ach.getReward());
            saveData();
            LOG.info("Manually claimed {} orbs for achievement: {}", ach.getReward(), ach.getTitle());
            return true;
        }
        return false;
    }
}
