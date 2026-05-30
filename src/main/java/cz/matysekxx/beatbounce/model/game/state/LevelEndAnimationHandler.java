package cz.matysekxx.beatbounce.model.game.state;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.game.GameEngine;
import cz.matysekxx.beatbounce.model.score.ScoreManager;
import cz.matysekxx.beatbounce.util.LevelUtil;

/**
 * Handles the logic for the {@link GameState#LEVEL_END_ANIMATION} state.
 * Manages the final cutscene where the camera pulls back and the sphere flies off
 * after completing the level. Also updates the persistent user score and currency.
 *
 * @author Matysekxx
 */
public class LevelEndAnimationHandler implements GameStateHandler {
    /**
     * Total duration of the end animation sequence in seconds.
     */
    private static final double TOTAL_ANIMATION_DURATION = 3.0;

    /**
     * The game engine providing state data.
     */
    private final GameEngine gameEngine;

    /**
     * The camera used for the cinematic pull-back.
     */
    private final Camera3D cam;

    /**
     * The player character.
     */
    private final Sphere sphere;

    /**
     * Constructs a new LevelEndAnimationHandler.
     *
     * @param gameEngine the game engine
     * @param cam        the 3D camera
     * @param sphere     the player character
     */
    public LevelEndAnimationHandler(GameEngine gameEngine, Camera3D cam, Sphere sphere) {
        this.gameEngine = gameEngine;
        this.cam = cam;
        this.sphere = sphere;
    }

    /**
     * Updates the cinematic animation frame by frame.
     *
     * @param currentTime the current world time
     * @param deltaTime   time since last frame in seconds
     */
    @Override
    public void handle(double currentTime, double deltaTime) {
        updateAnimationTimer(deltaTime);

        final double progress = calculateProgress();
        final double easedProgress = easeInOutQuad(progress);

        updateSphereAndProgress(deltaTime);
        updateCamera(easedProgress, deltaTime);
        updateVisualEffects(progress);

        if (hasAnimationFinished()) {
            finishLevel();
        }
    }

    /**
     * Decrements the internal animation timer.
     */
    private void updateAnimationTimer(double deltaTime) {
        gameEngine.setEndAnimationTimer(gameEngine.getEndAnimationTimer() - deltaTime);
    }

    /**
     * Calculates the normalized progress (0.0 to 1.0) of the animation.
     */
    private double calculateProgress() {
        return Math.min(1.0, 1.0 - (gameEngine.getEndAnimationTimer() / TOTAL_ANIMATION_DURATION));
    }

    /**
     * Applies quadratic easing to the progress value for smoother motion.
     */
    private double easeInOutQuad(double progress) {
        return (progress < 0.5)
                ? 2 * progress * progress
                : 1 - Math.pow(-2 * progress + 2, 2) / 2;
    }

    /**
     * Updates the world progress and the sphere's position.
     */
    private void updateSphereAndProgress(double deltaTime) {
        double newZProgress = gameEngine.getGameZProgress() + gameEngine.getZUnitsPerSecond() * deltaTime;
        gameEngine.setGameZProgress(newZProgress);

        sphere.setZ(newZProgress);
        sphere.setScaleMultiplier(1.0f);
        sphere.setAlpha(1.0f);
    }

    /**
     * Animates the camera movement based on eased progress.
     */
    private void updateCamera(double easedProgress, double deltaTime) {
        cam.setZ(gameEngine.getGameZProgress() - (500 + easedProgress * 1500));
        cam.setY(-easedProgress * 300);
        cam.setX(cam.getX() * (1.0 - deltaTime * 2));
    }

    /**
     * Updates full-screen visual effects (e.g. fade to black).
     */
    private void updateVisualEffects(double progress) {
        float alpha = (progress > 0.7) ? (float) ((progress - 0.7) / 0.3) : 0f;
        gameEngine.setNeonFlashAlpha(alpha);
    }

    /**
     * Checks if the animation duration has elapsed.
     */
    private boolean hasAnimationFinished() {
        return gameEngine.getEndAnimationTimer() <= 0;
    }

    /**
     * Finalizes the level, stops music, and persists scores.
     */
    private void finishLevel() {
        gameEngine.setGameState(GameState.FINISHED);
        gameEngine.setNeonFlashAlpha(0f);
        cam.setY(0);
        gameEngine.stopClip();
        final String songName = LevelUtil.getCleanSongName(gameEngine.getLevel());
        gameEngine.setNewHighScore(ScoreManager.isHighScore(songName, gameEngine.getScore()));
        ScoreManager.updateScore(songName, gameEngine.getScore());
    }
}
