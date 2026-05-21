package cz.matysekxx.beatbounce.controller;

import cz.matysekxx.beatbounce.action.ActionQueue;
import cz.matysekxx.beatbounce.model.game.GameEngine;
import cz.matysekxx.beatbounce.model.game.ReviveManager;
import cz.matysekxx.beatbounce.model.game.state.GameState;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GameKeyController extends KeyAdapter {
    private final GameEngine gameEngine;
    private final ActionQueue actionQueue;
    private final Runnable quitAction;

    public GameKeyController(GameEngine gameEngine, ActionQueue actionQueue, Runnable quitAction) {
        this.gameEngine = gameEngine;
        this.actionQueue = actionQueue;
        this.quitAction = quitAction;
    }

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
                switch (key) {
                    case KeyEvent.VK_R -> actionQueue.add(gameEngine::init);
                    case KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER -> actionQueue.add(quitAction);
                }
            }
        }
    }
}
