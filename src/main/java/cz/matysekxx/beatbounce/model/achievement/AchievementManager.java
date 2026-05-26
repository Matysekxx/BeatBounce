package cz.matysekxx.beatbounce.model.achievement;

import cz.matysekxx.beatbounce.model.achievement.evaluator.AchievementEvaluatorRegistry;
import cz.matysekxx.beatbounce.model.score.ScoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AchievementManager {
    private static final Logger LOG = LoggerFactory.getLogger(AchievementManager.class);

    private static final AchievementRepository repository = new AchievementRepository();
    private static final List<AchievementListener> listeners = new CopyOnWriteArrayList<>();
    private static List<Achievement> achievements = new ArrayList<>();
    private static AchievementSaveData saveData = new AchievementSaveData();
    private static boolean checking = false;

    static {
        loadData();
        checkAchievements();
    }

    public static void addListener(AchievementListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(AchievementListener listener) {
        listeners.remove(listener);
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
                    listeners.forEach(
                            listener -> listener.onAchievementUnlocked(ach)
                    );
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

    /**
     * Filters a list of achievements based on the specified filter option.
     * Supported options: "ALL", "READY TO CLAIM", "CLAIMED", "IN PROGRESS".
     */
    public static List<Achievement> filterAchievements(List<Achievement> list, String filterOption) {
        if (filterOption == null || filterOption.equalsIgnoreCase("ALL")) {
            return new ArrayList<>(list);
        }
        final List<Achievement> filtered = new ArrayList<>();
        list.forEach(ach -> {
            final boolean match = switch (filterOption.toUpperCase()) {
                case "READY TO CLAIM" -> ach.isCompleted() && !ach.isRewarded();
                case "CLAIMED" -> ach.isCompleted() && ach.isRewarded();
                case "IN PROGRESS" -> !ach.isCompleted();
                default -> true;
            };
            if (match) {
                filtered.add(ach);
            }
        });
        return filtered;
    }

    /**
     * Sorts a list of achievements based on the specified sort option.
     * Supported options: "DEFAULT", "PROGRESS", "REWARD".
     * <p>
     * To make the sorting extremely natural and user-friendly:
     * - We will sort primarily such that "READY TO CLAIM" achievements (which require action)
     * are placed at the very top.
     * - "IN PROGRESS" achievements are next.
     * - Already "CLAIMED" achievements are placed at the bottom since they are done.
     * - Secondary sorting will apply the requested sorting type ("PROGRESS" percentage descending,
     * "REWARD" value descending, or "DEFAULT" repository order).
     */
    public static List<Achievement> sortAchievements(List<Achievement> list, String sortOption) {
        final List<Achievement> sorted = new ArrayList<>(list);
        if (sortOption == null) {
            sortOption = "DEFAULT";
        }

        final String finalSort = sortOption.toUpperCase();
        sorted.sort((a, b) -> {
            switch (finalSort) {
                case "PROGRESS" -> {
                    final int cmp = Integer.compare(b.getProgressPercentage(), a.getProgressPercentage());
                    if (cmp != 0) return cmp;
                    final int pA = getPriority(a);
                    final int pB = getPriority(b);
                    if (pA != pB) return Integer.compare(pA, pB);
                }
                case "REWARD" -> {
                    final int cmp = Integer.compare(b.getReward(), a.getReward());
                    if (cmp != 0) return cmp;
                    final int pA = getPriority(a);
                    final int pB = getPriority(b);
                    if (pA != pB) return Integer.compare(pA, pB);
                }
                default -> {
                    final int pA = getPriority(a);
                    final int pB = getPriority(b);
                    if (pA != pB) return Integer.compare(pA, pB);
                    return 0;
                }
            }
            final int cmpReward = Integer.compare(b.getReward(), a.getReward());
            if (cmpReward != 0) return cmpReward;
            return a.getTitle().compareToIgnoreCase(b.getTitle());
        });
        return sorted;
    }

    private static int getPriority(Achievement ach) {
        if (ach.isCompleted() && !ach.isRewarded()) {
            return 1;
        }
        if (!ach.isCompleted()) {
            return 2;
        }
        return 3;
    }

    public interface AchievementListener {
        void onAchievementUnlocked(Achievement achievement);
    }
}
