package cz.matysekxx.beatbounce.model.game;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.model.entity.*;
import cz.matysekxx.beatbounce.model.game.state.*;
import cz.matysekxx.beatbounce.model.level.Level;
import cz.matysekxx.beatbounce.model.level.LevelGenerator;
import cz.matysekxx.beatbounce.model.score.ScoreManager;
import cz.matysekxx.beatbounce.util.LevelUtil;

import javax.sound.sampled.Clip;
import java.util.ArrayList;
import java.util.List;

/**
 * The core logic of the game, managing the game state, player movement, score, and level progress.
 */
public class GameEngine {
    private final Level level;
    private final Sphere sphere;
    private final Camera3D cam;
    private final Clip clip;
    private final double zUnitsPerSecond;
    private final List<Orb> orbs = new ArrayList<>();
    private final List<AbstractTile> updatableTiles;
    private final GameStateHandler countdownHandler;
    private final GameStateHandler playingHandler;
    private final GameStateHandler levelEndAnimationHandler;
    private final GameStateHandler fallingHandler;
    private final ReviveManager reviveManager;
    private final OrbSpawner orbSpawner;
    private volatile GameState gameState = GameState.COUNTDOWN;
    private int currentTileIndex = -1;
    private double gameZProgress;
    private double fallStartZ = 0;
    private int score = 0;
    private double countdownTime = 3.0;
    private double endAnimationTimer = 0;
    private float neonFlashAlpha = 0f;
    private int collectedOrbs = 0;
    private double smoothedAudioTime = 0;
    private int longTileScoreAccum = 0;
    private boolean speedEffectActive = false;
    private double speedEffectTimeRemaining = 0.0;
    private double activeSpeedMultiplier = 1.0;
    private boolean reviveDeclined = false;

    public GameEngine(Level level, Sphere sphere, Camera3D cam, Clip clip) {
        this.level = level;
        this.sphere = sphere;
        this.cam = cam;
        this.clip = clip;
        this.zUnitsPerSecond = LevelGenerator.getZSpeed();
        this.updatableTiles = level.tiles()
                .stream()
                .filter(t -> t instanceof MovingTile || t instanceof BreakableTile)
                .toList();

        this.reviveManager = new ReviveManager(this, sphere);
        this.orbSpawner = new OrbSpawner();
        this.countdownHandler = new CountdownHandler(this);
        this.playingHandler = new PlayingHandler(this, clip, new TileManager(this));
        this.levelEndAnimationHandler = new LevelEndAnimationHandler(this, cam, sphere);
        this.fallingHandler = new FallingHandler(this, sphere);
    }

    public void init() {
        this.gameState = GameState.COUNTDOWN;
        this.countdownTime = 2.99;
        this.currentTileIndex = -1;
        this.gameZProgress = 0;
        this.fallStartZ = 0;
        this.score = 0;
        this.smoothedAudioTime = 0;
        this.longTileScoreAccum = 0;
        this.speedEffectActive = false;
        this.speedEffectTimeRemaining = 0.0;
        this.activeSpeedMultiplier = 1.0;
        this.reviveManager.setRevivesUsed(0);
        this.reviveDeclined = false;
        this.sphere.reset();

        cam.setX(0);
        cam.setY(0);
        cam.setZ(-500);

        orbs.clear();
        collectedOrbs = 0;

        level.tiles().forEach(AbstractTile::reset);

        orbSpawner.spawnOrbs(level, clip, zUnitsPerSecond, orbs);
        startNextJump(0);
        clip.setFramePosition(0);
    }

    public void stopClip() {
        if (clip.isRunning()) clip.stop();
    }

    public void stop() {
        stopClip();
    }

    public void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            clip.stop();
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.COUNTDOWN;
            countdownTime = 3.99;
        }
    }

    public void declineRevive() {
        this.reviveDeclined = true;
    }

    public boolean revive() {
        return reviveManager.revive();
    }

    public boolean canRevive() {
        return reviveManager.canRevive();
    }

    public boolean isNewHighScore() {
        return ScoreManager.isHighScore(LevelUtil.getCleanSongName(level), score);
    }

    public void update(double currentTime, double deltaTime) {
        switch (gameState) {
            case FALLING -> fallingHandler.handle(currentTime, deltaTime);
            case PLAYING -> playingHandler.handle(currentTime, deltaTime);
            case COUNTDOWN -> countdownHandler.handle(currentTime, deltaTime);
            case LEVEL_END_ANIMATION -> levelEndAnimationHandler.handle(currentTime, deltaTime);
        }
    }

    public int getCurrentTileIndex() {
        return currentTileIndex;
    }

    public void setCurrentTileIndex(int currentTileIndex) {
        this.currentTileIndex = currentTileIndex;
    }

    public double getzUnitsPerSecond() {
        return zUnitsPerSecond;
    }

    public int getLongTileScoreAccum() {
        return longTileScoreAccum;
    }

    public void setLongTileScoreAccum(int longTileScoreAccum) {
        this.longTileScoreAccum = longTileScoreAccum;
    }

    public void startFalling() {
        gameState = GameState.FALLING;
        sphere.startFalling();
        fallStartZ = sphere.getZ();
        clip.stop();
    }

    public void startNextJump(double currentTime) {
        final var tiles = level.tiles();
        final int nextIdx = currentTileIndex + 1;
        if (nextIdx >= tiles.size()) return;
        final AbstractTile nextTile = tiles.get(nextIdx);

        final double endZ = nextTile.getZ();
        double distanceZ = endZ - gameZProgress;
        if (distanceZ < 10) distanceZ = 10;

        double duration = distanceZ / zUnitsPerSecond;
        if (duration <= 0) duration = 0.2;
        duration /= activeSpeedMultiplier;
        final double height = 100.0;
        sphere.startJump(currentTime, duration, height);
    }

    public Clip getClip() {
        return clip;
    }

    public void startClip() {
        clip.start();
    }

    public double getCountdownTime() {
        return countdownTime;
    }

    public void setCountdownTime(double countdownTime) {
        this.countdownTime = countdownTime;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public double getEndAnimationTimer() {
        return endAnimationTimer;
    }

    public void setEndAnimationTimer(double endAnimationTimer) {
        this.endAnimationTimer = endAnimationTimer;
    }

    public double getSmoothedAudioTime() {
        return smoothedAudioTime;
    }

    public void setSmoothedAudioTime(double smoothedAudioTime) {
        this.smoothedAudioTime = smoothedAudioTime;
    }

    public double getGameZProgress() {
        return gameZProgress;
    }

    public void setGameZProgress(double gameZProgress) {
        this.gameZProgress = gameZProgress;
    }

    public double getZUnitsPerSecond() {
        return zUnitsPerSecond;
    }

    public boolean isSpeedEffectActive() {
        return speedEffectActive;
    }

    public void setSpeedEffectActive(boolean speedEffectActive) {
        this.speedEffectActive = speedEffectActive;
    }

    public double getSpeedEffectTimeRemaining() {
        return speedEffectTimeRemaining;
    }

    public void setSpeedEffectTimeRemaining(double speedEffectTimeRemaining) {
        this.speedEffectTimeRemaining = speedEffectTimeRemaining;
    }

    public void setActiveSpeedMultiplier(double activeSpeedMultiplier) {
        this.activeSpeedMultiplier = activeSpeedMultiplier;
    }

    public List<AbstractTile> getUpdatableTiles() {
        return updatableTiles;
    }

    public Camera3D getCam() {
        return cam;
    }

    public Sphere getSphere() {
        return sphere;
    }

    public List<Orb> getOrbs() {
        return orbs;
    }

    public void incrementCollectedOrbs() {
        this.collectedOrbs++;
    }

    public double getFallStartZ() {
        return fallStartZ;
    }

    public void setFallStartZ(double fallStartZ) {
        this.fallStartZ = fallStartZ;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getCollectedOrbs() {
        return collectedOrbs;
    }

    public Level getLevel() {
        return level;
    }

    public float getNeonFlashAlpha() {
        return neonFlashAlpha;
    }

    public void setNeonFlashAlpha(float neonFlashAlpha) {
        this.neonFlashAlpha = neonFlashAlpha;
    }

    public int getRevivesUsed() {
        return reviveManager.getRevivesUsed();
    }

    public boolean isReviveDeclined() {
        return reviveDeclined;
    }

    public int getReviveCost() {
        return reviveManager.getReviveCost();
    }
}