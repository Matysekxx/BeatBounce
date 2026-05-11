package cz.matysekxx.beatbounce.model.game.state;

public interface GameStateHandler {
    void handle(double currentTime, double deltaTime);
}