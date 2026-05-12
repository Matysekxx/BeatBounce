package cz.matysekxx.beatbounce.model.game.state;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.game.GameEngine;
import cz.matysekxx.beatbounce.model.score.ScoreManager;
import cz.matysekxx.beatbounce.util.LevelUtil;

public class LevelEndAnimationHandler implements GameStateHandler {
    private static final double TOTAL_ANIMATION_DURATION = 3.0;

    private final GameEngine gameEngine;
    private final Camera3D cam;
    private final Sphere sphere;

    public LevelEndAnimationHandler(GameEngine gameEngine, Camera3D cam, Sphere sphere) {
        this.gameEngine = gameEngine;
        this.cam = cam;
        this.sphere = sphere;
    }

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

    private void updateAnimationTimer(double deltaTime) {
        gameEngine.setEndAnimationTimer(gameEngine.getEndAnimationTimer() - deltaTime);
    }

    private double calculateProgress() {
        return Math.min(1.0, 1.0 - (gameEngine.getEndAnimationTimer() / TOTAL_ANIMATION_DURATION));
    }

    private double easeInOutQuad(double progress) {
        return (progress < 0.5)
                ? 2 * progress * progress
                : 1 - Math.pow(-2 * progress + 2, 2) / 2;
    }

    private void updateSphereAndProgress(double deltaTime) {
        double newZProgress = gameEngine.getGameZProgress() + gameEngine.getZUnitsPerSecond() * deltaTime;
        gameEngine.setGameZProgress(newZProgress);

        sphere.setZ(newZProgress);
        sphere.setScaleMultiplier(1.0f);
        sphere.setAlpha(1.0f);
    }

    private void updateCamera(double easedProgress, double deltaTime) {
        cam.setZ(gameEngine.getGameZProgress() - (500 + easedProgress * 1500));
        cam.setY(-easedProgress * 300);
        cam.setX(cam.getX() * (1.0 - deltaTime * 2));
    }

    private void updateVisualEffects(double progress) {
        float alpha = (progress > 0.7) ? (float) ((progress - 0.7) / 0.3) : 0f;
        gameEngine.setNeonFlashAlpha(alpha);
    }

    private boolean hasAnimationFinished() {
        return gameEngine.getEndAnimationTimer() <= 0;
    }

    private void finishLevel() {
        gameEngine.setGameState(GameState.FINISHED);
        gameEngine.setNeonFlashAlpha(0f);
        cam.setY(0);
        gameEngine.stopClip();
        ScoreManager.updateScore(LevelUtil.getCleanSongName(gameEngine.getLevel()), gameEngine.getScore());
        ScoreManager.addCurrency(gameEngine.getCollectedOrbs());
    }
}