package cz.matysekxx.beatbounce.model.achievement;

import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object representing the persistent state of a player's achievements.
 * This class is serialized to JSON and encrypted for local storage.
 */
public class AchievementSaveData {
    /**
     * Total number of games the player has started.
     */
    private int totalPlays = 0;

    /**
     * List of achievement IDs that have been completed by the player.
     */
    private List<String> completedIds = new ArrayList<>();

    /**
     * List of achievement IDs for which the player has already claimed the reward.
     */
    private List<String> rewardedIds = new ArrayList<>();

    /**
     * Default constructor for Jackson.
     */
    public AchievementSaveData() {
    }

    /**
     * @return the total play count
     */
    public int getTotalPlays() {
        return totalPlays;
    }

    public void setTotalPlays(int totalPlays) {
        this.totalPlays = totalPlays;
    }

    /**
     * @return list of completed achievement IDs
     */
    public List<String> getCompletedIds() {
        return completedIds;
    }

    public void setCompletedIds(List<String> completedIds) {
        this.completedIds = completedIds;
    }

    /**
     * @return list of IDs with claimed rewards
     */
    public List<String> getRewardedIds() {
        return rewardedIds;
    }

    public void setRewardedIds(List<String> rewardedIds) {
        this.rewardedIds = rewardedIds;
    }
}
