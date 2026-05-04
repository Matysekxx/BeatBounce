package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.gui.components.GamePanel;
import cz.matysekxx.beatbounce.gui.components.LoadingPanel;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.level.LevelGenerator;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class GameScreen extends Screen {
    private final LoadingPanel loadingPanel;
    private final ScreenManager screenManager;
    private GamePanel gamePanel;

    public GameScreen(ScreenManager screenManager) {
        super();
        this.screenManager = screenManager;
        this.setLayout(new BorderLayout());
        this.setBackground(Color.BLACK);
        loadingPanel = new LoadingPanel();
    }

    public void setupGamePanel(Path audioPath, int stars) {
        if (gamePanel != null) {
            gamePanel.stopGame();
            gamePanel = null;
        }
        loadingPanel.setText("Loading Level...");
        loadingPanel.startAnimation();

        this.getContentPane().removeAll();
        this.getContentPane().add(loadingPanel, BorderLayout.CENTER);
        this.getContentPane().revalidate();
        this.getContentPane().repaint();

        AudioData.createAsync(audioPath.toFile().getPath()).thenApply(audioData -> {
            final float speedMultiplier = 1.0f;
            return LevelGenerator.generateLevel(audioData, speedMultiplier, stars);
        }).thenAccept(level -> SwingUtilities.invokeLater(() -> {
            this.getContentPane().removeAll();
            final Runnable onExit = () -> {
                screenManager.showScreen(MainMenuScreen.class);
            };
            gamePanel = new GamePanel(onExit);
            gamePanel.init(level);
            this.getContentPane().add(gamePanel, BorderLayout.CENTER);
            this.getContentPane().revalidate();
            this.getContentPane().repaint();
            gamePanel.startGame();
            gamePanel.requestFocusInWindow();
            loadingPanel.stopAnimation();
        })).exceptionally(ex -> {
            System.err.println(ex.getMessage());
            SwingUtilities.invokeLater(() -> {
                loadingPanel.setText("Failed to load level!");
            });
            return null;
        });
    }

    @Override
    public void start() {
        if (gamePanel != null) {
            gamePanel.startGame();
        }
    }

    @Override
    public void stop() {
        if (gamePanel != null) {
            gamePanel.stopGame();
        }
    }
}