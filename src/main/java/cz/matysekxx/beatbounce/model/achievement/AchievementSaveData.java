package cz.matysekxx.beatbounce.model.achievement;

import java.util.ArrayList;
import java.util.List;

public class AchievementSaveData {
    private int totalPlays = 0;
    private List<String> completedIds = new ArrayList<>();
    private List<String> rewardedIds = new ArrayList<>();

    public AchievementSaveData() {
    }

    public int getTotalPlays() {
        return totalPlays;
    }

    public void setTotalPlays(int totalPlays) {
        this.totalPlays = totalPlays;
    }

    public List<String> getCompletedIds() {
        return completedIds;
    }

    public void setCompletedIds(List<String> completedIds) {
        this.completedIds = completedIds;
    }

    public List<String> getRewardedIds() {
        return rewardedIds;
    }

    public void setRewardedIds(List<String> rewardedIds) {
        this.rewardedIds = rewardedIds;
    }
}
