package cz.matysekxx.beatbounce.model.game;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.components.ScorePopup;
import cz.matysekxx.beatbounce.model.entity.*;
import cz.matysekxx.beatbounce.model.game.state.*;
import cz.matysekxx.beatbounce.model.level.Level;
import cz.matysekxx.beatbounce.model.level.LevelGenerator;
import cz.matysekxx.beatbounce.model.score.ScoreManager;
import cz.matysekxx.beatbounce.util.LevelUtil;

import javax.sound.sampled.Clip;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The core logic of the game, managing the game state, player movement, score, and level progress.
 * It acts as the central hub for gameplay mechanics and orchestrates various state handlers.
 */
public class GameEngine {
    /**
     * The level data currently being played.
     */
    private final Level level;

    /**
     * The player character (sphere).
     */
    private final Sphere sphere;

    /**
     * The 3D camera for world-to-screen projection.
     */
    private final Camera3D cam;

    /**
     * The audio clip playing the level's music.
     */
    private final Clip clip;

    /**
     * The speed at which the game progresses along the Z-axis (units per second).
     */
    private final double zUnitsPerSecond;

    /**
     * List of collectible orbs in the level.
     */
    private final List<Orb> orbs = new ArrayList<>();

    /**
     * Thread-safe list of active score popups to be rendered.
     */
    private final List<ScorePopup> scorePopups = new CopyOnWriteArrayList<>();

    /**
     * List of tiles that require periodic updates (e.g., moving or breaking tiles).
     */
    private final List<AbstractTile> updatableTiles;

    /**
     * Handler for the initial countdown state.
     */
    private final GameStateHandler countdownHandler;

    /**
     * Handler for active gameplay.
     */
    private final GameStateHandler playingHandler;

    /**
     * Handler for the level completion animation.
     */
    private final GameStateHandler levelEndAnimationHandler;

    /**
     * Handler for the falling state after a miss.
     */
    private final GameStateHandler fallingHandler;

    /**
     * Manager for handling revives and currency.
     */
    private final ReviveManager reviveManager;

    /**
     * Helper for generating orbs along the level path.
     */
    private final OrbSpawner orbSpawner;

    /**
     * The current high-level state of the game.
     */
    private volatile GameState gameState = GameState.COUNTDOWN;

    /**
     * Index of the last tile the player successfully landed on.
     */
    private int currentTileIndex = -1;

    /**
     * Current cumulative progress along the Z-axis.
     */
    private double gameZProgress;

    /**
     * Z-coordinate where the player started falling.
     */
    private double fallStartZ = 0;

    /**
     * Current player score.
     */
    private int score = 0;

    /**
     * Remaining time in the countdown phase.
     */
    private double countdownTime = 3.0;

    /**
     * Timer for the final level completion sequence.
     */
    private double endAnimationTimer = 0;

    /**
     * Transparency alpha for the neon flash effect upon landing.
     */
    private float neonFlashAlpha = 0f;

    /**
     * Count of orbs collected in the current run.
     */
    private int collectedOrbs = 0;

    /**
     * Audio timestamp corrected for lag and smoothing.
     */
    private double smoothedAudioTime = 0;

    /**
     * The nanoTime value at the last audio sync point.
     */
    private long lastSyncNano = 0;

    /**
     * The clip microsecond position at the last audio sync point.
     */
    private long lastClipMicroPos = 0;

    /**
     * Accumulator for score earned while rolling on a long tile.
     */
    private int longTileScoreAccum = 0;

    /**
     * Flag set if the user explicitly declines a revive.
     */
    private boolean reviveDeclined = false;

    /**
     * Constructs a new GameEngine.
     *
     * @param level  the level to play
     * @param sphere the player character
     * @param cam    the 3D camera
     * @param clip   the music clip
     */
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

    /**
     * Initializes or resets the engine to its starting state for a new level run.
     */
    public void init() {
        this.gameState = GameState.COUNTDOWN;
        this.countdownTime = 2.99;
        this.currentTileIndex = -1;
        this.gameZProgress = 0;
        this.fallStartZ = 0;
        this.score = 0;
        this.smoothedAudioTime = 0;
        this.longTileScoreAccum = 0;
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

    /**
     * Stops the music clip.
     */
    public void stopClip() {
        if (clip.isRunning()) clip.stop();
    }

    /**
     * Shuts down the engine and releases resources.
     */
    public void stop() {
        stopClip();
    }

    /**
     * Toggles between PLAYING and PAUSED states.
     */
    public void togglePause() {
        switch (gameState) {
            case PLAYING -> pause();
            case PAUSED -> {
                gameState = GameState.COUNTDOWN;
                countdownTime = 3.99;
            }
        }
    }

    /**
     * Transitions the game into the PAUSED state if it is currently PLAYING.
     */
    public void pause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            if (clip != null && clip.isRunning()) clip.stop();
        }
    }

    /**
     * Marks a revive as declined, skipping any further revive prompts.
     */
    public void declineRevive() {
        this.reviveDeclined = true;
    }

    /**
     * Attempts to revive the player.
     *
     * @return true if successful, false otherwise
     */
    public boolean revive() {
        return reviveManager.revive();
    }

    /**
     * Checks if the player is currently eligible for a revive.
     *
     * @return true if eligible
     */
    public boolean canRevive() {
        return reviveManager.canRevive();
    }

    /**
     * Checks if the current score is higher than the previous best for this song.
     *
     * @return true if a new high score is achieved
     */
    public boolean isNewHighScore() {
        return ScoreManager.isHighScore(LevelUtil.getCleanSongName(level), score);
    }

    /**
     * Synchronizes the internal game timer with the raw audio clip position.
     * Uses a smoothing algorithm and nanoTime interpolation to prevent jitter.
     *
     * @param deltaTime time since last frame in seconds
     */
    private void syncAudioTime(double deltaTime) {
        if (clip == null || !clip.isRunning()) return;

        final long currentNano = System.nanoTime();
        final long currentClipMicro = clip.getMicrosecondPosition();

        if (currentClipMicro != lastClipMicroPos) {
            lastClipMicroPos = currentClipMicro;
            lastSyncNano = currentNano;
        }

        final double rawPreciseTime = (lastClipMicroPos + (currentNano - lastSyncNano) / 1000.0) / 1_000_000.0;

        if (smoothedAudioTime == 0 && rawPreciseTime > 0) {
            smoothedAudioTime = rawPreciseTime;
            return;
        }

        smoothedAudioTime += deltaTime;
        final double diff = rawPreciseTime - smoothedAudioTime;
        if (Math.abs(diff) > 0.15) {
            smoothedAudioTime = rawPreciseTime;
        } else {
            final double lerpFactor = 1.0 - Math.exp(-20.0 * deltaTime);
            smoothedAudioTime += diff * lerpFactor;
        }
    }

    /**
     * Updates the game world and logic for one frame.
     *
     * @param deltaTime time since last frame in seconds
     */
    public void update(double deltaTime) {
        syncAudioTime(deltaTime);
        scorePopups.removeIf(ScorePopup::isFinished);
        scorePopups.forEach(popup -> popup.update(deltaTime));
        switch (gameState) {
            case FALLING -> fallingHandler.handle(smoothedAudioTime, deltaTime);
            case PLAYING -> playingHandler.handle(smoothedAudioTime, deltaTime);
            case COUNTDOWN -> countdownHandler.handle(smoothedAudioTime, deltaTime);
            case LEVEL_END_ANIMATION -> levelEndAnimationHandler.handle(smoothedAudioTime, deltaTime);
        }
    }

    /**
     * Returns the index of the last successfully landed tile.
     */
    public int getCurrentTileIndex() {
        return currentTileIndex;
    }

    /**
     * Sets the index of the currently active tile.
     */
    public void setCurrentTileIndex(int currentTileIndex) {
        this.currentTileIndex = currentTileIndex;
    }

    /**
     * Returns the speed of the game progress.
     */
    public double getzUnitsPerSecond() {
        return zUnitsPerSecond;
    }

    /**
     * Returns the accumulated score from long-tile rolling.
     */
    public int getLongTileScoreAccum() {
        return longTileScoreAccum;
    }

    /**
     * Sets the accumulated score from long-tile rolling.
     */
    public void setLongTileScoreAccum(int longTileScoreAccum) {
        this.longTileScoreAccum = longTileScoreAccum;
    }

    /**
     * Transitions the game into the FALLING state.
     */
    public void startFalling() {
        gameState = GameState.FALLING;
        sphere.startFalling();
        fallStartZ = sphere.getZ();
        clip.stop();
    }

    /**
     * Calculates the parameters for the next automatic jump based on the next tile position.
     *
     * @param currentTime current world time
     */
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
        final double height = 100.0;
        sphere.startJump(currentTime, duration, height);
    }

    /**
     * Returns the music clip.
     */
    public Clip getClip() {
        return clip;
    }

    /**
     * Starts the music clip.
     */
    public void startClip() {
        clip.start();
    }

    /**
     * Returns the remaining countdown time.
     */
    public double getCountdownTime() {
        return countdownTime;
    }

    /**
     * Sets the remaining countdown time.
     */
    public void setCountdownTime(double countdownTime) {
        this.countdownTime = countdownTime;
    }

    /**
     * Returns the current game state.
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Sets the current game state.
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Returns the end animation timer value.
     */
    public double getEndAnimationTimer() {
        return endAnimationTimer;
    }

    /**
     * Sets the end animation timer value.
     */
    public void setEndAnimationTimer(double endAnimationTimer) {
        this.endAnimationTimer = endAnimationTimer;
    }

    /**
     * Returns the smoothed audio time.
     */
    public double getSmoothedAudioTime() {
        return smoothedAudioTime;
    }

    /**
     * Sets the smoothed audio time.
     */
    public void setSmoothedAudioTime(double smoothedAudioTime) {
        this.smoothedAudioTime = smoothedAudioTime;
    }

    /**
     * Returns the nanoTime value at the last audio sync point.
     */
    public long getLastSyncNano() {
        return lastSyncNano;
    }

    /**
     * Sets the nanoTime value at the last audio sync point.
     */
    public void setLastSyncNano(long lastSyncNano) {
        this.lastSyncNano = lastSyncNano;
    }

    /**
     * Returns the clip microsecond position at the last audio sync point.
     */
    public long getLastClipMicroPos() {
        return lastClipMicroPos;
    }

    /**
     * Sets the clip microsecond position at the last audio sync point.
     */
    public void setLastClipMicroPos(long lastClipMicroPos) {
        this.lastClipMicroPos = lastClipMicroPos;
    }

    /**
     * Returns the total Z-axis progress.
     */
    public double getGameZProgress() {
        return gameZProgress;
    }

    /**
     * Sets the total Z-axis progress.
     */
    public void setGameZProgress(double gameZProgress) {
        this.gameZProgress = gameZProgress;
    }

    /**
     * Returns the progress speed multiplier.
     */
    public double getZUnitsPerSecond() {
        return zUnitsPerSecond;
    }

    /**
     * Returns the list of tiles that need dynamic updates.
     */
    public List<AbstractTile> getUpdatableTiles() {
        return updatableTiles;
    }

    /**
     * Returns the 3D camera.
     */
    public Camera3D getCam() {
        return cam;
    }

    /**
     * Returns the player character.
     */
    public Sphere getSphere() {
        return sphere;
    }

    /**
     * Returns the list of active orbs.
     */
    public List<Orb> getOrbs() {
        return orbs;
    }

    /**
     * Increments the count of collected orbs.
     */
    public void incrementCollectedOrbs() {
        this.collectedOrbs++;
    }

    /**
     * Returns the Z-position where falling started.
     */
    public double getFallStartZ() {
        return fallStartZ;
    }

    /**
     * Sets the Z-position where falling started.
     */
    public void setFallStartZ(double fallStartZ) {
        this.fallStartZ = fallStartZ;
    }

    /**
     * Returns the current score.
     */
    public Integer getScore() {
        return score;
    }

    /**
     * Sets the current score.
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Returns the total number of collected orbs.
     */
    public int getCollectedOrbs() {
        return collectedOrbs;
    }

    /**
     * Returns the current level.
     */
    public Level getLevel() {
        return level;
    }

    /**
     * Returns the alpha transparency of the neon flash effect.
     */
    public float getNeonFlashAlpha() {
        return neonFlashAlpha;
    }

    /**
     * Sets the alpha transparency of the neon flash effect.
     */
    public void setNeonFlashAlpha(float neonFlashAlpha) {
        this.neonFlashAlpha = neonFlashAlpha;
    }

    /**
     * Returns the total number of revives used in the current run.
     */
    public int getRevivesUsed() {
        return reviveManager.getRevivesUsed();
    }

    /**
     * Checks if the user declined a revive.
     */
    public boolean isReviveDeclined() {
        return reviveDeclined;
    }

    /**
     * Returns the list of active score popups.
     */
    public List<ScorePopup> getScorePopups() {
        return scorePopups;
    }

    /**
     * Adds a new score popup to the engine.
     *
     * @param popup the popup to add
     */
    public void addScorePopup(ScorePopup popup) {
        scorePopups.add(popup);
    }

    /**
     * Returns the cost in orbs to revive the player.
     */
    public int getReviveCost() {
        return reviveManager.getReviveCost();
    }
}
