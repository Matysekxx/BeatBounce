package cz.matysekxx.beatbounce.model.game.state;

import cz.matysekxx.beatbounce.model.audio.AudioManager;
import cz.matysekxx.beatbounce.model.game.GameEngine;
import cz.matysekxx.beatbounce.model.game.TileManager;
import cz.matysekxx.beatbounce.model.game.collision.CollisionEngine;
import cz.matysekxx.beatbounce.model.game.collision.OrbCollisionEngine;

import javax.sound.sampled.Clip;

/**
 * Handles the logic for the {@link GameState#PLAYING} state.
 * This is the primary gameplay handler, responsible for updating audio sync,
 * tile animations, camera movement, and orchestrating collision checks.
 */
public class PlayingHandler implements GameStateHandler {
    /**
     * The game engine providing state data.
     */
    private final GameEngine gameEngine;

    /**
     * The music clip currently playing.
     */
    private final Clip clip;

    /**
     * The engine responsible for tile collision detection.
     */
    private final CollisionEngine collisionEngine;

    /**
     * The manager for tile state updates.
     */
    private final TileManager tileManager;

    /**
     * The engine responsible for orb collision detection.
     */
    private final OrbCollisionEngine orbCollisionEngine;

    /**
     * Constructs a new PlayingHandler.
     *
     * @param gameEngine  the game engine
     * @param clip        the audio clip
     * @param tileManager the tile manager
     */
    public PlayingHandler(GameEngine gameEngine, Clip clip, TileManager tileManager) {
        this.gameEngine = gameEngine;
        this.clip = clip;
        this.collisionEngine = new CollisionEngine(gameEngine);
        this.tileManager = tileManager;
        this.orbCollisionEngine = new OrbCollisionEngine(gameEngine);
    }

    /**
     * Resets the collision detection interval.
     */
    public void resetCCD() {
        collisionEngine.resetCCD();
    }

    /**
     * Sets whether the player is currently on a long tile.
     *
     * @param onLongTile true if the player should be in the long-tile state
     */
    public void setOnLongTile(boolean onLongTile) {
        collisionEngine.setOnLongTile(onLongTile);
    }

    /**
     * Updates all gameplay systems for the current frame.
     *
     * @param currentTime the current world time
     * @param deltaTime   time since last frame in seconds
     */
    @Override
    public void handle(double currentTime, double deltaTime) {
        gameEngine.getSphere().update(currentTime, deltaTime);
        AudioManager.applyMusicVolume(clip);

        if (checkLevelEnd()) {
            return;
        }

        gameEngine.setGameZProgress(currentTime * gameEngine.getZUnitsPerSecond());
        tileManager.update(deltaTime);
        updateCameraAndSphere();
        collisionEngine.handleCollisions();
        orbCollisionEngine.checkOrbCollisions();
    }

    /**
     * Checks if the song has nearly finished and transitions to the end animation.
     */
    private boolean checkLevelEnd() {
        if (clip.getMicrosecondPosition() >= clip.getMicrosecondLength() - 50000) {
            gameEngine.setGameState(GameState.LEVEL_END_ANIMATION);
            gameEngine.setEndAnimationTimer(3.0);
            return true;
        }
        return false;
    }

    /**
     * Updates the camera and sphere positions based on world progress.
     */
    private void updateCameraAndSphere() {
        gameEngine.getSphere().setZ(gameEngine.getGameZProgress());
        gameEngine.getCam().setZ(gameEngine.getGameZProgress() - 500);
        final double targetCamX = gameEngine.getSphere().getX() * 0.2;
        gameEngine.getCam().setX(gameEngine.getCam().getX() + (targetCamX - gameEngine.getCam().getX()) * 0.05);
    }
}
