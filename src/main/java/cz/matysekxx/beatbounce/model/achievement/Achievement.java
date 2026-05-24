package cz.matysekxx.beatbounce.model.achievement;

public class Achievement {
    private String id;
    private String title;
    private String description;
    private AchievementType type;
    private int target;
    private int reward;

    private boolean completed;
    private boolean rewarded;
    private int currentProgress;

    public Achievement() {
    }

    public Achievement(String id, String title, String description, AchievementType type, int target, int reward) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.target = target;
        this.reward = reward;
        this.completed = false;
        this.rewarded = false;
        this.currentProgress = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AchievementType getType() {
        return type;
    }

    public void setType(AchievementType type) {
        this.type = type;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isRewarded() {
        return rewarded;
    }

    public void setRewarded(boolean rewarded) {
        this.rewarded = rewarded;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    public int getProgressPercentage() {
        if (target <= 0) return 0;
        return Math.clamp((int) (((double) currentProgress / target) * 100), 0, 100);
    }
}
