package cz.matysekxx.beatbounce.model.game.state;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.BreakableTile;
import cz.matysekxx.beatbounce.model.entity.MovingTile;
import cz.matysekxx.beatbounce.model.entity.Orb;
import cz.matysekxx.beatbounce.model.game.GameEngine;
import cz.matysekxx.beatbounce.model.game.OrbCollisionEngine;
import cz.matysekxx.beatbounce.model.game.TileManager;
import cz.matysekxx.beatbounce.model.game.collision.CollisionEngine;

import javax.sound.sampled.Clip;

public class PlayingHandler implements GameStateHandler {
    private final GameEngine gameEngine;
    private final Clip clip;
    private final CollisionEngine collisionEngine;
    private final TileManager tileManager;
    private final OrbCollisionEngine  orbCollisionEngine;

    public PlayingHandler(GameEngine gameEngine, Clip clip, TileManager tileManager) {
        this.gameEngine = gameEngine;
        this.clip = clip;
        this.collisionEngine = new CollisionEngine(gameEngine);
        this.tileManager = tileManager;
        this.orbCollisionEngine = new OrbCollisionEngine(gameEngine);
    }

    @Override
    public void handle(double currentTime, double deltaTime) {
        Settings.applyMusicVolume(clip);

        if (checkLevelEnd()) {
            return;
        }

        updateAudioAndProgress(deltaTime);
        updateSpeedEffect(deltaTime);
        tileManager.update(deltaTime);
        updateCameraAndSphere();
        collisionEngine.handleCollisions();
        orbCollisionEngine.checkOrbCollisions();

        gameEngine.getSphere().update(gameEngine.getSmoothedAudioTime(), deltaTime);
    }

    private boolean checkLevelEnd() {
        if (clip.getMicrosecondPosition() >= clip.getMicrosecondLength() - 50000) {
            gameEngine.setGameState(GameState.LEVEL_END_ANIMATION);
            gameEngine.setEndAnimationTimer(3.0);
            return true;
        }
        return false;
    }

    private void updateAudioAndProgress(double deltaTime) {
        final double rawAudioTime = clip.getMicrosecondPosition() / 1_000_000.0;

        if (gameEngine.getSmoothedAudioTime() == 0 && rawAudioTime > 0) {
            gameEngine.setSmoothedAudioTime(rawAudioTime);
        }

        gameEngine.setSmoothedAudioTime(gameEngine.getSmoothedAudioTime() + deltaTime);
        final double diff = rawAudioTime - gameEngine.getSmoothedAudioTime();

        double adjustment = (Math.abs(diff) > 0.05) ? diff : diff * 0.1;
        gameEngine.setSmoothedAudioTime(gameEngine.getSmoothedAudioTime() + adjustment);
        gameEngine.setGameZProgress(gameEngine.getSmoothedAudioTime() * gameEngine.getZUnitsPerSecond());
    }

    private void updateSpeedEffect(double deltaTime) {
        if (gameEngine.isSpeedEffectActive()) {
            gameEngine.setSpeedEffectTimeRemaining(gameEngine.getSpeedEffectTimeRemaining() - deltaTime);
            if (gameEngine.getSpeedEffectTimeRemaining() <= 0) {
                gameEngine.setSpeedEffectActive(false);
                gameEngine.setActiveSpeedMultiplier(1.0);
            }
        }
    }

    private void updateCameraAndSphere() {
        gameEngine.getSphere().setZ(gameEngine.getGameZProgress());
        gameEngine.getCam().setZ(gameEngine.getGameZProgress() - 500);
        final double targetCamX = gameEngine.getSphere().getX() * 0.2;
        gameEngine.getCam().setX(gameEngine.getCam().getX() + (targetCamX - gameEngine.getCam().getX()) * 0.05);
    }
}
