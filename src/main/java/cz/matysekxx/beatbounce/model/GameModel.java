package cz.matysekxx.beatbounce.model;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.entity.*;
import cz.matysekxx.beatbounce.model.level.Level;
import cz.matysekxx.beatbounce.model.level.LevelGenerator;

import javax.sound.sampled.Clip;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The core logic of the game, managing the game state, player movement, score, and level progress.
 * <p>
 * Supports all tile types:
 * <ul>
 *   <li>{@link NormalTile} — standard collision, single landing.</li>
 *   <li>{@link MovingTile} — standard collision with position update.</li>
 *   <li>{@link LongTile}   — player rolls on top for its full Z-length, scoring per tick.</li>
 *   <li>{@link SmallTile}  — narrower hitbox (60 px) requiring precision.</li>
 *   <li>{@link BreakableTile} — first landing valid; second landing triggers fall.</li>
 *   <li>{@link SpeedTile}  — temporarily modifies scroll speed on first contact.</li>
 * </ul>
 */
public class GameModel {
    private static final int LANE_WIDTH = 120;
    /**
     * Normal tile hitbox half-width in world units.
     */
    private static final double NORMAL_HALF_WIDTH = LANE_WIDTH / 2.0;
    /**
     * Small tile hitbox half-width in world units.
     */
    private static final double SMALL_HALF_WIDTH = 30.0;
    /**
     * Duration the speed tile effect lasts (seconds).
     */
    private static final double SPEED_EFFECT_DURATION = 3.0;
    private final Level level;
    private final Sphere sphere;
    private final Camera3D cam;
    private final Clip clip;
    private final double zUnitsPerSecond;
    private final List<Orb> orbs = new ArrayList<>();
    private final List<AbstractTile> updatableTiles;
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
    private boolean onLongTile = false;
    private int longTileScoreAccum = 0;
    private boolean speedEffectActive = false;
    private double speedEffectTimeRemaining = 0.0;
    private double activeSpeedMultiplier = 1.0;

    /**
     * Constructs a new GameModel.
     *
     * @param level  the level to play
     * @param sphere the player's sphere
     * @param cam    the game camera
     * @param clip   the audio clip for the level
     */
    public GameModel(Level level, Sphere sphere, Camera3D cam, Clip clip) {
        this.level = level;
        this.sphere = sphere;
        this.cam = cam;
        this.clip = clip;
        this.zUnitsPerSecond = LevelGenerator.getZSpeed();
        this.updatableTiles = level.tiles()
                .stream()
                .filter(t -> t instanceof MovingTile || t instanceof BreakableTile)
                .toList();
    }

    /**
     * Initializes or resets the game state.
     */
    public void init() {
        this.gameState = GameState.COUNTDOWN;
        this.countdownTime = 2.99;
        this.currentTileIndex = -1;
        this.gameZProgress = 0;
        this.fallStartZ = 0;
        this.score = 0;
        this.smoothedAudioTime = 0;
        this.onLongTile = false;
        this.longTileScoreAccum = 0;
        this.speedEffectActive = false;
        this.speedEffectTimeRemaining = 0.0;
        this.activeSpeedMultiplier = 1.0;
        this.sphere.reset();

        cam.setX(0);
        cam.setY(0);
        cam.setZ(-500);

        orbs.clear();
        collectedOrbs = 0;

        level.tiles().forEach(AbstractTile::reset);

        spawnOrbs();
        startNextJump(0);
        clip.setFramePosition(0);
    }

    private void spawnOrbs() {
        final double totalSeconds = clip.getMicrosecondLength() / 1_000_000.0;
        final int numOrbs;
        if (totalSeconds < 30) numOrbs = 1;
        else if (totalSeconds < 60) numOrbs = 2;
        else {
            final double roll = new Random().nextDouble();
            if (roll < 0.7) numOrbs = 3;
            else if (roll < 0.9) numOrbs = 4;
            else numOrbs = 5;
        }

        final double maxOrbZ = totalSeconds * zUnitsPerSecond;
        final List<AbstractTile> validTiles = new ArrayList<>();
        for (AbstractTile t : level.tiles()) {
            if (t instanceof NormalTile && t.getZ() > 2000 && t.getZ() < maxOrbZ) {
                validTiles.add(t);
            }
        }
        final int toSpawn = Math.min(numOrbs, validTiles.size());
        if (toSpawn > 0) {
            Collections.shuffle(validTiles, new Random());
            for (int i = 0; i < toSpawn; i++) {
                final AbstractTile t = validTiles.get(i);
                orbs.add(new Orb(t.getX(), 110, t.getZ(), 20));
            }
        }
    }

    /**
     * Stops the audio clip if it is running.
     */
    public void stop() {
        if (clip.isRunning()) clip.stop();
    }

    /**
     * Toggles between paused and playing/countdown states.
     */
    public void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            clip.stop();
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.COUNTDOWN;
            countdownTime = 3.99;
        }
    }

    /**
     * @return current score
     */
    public Integer getScore() {
        return score;
    }

    /**
     * @return number of collected orbs
     */
    public int getCollectedOrbs() {
        return collectedOrbs;
    }

    /**
     * @return list of orbs in the level
     */
    public List<Orb> getOrbs() {
        return orbs;
    }

    /**
     * @return remaining countdown time
     */
    public double getCountdownTime() {
        return countdownTime;
    }

    /**
     * @return alpha for the neon flash effect
     */
    public float getNeonFlashAlpha() {
        return neonFlashAlpha;
    }

    /**
     * @return current game state
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Updates game logic based on elapsed time.
     *
     * @param currentTime current audio time in seconds
     * @param deltaTime   time since last update in seconds
     */
    public void update(double currentTime, double deltaTime) {
        switch (gameState) {
            case COUNTDOWN -> handleCountdown(deltaTime);
            case PLAYING -> {
                Settings.applyMusicVolume(clip);
                handlePlaying(deltaTime);
            }
            case LEVEL_END_ANIMATION -> handleLevelEndAnimation(deltaTime);
            case FALLING -> handleFalling(currentTime);
            case PAUSED, FINISHED, GAME_OVER -> {}
        }
    }

    private void handleCountdown(double deltaTime) {
        countdownTime -= deltaTime;
        if (countdownTime <= 0) {
            gameState = GameState.PLAYING;
            Settings.applyMusicVolume(clip);
            clip.start();
        }
    }

    private void handlePlaying(double deltaTime) {
        if (clip.getMicrosecondPosition() >= clip.getMicrosecondLength() - 50000) {
            gameState = GameState.LEVEL_END_ANIMATION;
            endAnimationTimer = 3.0;
            return;
        }
        final double rawAudioTime = clip.getMicrosecondPosition() / 1_000_000.0;
        if (smoothedAudioTime == 0 && rawAudioTime > 0) smoothedAudioTime = rawAudioTime;
        smoothedAudioTime += deltaTime;
        final double diff = rawAudioTime - smoothedAudioTime;
        smoothedAudioTime += (Math.abs(diff) > 0.05) ? diff : diff * 0.1;
        this.gameZProgress = smoothedAudioTime * zUnitsPerSecond;
        if (speedEffectActive) {
            speedEffectTimeRemaining -= deltaTime;
            if (speedEffectTimeRemaining <= 0) {
                speedEffectActive = false;
                activeSpeedMultiplier = 1.0;
            }
        }

        for (AbstractTile tile : updatableTiles) {
            switch (tile) {
                case MovingTile movingTile -> {
                    final double distance = cam.getDistanceTo(tile.getZ());
                    if (distance <= 0 || distance > 3000) continue;
                    movingTile.update(deltaTime);
                    int newX = movingTile.getX();
                    if (newX < -RenderUtils.ROAD_WIDTH) newX = -RenderUtils.ROAD_WIDTH;
                    else if (newX > RenderUtils.ROAD_WIDTH) newX = RenderUtils.ROAD_WIDTH;
                    movingTile.setLocation(newX, movingTile.getY());
                }
                case BreakableTile bt when bt.isBroken() -> bt.updateBreakAnimation(deltaTime);
                default -> {}
            }
        }
        sphere.setZ(gameZProgress);
        cam.setZ(gameZProgress - 500);
        final double targetCamX = sphere.getX() * 0.2;
        cam.setX(cam.getX() + (targetCamX - cam.getX()) * 0.05);
        handleCollisions(deltaTime);
        for (Orb orb : orbs) {
            if (!orb.isCollected()) {
                final double dz = orb.getZ() - sphere.getZ();
                final double dx = orb.getX() - sphere.getX();
                final double dy = orb.getY() - sphere.getCurrentY();
                if (dz * dz + dx * dx + dy * dy < 6400) {
                    orb.setCollected(true);
                    collectedOrbs++;
                }
            }
        }
        sphere.update(smoothedAudioTime);
    }

    /**
     * Unified collision detection for all tile types.
     */
    private void handleCollisions(double deltaTime) {
        if (currentTileIndex + 1 >= level.tiles().size()) return;
        final AbstractTile nextTile = level.tiles().get(currentTileIndex + 1);
        if (onLongTile && currentTileIndex >= 0) {
            final AbstractTile curTile = level.tiles().get(currentTileIndex);
            if (curTile instanceof LongTile lt) {
                final double timeToNextTile = (nextTile.getZ() - gameZProgress) / zUnitsPerSecond;
                final boolean shouldJumpEarly = timeToNextTile <= 0.15;
                if (gameZProgress <= lt.getZ() + lt.getLengthInZ() && !shouldJumpEarly) {
                    longTileScoreAccum++;
                    if (longTileScoreAccum % 6 == 0) score += 1;
                    return;
                } else {
                    onLongTile = false;
                    startNextJump(smoothedAudioTime);
                }
            } else {
                onLongTile = false;
            }
        }
        if (gameZProgress < nextTile.getZ()) return;
        final double halfWidth;
        if (nextTile instanceof SmallTile) {
            halfWidth = SMALL_HALF_WIDTH + sphere.getRadius();
        } else {
            halfWidth = NORMAL_HALF_WIDTH + sphere.getRadius();
        }

        final double tileMinX = nextTile.getX() - halfWidth;
        final double tileMaxX = nextTile.getX() + halfWidth;

        if (sphere.getX() < tileMinX || sphere.getX() > tileMaxX) {
            startFalling();
            return;
        }
        switch (nextTile) {
            case BreakableTile bt -> handleBreakableCollision(bt);
            case SpeedTile st -> handleSpeedTileCollision(st);
            case LongTile _ -> handleLongTileCollision();
            default -> {
                currentTileIndex++;
                score += (nextTile instanceof SmallTile) ? 15 : 10;
                startNextJump(smoothedAudioTime);
            }
        }
    }

    private void handleBreakableCollision(BreakableTile bt) {
        if (bt.isBroken()) {
            startFalling();
            return;
        }
        bt.breakTile();
        currentTileIndex++;
        score += 12;
        startNextJump(smoothedAudioTime);
    }

    private void handleSpeedTileCollision(SpeedTile st) {
        currentTileIndex++;
        score += 10;
        if (!st.isActivated()) {
            st.activate();
            speedEffectActive = true;
            speedEffectTimeRemaining = SPEED_EFFECT_DURATION;
            activeSpeedMultiplier = st.getSpeedMultiplier();
        }
        startNextJump(smoothedAudioTime);
    }

    private void handleLongTileCollision() {
        currentTileIndex++;
        onLongTile = true;
        longTileScoreAccum = 0;
        score += 5;
        sphere.cancelJump();
    }

    private void startFalling() {
        gameState = GameState.FALLING;
        sphere.startFalling();
        fallStartZ = sphere.getZ();
        clip.stop();
    }

    private void handleLevelEndAnimation(double deltaTime) {
        endAnimationTimer -= deltaTime;
        final double totalDuration = 3.0;
        final double progress = Math.min(1.0, 1.0 - (endAnimationTimer / totalDuration));
        final double eased = (progress < 0.5)
                ? 2 * progress * progress
                : 1 - Math.pow(-2 * progress + 2, 2) / 2;

        gameZProgress += zUnitsPerSecond * deltaTime;
        sphere.setZ(gameZProgress);
        sphere.setStretch(1.0f);
        sphere.setVibration(0f);
        sphere.setScaleMultiplier(1.0f);
        sphere.setAlpha(1.0f);

        cam.setZ(gameZProgress - (500 + eased * 1500));
        cam.setY(-eased * 300);
        cam.setX(cam.getX() * (1.0 - deltaTime * 2));

        neonFlashAlpha = (progress > 0.7) ? (float) ((progress - 0.7) / 0.3) : 0f;

        if (endAnimationTimer <= 0) {
            gameState = GameState.FINISHED;
            neonFlashAlpha = 0f;
            cam.setY(0);
            clip.stop();
            ScoreManager.updateScore(getCleanSongName(), score);
            ScoreManager.addCurrency(collectedOrbs);
        }
    }

    private void handleFalling(double currentTime) {
        sphere.update(currentTime);
        sphere.setZ(fallStartZ);
        cam.setZ(gameZProgress - 500);
        if (sphere.getCurrentY() > 500) {
            gameState = GameState.GAME_OVER;
            ScoreManager.updateScore(getCleanSongName(), score);
        }
    }

    private String getCleanSongName() {
        final String name = level.songName();
        final int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private void startNextJump(double currentTime) {
        final var tiles = level.tiles();
        final int nextIdx = currentTileIndex + 1;
        if (nextIdx >= tiles.size()) return;

        final AbstractTile nextTile = tiles.get(nextIdx);

        double startZ = gameZProgress;
        final double endZ = nextTile.getZ();
        double distanceZ = endZ - startZ;
        if (distanceZ < 10) distanceZ = 10;
        
        double duration = distanceZ / zUnitsPerSecond;
        if (duration <= 0) duration = 0.2;
        duration /= activeSpeedMultiplier;

        final double height = 50 + (distanceZ * 0.15);
        sphere.startJump(currentTime, duration, height);
    }
}