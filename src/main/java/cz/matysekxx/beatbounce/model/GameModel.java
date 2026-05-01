package cz.matysekxx.beatbounce.model;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.MovingTile;
import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.level.Level;
import cz.matysekxx.beatbounce.model.level.LevelGenerator;

import javax.sound.sampled.Clip;

public class GameModel {
    private static final int LANE_WIDTH = 120;

    private final Level level;
    private final Sphere sphere;
    private final Camera3D cam;
    private final Clip clip;
    private volatile GameState gameState = GameState.COUNTDOWN;
    private int currentTileIndex = -1;
    private double gameZProgress;
    private double fallStartZ = 0;
    private int score = 0;
    private double countdownTime = 3.0;
    
    private final double zUnitsPerSecond;

    public GameModel(Level level, Sphere sphere, Camera3D cam, Clip clip) {
        this.level = level;
        this.sphere = sphere;
        this.cam = cam;
        this.clip = clip;
        this.zUnitsPerSecond = LevelGenerator.getZSpeed(level.stars() > 0 ? level.stars() : 1);
    }

    public void init() {
        this.gameState = GameState.COUNTDOWN;
        this.countdownTime = 3.99;
        this.currentTileIndex = -1;
        this.gameZProgress = 0;
        this.fallStartZ = 0;
        this.score = 0;
        this.sphere.reset();

        cam.setX(0);
        cam.setY(0);
        cam.setZ(-500);

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
    
    public double getCountdownTime() {
        return countdownTime;
    }

    public void update(double currentTime, double deltaTime) {
        switch (gameState) {
            case COUNTDOWN -> handleCountdown(deltaTime);
            case PLAYING -> handlePlaying(currentTime, deltaTime);
            case FALLING -> handleFalling(currentTime);
            case PAUSED, FINISHED, GAME_OVER -> {
            }
        }
    }
    
    private void handleCountdown(double deltaTime) {
        countdownTime -= deltaTime;
        if (countdownTime <= 0) {
            gameState = GameState.PLAYING;
            clip.start();
        }
    }

    private void handlePlaying(double currentTime, double deltaTime) {
        if (clip.getMicrosecondPosition() >= clip.getMicrosecondLength() - 50000) {
            gameState = GameState.FINISHED;
            clip.stop();
            ScoreManager.updateScore(level.songName(), score);
            return;
        }

        this.gameZProgress = currentTime * zUnitsPerSecond;

        for (AbstractTile tile : level.tiles()) {
            if (tile instanceof MovingTile movingTile) {
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
        double targetCamX = sphere.getX() * 0.2;
        cam.setX(cam.getX() + (targetCamX - cam.getX()) * 0.05);

        if (currentTileIndex + 1 < level.tiles().size()) {
            final AbstractTile nextTile = level.tiles().get(currentTileIndex + 1);
            if (gameZProgress >= nextTile.getZ()) {
                final double tileMinX = nextTile.getX() - (LANE_WIDTH / 2.0) - sphere.getRadius();
                final double tileMaxX = nextTile.getX() + (LANE_WIDTH / 2.0) + sphere.getRadius();

                if (sphere.getX() >= tileMinX && sphere.getX() <= tileMaxX) {
                    currentTileIndex++;
                    score += 10;
                    startNextJump(currentTime);
                } else {
                    gameState = GameState.FALLING;
                    sphere.startFalling();
                    fallStartZ = sphere.getZ();
                    clip.stop();
                }
            }
        }

        sphere.update(currentTime);
    }

    private void handleFalling(double currentTime) {
        sphere.update(currentTime);
        sphere.setZ(fallStartZ);
        cam.setZ(gameZProgress - 500);
        if (sphere.getCurrentY() > 500) {
            gameState = GameState.GAME_OVER;
            ScoreManager.updateScore(level.songName(), score);
        }
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