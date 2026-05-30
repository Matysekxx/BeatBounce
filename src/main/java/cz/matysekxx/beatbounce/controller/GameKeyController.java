package cz.matysekxx.beatbounce.controller;

import cz.matysekxx.beatbounce.action.ActionQueue;
import cz.matysekxx.beatbounce.model.game.GameEngine;
import cz.matysekxx.beatbounce.model.game.ReviveManager;
import cz.matysekxx.beatbounce.model.game.state.GameState;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * A keyboard input controller that translates key events into game actions.
 * It routes commands through the {@link ActionQueue} to ensure thread-safe execution
 * relative to the game state.
 *
 * @author Matysekxx
 */
public class GameKeyController extends KeyAdapter {
    /**
     * The game engine instance to control.
     */
    private final GameEngine gameEngine;

    /**
     * The queue where game actions are submitted.
     */
    private final ActionQueue actionQueue;

    /**
     * Action to be executed when exiting the game screen.
     */
    private final Runnable quitAction;

    /**
     * Constructs a new {@code GameKeyController}.
     *
     * @param gameEngine  the game logic engine
     * @param actionQueue the action queue for submitted tasks
     * @param quitAction  the task to execute on exit/quit
     */
    public GameKeyController(GameEngine gameEngine, ActionQueue actionQueue, Runnable quitAction) {
        this.gameEngine = gameEngine;
        this.actionQueue = actionQueue;
        this.quitAction = quitAction;
    }

    /**
     * Handles key press events and submits appropriate actions based on current {@link GameState}.
     *
     * @param e the key event
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (gameEngine == null) return;
        final GameState state = gameEngine.getGameState();
        final int key = e.getKeyCode();
        switch (state) {
            case PLAYING, COUNTDOWN -> {
                if (key == KeyEvent.VK_ESCAPE) actionQueue.add(gameEngine::togglePause);
            }
            case PAUSED -> {
                if (key == KeyEvent.VK_ESCAPE) actionQueue.add(gameEngine::togglePause);
                else if (key == KeyEvent.VK_ENTER) actionQueue.add(quitAction);
            }
            case GAME_OVER -> {
                if (gameEngine.getRevivesUsed() < ReviveManager.MAX_REVIVES && !gameEngine.isReviveDeclined()) {
                    if (key == KeyEvent.VK_V) actionQueue.add(gameEngine::revive);
                    else if (key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_ENTER)
                        actionQueue.add(gameEngine::declineRevive);
                } else {
                    if (key == KeyEvent.VK_R) actionQueue.add(gameEngine::init);
                    else if (key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_ENTER) actionQueue.add(quitAction);
                }
            }
            case FINISHED -> {
                if (key == KeyEvent.VK_R) actionQueue.add(gameEngine::init);
                else if (key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_ENTER) actionQueue.add(quitAction);
            }
        }
    }
}
