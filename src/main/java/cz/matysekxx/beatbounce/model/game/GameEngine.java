package cz.matysekxx.beatbounce.model.game;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.model.entity.*;
import cz.matysekxx.beatbounce.model.game.state.*;
import cz.matysekxx.beatbounce.model.level.Level;
import cz.matysekxx.beatbounce.model.level.LevelGenerator;
import cz.matysekxx.beatbounce.model.score.ScoreManager;

import javax.sound.sampled.Clip;
import java.util.*;

/**
 * The core logic of the game, managing the game state, player movement, score, and level progress.
 */
public class GameEngine { //TODO: vytvorit CollisionEngine pro spravu kolizi mezi hracem a dlazdicemi
    public static final int MAX_REVIVES = 3;
    private static final int LANE_WIDTH = 120;
    private static final double NORMAL_HALF_WIDTH = LANE_WIDTH / 2.0;
    private static final double SMALL_HALF_WIDTH = 30.0;
    private static final double SPEED_EFFECT_DURATION = 3.0;

    private final Level level;
    private final Sphere sphere;
    private final Camera3D cam;
    private final Clip clip;
    private final double zUnitsPerSecond;
    private final List<Orb> orbs = new ArrayList<>();
    private final List<AbstractTile> updatableTiles;
    
    private volatile GameState gameState = GameState.COUNTDOWN;
    private final EnumMap<GameState, GameStateHandler> stateHandlers = new EnumMap<>(GameState.class);

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
    private int revivesUsed = 0;
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

        stateHandlers.put(GameState.COUNTDOWN, new CountdownHandler(this));
        stateHandlers.put(GameState.PLAYING, new PlayingHandler(this, clip));
        stateHandlers.put(GameState.LEVEL_END_ANIMATION, new LevelEndAnimationHandler(this, cam, sphere));
        stateHandlers.put(GameState.FALLING, new FallingHandler(this, sphere));
    }

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
        this.revivesUsed = 0;
        this.reviveDeclined = false;
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
        if (revivesUsed >= MAX_REVIVES) return false;
        final int cost = getReviveCost();
        if (ScoreManager.getCurrency() >= cost) {
            ScoreManager.addCurrency(-cost);
            revivesUsed++;
            this.gameState = GameState.COUNTDOWN;
            this.countdownTime = 2.99;
            this.fallStartZ = 0;
            this.sphere.revive();
            final long microPos = clip.getMicrosecondPosition();
            clip.setMicrosecondPosition(Math.max(0, microPos - 1_500_000));
            this.smoothedAudioTime = clip.getMicrosecondPosition() / 1_000_000.0;
            this.gameZProgress = smoothedAudioTime * zUnitsPerSecond;

            return true;
        }
        return false;
    }

    public boolean canRevive() {
        return revivesUsed < MAX_REVIVES && ScoreManager.getCurrency() >= getReviveCost();
    }

    public boolean isNewHighScore() {
        return ScoreManager.isHighScore(getCleanSongName(), score);
    }

    public void update(double currentTime, double deltaTime) {
        if (stateHandlers.containsKey(gameState)) {
            GameStateHandler handler = stateHandlers.get(gameState);
            handler.handle(currentTime, deltaTime);
        }
    }

    public void handleCollisions(double deltaTime) {
        if (currentTileIndex + 1 >= level.tiles().size()) return;
        final AbstractTile nextTile = level.tiles().get(currentTileIndex + 1);
        if (onLongTile && currentTileIndex >= 0) {
            final AbstractTile curTile = level.tiles().get(currentTileIndex);
            if (curTile instanceof LongTile lt) {
                final double timeToNextTile = (nextTile.getZ() - gameZProgress) / zUnitsPerSecond;
                final boolean shouldJumpEarly = timeToNextTile <= 0.25;
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
        final double height = 100.0;
        sphere.startJump(currentTime, duration, height);
    }
    public String getCleanSongName() {
        final String name = level.songName();
        final int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    public Clip getClip() { return clip; }
    public void startClip() { clip.start(); }
    public double getCountdownTime() { return countdownTime; }
    public void setCountdownTime(double countdownTime) { this.countdownTime = countdownTime; }
    public GameState getGameState() { return gameState; }
    public void setGameState(GameState gameState) { this.gameState = gameState; }
    public double getEndAnimationTimer() { return endAnimationTimer; }
    public void setEndAnimationTimer(double endAnimationTimer) { this.endAnimationTimer = endAnimationTimer; }
    public double getSmoothedAudioTime() { return smoothedAudioTime; }
    public void setSmoothedAudioTime(double smoothedAudioTime) { this.smoothedAudioTime = smoothedAudioTime; }
    public double getGameZProgress() { return gameZProgress; }
    public void setGameZProgress(double gameZProgress) { this.gameZProgress = gameZProgress; }
    public double getZUnitsPerSecond() { return zUnitsPerSecond; }
    public boolean isSpeedEffectActive() { return speedEffectActive; }
    public void setSpeedEffectActive(boolean speedEffectActive) { this.speedEffectActive = speedEffectActive; }
    public double getSpeedEffectTimeRemaining() { return speedEffectTimeRemaining; }
    public void setSpeedEffectTimeRemaining(double speedEffectTimeRemaining) { this.speedEffectTimeRemaining = speedEffectTimeRemaining; }
    public void setActiveSpeedMultiplier(double activeSpeedMultiplier) { this.activeSpeedMultiplier = activeSpeedMultiplier; }
    public List<AbstractTile> getUpdatableTiles() { return updatableTiles; }
    public Camera3D getCam() { return cam; }
    public Sphere getSphere() { return sphere; }
    public List<Orb> getOrbs() { return orbs; }
    public void incrementCollectedOrbs() { this.collectedOrbs++; }
    public void setNeonFlashAlpha(float neonFlashAlpha) { this.neonFlashAlpha = neonFlashAlpha; }
    public double getFallStartZ() { return fallStartZ; }
    public Integer getScore() { return score; }
    public int getCollectedOrbs() { return collectedOrbs; }
    public Level getLevel() { return level; }
    public float getNeonFlashAlpha() { return neonFlashAlpha; }
    public int getRevivesUsed() { return revivesUsed; }
    public int getReviveCost() { return 10 * (int) Math.pow(2, revivesUsed); }
    public boolean isReviveDeclined() { return reviveDeclined; }
}