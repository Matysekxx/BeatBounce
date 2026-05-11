package cz.matysekxx.beatbounce.model.game.state;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.BreakableTile;
import cz.matysekxx.beatbounce.model.entity.MovingTile;
import cz.matysekxx.beatbounce.model.entity.Orb;
import cz.matysekxx.beatbounce.model.game.GameEngine;

import javax.sound.sampled.Clip;

public class PlayingHandler implements GameStateHandler {
    private final GameEngine gameEngine;
    private final Clip clip;

    public PlayingHandler(GameEngine gameEngine, Clip clip) {
        this.gameEngine = gameEngine;
        this.clip = clip;
    }

    @Override
    public void handle(double currentTime, double deltaTime) {
        Settings.applyMusicVolume(clip);

        if (checkLevelEnd()) {
            return;
        }

        updateAudioAndProgress(deltaTime);
        updateSpeedEffect(deltaTime);
        updateTiles(deltaTime);
        updateCameraAndSphere();
        gameEngine.handleCollisions(deltaTime);
        checkOrbCollisions();

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

    private void updateTiles(double deltaTime) {
        for (AbstractTile tile : gameEngine.getUpdatableTiles()) {
            switch (tile) {
                case MovingTile movingTile -> {
                    final double distance = gameEngine.getCam().getDistanceTo(tile.getZ());
                    if (distance <= 0 || distance > 3000) continue;

                    movingTile.update(deltaTime);
                    int newX = movingTile.getX();

                    if (newX < -RenderUtils.ROAD_WIDTH) newX = -RenderUtils.ROAD_WIDTH;
                    else if (newX > RenderUtils.ROAD_WIDTH) newX = RenderUtils.ROAD_WIDTH;

                    movingTile.setLocation(newX, movingTile.getY());
                }
                case BreakableTile bt when bt.isBroken() -> bt.updateBreakAnimation(deltaTime);
                default -> {
                }
            }
        }
    }

    private void updateCameraAndSphere() {
        gameEngine.getSphere().setZ(gameEngine.getGameZProgress());
        gameEngine.getCam().setZ(gameEngine.getGameZProgress() - 500);

        final double targetCamX = gameEngine.getSphere().getX() * 0.2;
        gameEngine.getCam().setX(gameEngine.getCam().getX() + (targetCamX - gameEngine.getCam().getX()) * 0.05);
    }

    private void checkOrbCollisions() {
        for (Orb orb : gameEngine.getOrbs()) {
            if (!orb.isCollected()) {
                final double dz = orb.getZ() - gameEngine.getSphere().getZ();
                final double dx = orb.getX() - gameEngine.getSphere().getX();
                final double dy = orb.getY() - gameEngine.getSphere().getCurrentY();
                if (dz * dz + dx * dx + dy * dy < 6400) {
                    orb.setCollected(true);
                    gameEngine.incrementCollectedOrbs();
                }
            }
        }
    }
}