package cz.matysekxx.beatbounce.model.achievement;

/**
 * Represents a single achievement in the game.
 * It contains metadata like title, description, and requirements,
 * as well as the player's current progress and completion status.
 *
 * @author Matysekxx
 */
public class Achievement {
    /**
     * Unique identifier for the achievement.
     */
    private String id;

    /**
     * The display title of the achievement.
     */
    private String title;

    /**
     * A brief description of how to earn the achievement.
     */
    private String description;

    /**
     * The category or type of progress this achievement tracks.
     */
    private AchievementType type;

    /**
     * The numerical value required to complete the achievement.
     */
    private int target;

    /**
     * The reward in orbs given upon completion and claiming.
     */
    private int reward;

    /**
     * Whether the achievement has been completed (progress >= target).
     */
    private boolean completed;

    /**
     * Whether the reward for this achievement has been claimed.
     */
    private boolean rewarded;

    /**
     * The current numerical progress toward the target.
     */
    private int currentProgress;

    /**
     * Default constructor for Jackson JSON deserialization.
     */
    public Achievement() {
    }

    /**
     * Constructs a new Achievement with the specified parameters.
     *
     * @param id          unique identifier
     * @param title       display title
     * @param description how to earn it
     * @param type        tracking category
     * @param target      completion requirement
     * @param reward      orb reward
     */
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

    /**
     * @return the unique identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier for the achievement.
     *
     * @param id the new unique identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the display title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the display title of the achievement.
     *
     * @param title the new display title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the earning description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of how to earn the achievement.
     *
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the achievement category
     */
    public AchievementType getType() {
        return type;
    }

    /**
     * Sets the category or type of progress this achievement tracks.
     *
     * @param type the new achievement type
     */
    public void setType(AchievementType type) {
        this.type = type;
    }

    /**
     * @return the target requirement value
     */
    public int getTarget() {
        return target;
    }

    /**
     * Sets the numerical value required to complete the achievement.
     *
     * @param target the new target value
     */
    public void setTarget(int target) {
        this.target = target;
    }

    /**
     * @return the orb reward amount
     */
    public int getReward() {
        return reward;
    }

    /**
     * Sets the reward in orbs given upon completion and claiming.
     *
     * @param reward the new reward amount
     */
    public void setReward(int reward) {
        this.reward = reward;
    }

    /**
     * @return true if requirements are met
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Sets the completion status of the achievement.
     *
     * @param completed true if requirements are met, false otherwise
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * @return true if reward has been claimed
     */
    public boolean isRewarded() {
        return rewarded;
    }

    /**
     * Sets the reward claim status of the achievement.
     *
     * @param rewarded true if the reward has been claimed, false otherwise
     */
    public void setRewarded(boolean rewarded) {
        this.rewarded = rewarded;
    }

    /**
     * @return the current progress value
     */
    public int getCurrentProgress() {
        return currentProgress;
    }

    /**
     * Sets the current numerical progress toward the target.
     *
     * @param currentProgress the new current progress value
     */
    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    /**
     * Calculates the completion percentage (0-100).
     *
     * @return integer percentage
     */
    public int getProgressPercentage() {
        if (completed) return 100;
        if (target <= 0) return 0;
        return Math.clamp((int) (((double) currentProgress / target) * 100), 0, 100);
    }
}
