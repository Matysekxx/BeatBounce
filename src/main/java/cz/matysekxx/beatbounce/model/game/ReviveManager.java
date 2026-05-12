package cz.matysekxx.beatbounce.model.game;

import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.game.state.GameState;
import cz.matysekxx.beatbounce.model.score.ScoreManager;

public class ReviveManager {
    public static final int MAX_REVIVES = 3;
    private final GameEngine gameEngine;
    private final Sphere sphere;
    private int revivesUsed = 0;

    public ReviveManager(GameEngine gameEngine, Sphere sphere) {
        this.gameEngine = gameEngine;
        this.sphere = sphere;
    }

    public boolean revive() {
        if (revivesUsed >= MAX_REVIVES) return false;
        final int cost = getReviveCost();
        if (ScoreManager.getCurrency() >= cost) {
            ScoreManager.addCurrency(-cost);
            revivesUsed++;
            gameEngine.setGameState(GameState.COUNTDOWN);
            gameEngine.setCountdownTime(2.99);
            gameEngine.setFallStartZ(0);
            this.sphere.revive();
            final long microPos = gameEngine.getClip().getMicrosecondPosition();
            gameEngine.getClip().setMicrosecondPosition(Math.max(0, microPos - 1_500_000));
            gameEngine.setSmoothedAudioTime(gameEngine.getClip().getMicrosecondPosition() / 1_000_000.0);
            gameEngine.setGameZProgress(gameEngine.getSmoothedAudioTime() * gameEngine.getZUnitsPerSecond());
            return true;
        }
        return false;
    }

    public int getReviveCost() {
        return 10 * (int) Math.pow(2, revivesUsed);
    }

    public boolean canRevive() {
        return revivesUsed < MAX_REVIVES && ScoreManager.getCurrency() >= getReviveCost();
    }

    public int getRevivesUsed() {
        return revivesUsed;
    }

    public void setRevivesUsed(int revivesUsed) {
        this.revivesUsed = revivesUsed;
    }
}
