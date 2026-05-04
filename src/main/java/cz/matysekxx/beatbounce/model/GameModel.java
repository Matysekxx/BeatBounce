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

public class GameModel {
    private static final int LANE_WIDTH = 120;

    private final Level level;
    private final Sphere sphere;
    private final Camera3D cam;
    private final Clip clip;
    private final double zUnitsPerSecond;
    private final List<Orb> orbs = new ArrayList<>();
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

    public GameModel(Level level, Sphere sphere, Camera3D cam, Clip clip) {
        this.level = level;
        this.sphere = sphere;
        this.cam = cam;
        this.clip = clip;
        this.zUnitsPerSecond = LevelGenerator.getZSpeed();
    }

    public void init() {
        this.gameState = GameState.COUNTDOWN;
        this.countdownTime = 2.99;
        this.currentTileIndex = -1;
        this.gameZProgress = 0;
        this.fallStartZ = 0;
        this.score = 0;
        this.smoothedAudioTime = 0;
        this.sphere.reset();

        cam.setX(0);
        cam.setY(0);
        cam.setZ(-500);

        orbs.clear();
        collectedOrbs = 0;

        final double totalSeconds = clip.getMicrosecondLength() / 1_000_000.0;
        final int numOrbs;
        if (totalSeconds < 30) {
            numOrbs = 1;
        } else if (totalSeconds < 60) {
            numOrbs = 2;
        } else {
            final double roll = new Random().nextDouble();
            if (roll < 0.7) numOrbs = 3;
            else if (roll < 0.9) numOrbs = 4;
            else numOrbs = 5;
        }

        final double maxOrbZ = clip.getMicrosecondLength() / 1_000_000.0 * zUnitsPerSecond;
        final List<AbstractTile> validTiles = new ArrayList<>();
        for (AbstractTile t : level.tiles()) {
            if (t instanceof NormalTile && t.getZ() > 2000 && t.getZ() < maxOrbZ) {
                validTiles.add(t);
            }
        }

        int toSpawn = Math.min(numOrbs, validTiles.size());
        if (toSpawn > 0) {
            Collections.shuffle(validTiles, new Random());
            for (int i = 0; i < toSpawn; i++) {
                AbstractTile t = validTiles.get(i);
                orbs.add(new Orb(t.getX(), 110, t.getZ(), 20));
            }
        }

        startNextJump(0);
        clip.setFramePosition(0);
    }

    public void stop() {
        if (clip.isRunning()) {
            clip.stop();
        }
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

    public Integer getScore() {
        return score;
    }

    public int getCollectedOrbs() {
        return collectedOrbs;
    }

    public List<Orb> getOrbs() {
        return orbs;
    }

    public double getCountdownTime() {
        return countdownTime;
    }

    public float getNeonFlashAlpha() {
        return neonFlashAlpha;
    }

    public void update(double currentTime, double deltaTime) {
        switch (gameState) {
            case COUNTDOWN -> handleCountdown(deltaTime);
            case PLAYING -> {
                Settings.applyMusicVolume(clip);
                handlePlaying(deltaTime);
            }
            case LEVEL_END_ANIMATION -> handleLevelEndAnimation(deltaTime);
            case FALLING -> handleFalling(currentTime);
            case PAUSED, FINISHED, GAME_OVER -> {
            }
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

        if (smoothedAudioTime == 0 && rawAudioTime > 0) {
            smoothedAudioTime = rawAudioTime;
        }

        smoothedAudioTime += deltaTime;
        final double diff = rawAudioTime - smoothedAudioTime;
        if (Math.abs(diff) > 0.05) {
            smoothedAudioTime = rawAudioTime;
        } else {
            smoothedAudioTime += diff * 0.1;
        }

        this.gameZProgress = smoothedAudioTime * zUnitsPerSecond;

        for (AbstractTile tile : level.tiles()) {
            if (tile instanceof MovingTile movingTile) {
                final double distance = cam.getDistanceTo(tile.getZ());
                final double tileDepth = distance + tile.getLengthInZ();
                if (tileDepth <= 0 || distance > 3000) continue;
                movingTile.update(deltaTime);
                int newX = movingTile.getX();
                if (newX < -RenderUtils.ROAD_WIDTH) {
                    newX = -RenderUtils.ROAD_WIDTH;
                } else if (newX > RenderUtils.ROAD_WIDTH) {
                    newX = RenderUtils.ROAD_WIDTH;
                }
                movingTile.setLocation(newX, movingTile.getY());
            }
        }

        sphere.setZ(gameZProgress);
        cam.setZ(gameZProgress - 500);
        final double targetCamX = sphere.getX() * 0.2;
        cam.setX(cam.getX() + (targetCamX - cam.getX()) * 0.05);

        if (currentTileIndex + 1 < level.tiles().size()) {
            final AbstractTile nextTile = level.tiles().get(currentTileIndex + 1);
            if (gameZProgress >= nextTile.getZ()) {
                final double tileMinX = nextTile.getX() - (LANE_WIDTH / 2.0) - sphere.getRadius();
                final double tileMaxX = nextTile.getX() + (LANE_WIDTH / 2.0) + sphere.getRadius();

                if (sphere.getX() >= tileMinX && sphere.getX() <= tileMaxX) {
                    currentTileIndex++;
                    score += 10;
                    startNextJump(smoothedAudioTime);
                } else {
                    gameState = GameState.FALLING;
                    sphere.startFalling();
                    fallStartZ = sphere.getZ();
                    clip.stop();
                }
            }
        }

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

    private void handleLevelEndAnimation(double deltaTime) {
        endAnimationTimer -= deltaTime;
        final double totalDuration = 3.0;
        final double progress = Math.min(1.0, 1.0 - (endAnimationTimer / totalDuration));
        final double eased;
        if (progress < 0.5) eased = 2 * progress * progress;
        else eased = 1 - Math.pow(-2 * progress + 2, 2) / 2;

        gameZProgress += zUnitsPerSecond * deltaTime;
        sphere.setZ(gameZProgress);
        sphere.setStretch(1.0f);
        sphere.setVibration(0f);
        sphere.setScaleMultiplier(1.0f);
        sphere.setAlpha(1.0f);

        final double cameraLag = 500 + eased * 1500;
        cam.setZ(gameZProgress - cameraLag);
        cam.setY(-eased * 300);
        cam.setX(cam.getX() * (1.0 - deltaTime * 2));

        if (progress > 0.7) {
            neonFlashAlpha = (float) ((progress - 0.7) / 0.3);
        } else {
            neonFlashAlpha = 0f;
        }

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
        if (dot > 0) return name.substring(0, dot);
        return name;
    }

    private void startNextJump(double currentTime) {
        final var tiles = level.tiles();
        final int nextIndex = currentTileIndex + 1;
        if (nextIndex >= tiles.size()) return;

        AbstractTile currentTile = null;
        if (currentTileIndex >= 0) {
            currentTile = tiles.get(currentTileIndex);
        }

        final AbstractTile nextTile = tiles.get(nextIndex);

        double startZ = currentTile != null ? currentTile.getZ() : 0;
        final double endZ = nextTile.getZ();
        final double distanceZ = endZ - startZ;
        double duration = distanceZ / zUnitsPerSecond;
        if (duration <= 0) duration = 0.2;

        final double height = 50 + (distanceZ * 0.15);
        sphere.startJump(currentTime, duration, height);
    }

    public GameState getGameState() {
        return gameState;
    }
}