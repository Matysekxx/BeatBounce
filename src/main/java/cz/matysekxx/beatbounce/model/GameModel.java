package cz.matysekxx.beatbounce.model;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.level.Level;

import javax.sound.sampled.Clip;
import java.util.EnumMap;
import java.util.function.Consumer;

public class GameModel {
    private static final int LANE_WIDTH = 120;

    private final Level level;
    private final Sphere sphere;
    private final Camera3D cam;
    private final Clip clip;

    private GameState gameState = GameState.PLAYING;
    private int currentTileIndex = -1;
    private double gameZProgress;
    private double fallStartZ = 0;
    private int score = 0;

    private final EnumMap<GameState, Consumer<Double>> stateHandlers = new EnumMap<>(GameState.class);

    public GameModel(Level level, Sphere sphere, Camera3D cam, Clip clip) {
        this.level = level;
        this.sphere = sphere;
        this.cam = cam;
        this.clip = clip;
        stateHandlers.put(GameState.PLAYING, this::handlePlaying);
        stateHandlers.put(GameState.FALLING, this::handleFalling);
        stateHandlers.put(GameState.GAME_OVER, this::handleGameOver);
    }

    public void init() {
        this.gameState = GameState.PLAYING;
        this.currentTileIndex = -1;
        this.gameZProgress = 0;
        this.fallStartZ = 0;
        this.sphere.reset();

        cam.setX(0);
        cam.setY(0);
        cam.setZ(-500);

        startNextJump(0);

        clip.setFramePosition(0);
        clip.start();
    }

    public void stop() {
        if (clip.isRunning()) {
            clip.stop();
        }
    }

    public Integer getScore() { return score; }

    public void update(double currentTime) {
        this.gameZProgress = currentTime * 1000.0;
        this.stateHandlers.get(gameState).accept(currentTime);
    }

    public void handleGameOver(double currentTime) { cam.setZ(gameZProgress - 500); }
    public void handlePlaying(double currentTime) {
        sphere.setZ(gameZProgress);
        cam.setZ(gameZProgress - 500);
        if (currentTileIndex + 1 < level.tiles().size()) {
            final AbstractTile nextTile = level.tiles().get(currentTileIndex + 1);
            if (gameZProgress >= nextTile.getZ()) {
                final double tileMinX = nextTile.getX() - (LANE_WIDTH / 2.0) - sphere.getRadius();
                final double tileMaxX = nextTile.getX() + (LANE_WIDTH / 2.0) + sphere.getRadius();
                if (sphere.getX() >= tileMinX && sphere.getX() <= tileMaxX) {
                    currentTileIndex++;
                    score++;
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

    public void handleFalling(double currentTime) {
        sphere.update(currentTime);
        sphere.setZ(fallStartZ);
        cam.setZ(gameZProgress - 500);
        if (sphere.getCurrentY() > 500) {
            gameState = GameState.GAME_OVER;
            score = 0;
        }
    }

    private void startNextJump(double currentTime) {
        final var tiles = level.tiles();
        final int nextIndex = currentTileIndex + 1;
        if (nextIndex >= tiles.size()) return;
        final AbstractTile currentTile = nextIndex > 0 ? tiles.get(nextIndex - 1) : null;
        final AbstractTile nextTile = tiles.get(nextIndex);
        final double startZ = (currentTile != null) ? currentTile.getZ() : 0;
        final double endZ = nextTile.getZ();
        final double distanceZ = endZ - startZ;
        double duration = distanceZ / 1000.0;
        if (duration <= 0) duration = 0.2;
        final double height = 50 + (distanceZ * 0.15);
        sphere.startJump(currentTime, duration, height);
    }

    public GameState getGameState() {
        return gameState;
    }
}
