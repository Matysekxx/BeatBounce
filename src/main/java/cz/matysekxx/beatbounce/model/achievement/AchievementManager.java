package cz.matysekxx.beatbounce.model.achievement;

import cz.matysekxx.beatbounce.model.achievement.evaluator.AchievementEvaluatorRegistry;
import cz.matysekxx.beatbounce.model.score.ScoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages the player's achievements, including loading, saving, and evaluating progress.
 */
public class AchievementManager {
    /**
     * Logger for the AchievementManager class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AchievementManager.class);

    /**
     * Repository for accessing achievement data.
     */
    private static final AchievementRepository repository = new AchievementRepository();

    /**
     * List of listeners to be notified when an achievement is unlocked.
     */
    private static final List<AchievementListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * List of all defined achievements.
     */
    private static List<Achievement> achievements = new ArrayList<>();

    /**
     * Persistent save data for achievements.
     */
    private static AchievementSaveData saveData = new AchievementSaveData();

    /**
     * Flag to prevent concurrent achievement checks.
     */
    private static boolean checking = false;

    static {
        loadData();
        checkAchievements();
    }

    /**
     * Adds an achievement listener.
     *
     * @param listener the listener to add
     */
    public static void addListener(AchievementListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes an achievement listener.
     *
     * @param listener the listener to remove
     */
    public static void removeListener(AchievementListener listener) {
        listeners.remove(listener);
    }

    /**
     * Loads achievement definitions and player progress from the repository.
     */
    public static void loadData() {
        achievements = repository.loadDefinitions();
        saveData = repository.loadSaveData();
        LOG.info("Loaded {} achievements. Total plays: {}", achievements.size(), saveData.getTotalPlays());
    }

    /**
     * Saves the current achievement progress to the repository.
     */
    public static synchronized void saveData() {
        repository.saveSaveData(saveData);
    }

    /**
     * Updates progress when a level is completed and triggers an achievement check.
     */
    public static synchronized void onLevelEnded() {
        saveData.setTotalPlays(saveData.getTotalPlays() + 1);
        saveData();
        checkAchievements();
    }

    /**
     * Retrieves the list of all achievements, triggering a progress check first.
     *
     * @return a list of achievements
     */
    public static List<Achievement> getAchievements() {
        checkAchievements();
        return new ArrayList<>(achievements);
    }

    /**
     * Evaluates current progress for all achievements and unlocks those that meet requirements.
     */
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

    /**
     * Resets all achievement progress and saves the empty state.
     */
    public static synchronized void reset() {
        saveData = new AchievementSaveData();
        saveData();
        checkAchievements();
    }

    /**
     * Claims the reward for a completed achievement if it hasn't been claimed yet.
     *
     * @param ach the achievement to claim the reward for
     * @return true if the reward was successfully claimed, false otherwise
     */
    public static synchronized boolean claimReward(Achievement ach) {
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
     *
     * @param list         the list of achievements to filter
     * @param filterOption the filtering criteria
     * @return the filtered list of achievements
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
     *
     * @param list       the list of achievements to sort
     * @param sortOption the sorting criteria
     * @return the sorted list of achievements
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

    /**
     * Returns a priority value for an achievement to aid in natural sorting.
     *
     * @param ach the achievement to evaluate
     * @return the priority value (lower is higher priority)
     */
    private static int getPriority(Achievement ach) {
        if (ach.isCompleted() && !ach.isRewarded()) {
            return 1;
        }
        if (!ach.isCompleted()) {
            return 2;
        }
        return 3;
    }

    /**
     * Interface for listening to achievement unlock events.
     */
    public interface AchievementListener {
        /**
         * Called when an achievement is successfully unlocked.
         *
         * @param achievement the unlocked achievement
         */
        void onAchievementUnlocked(Achievement achievement);
    }
}
