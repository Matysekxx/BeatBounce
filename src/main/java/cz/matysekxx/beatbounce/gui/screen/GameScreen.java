package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.gui.components.GamePanel;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.level.LevelGenerator;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class GameScreen extends Screen {
    private GamePanel gamePanel;
    private final JLabel loadingLabel;

    public GameScreen() {
        super();
        this.setLayout(new BorderLayout());
        this.setBackground(Color.BLACK);
        
        loadingLabel = new JLabel("Loading Level...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        loadingLabel.setForeground(Color.WHITE);
    }

    public void setupGamePanel(Path audioPath) {
        if (gamePanel != null) {
            gamePanel.stopGame();
            gamePanel = null;
        }

        this.getContentPane().removeAll();
        this.getContentPane().add(loadingLabel, BorderLayout.CENTER);
        this.getContentPane().revalidate();
        this.getContentPane().repaint();

        CompletableFuture.supplyAsync(() -> {
            final String path = audioPath.toFile().getPath();
            final AudioData audioData = AudioData.create(path);
            return LevelGenerator.generateLevel(audioData, 1.f);
        }).thenAccept(level -> SwingUtilities.invokeLater(() -> {
            this.getContentPane().removeAll();
            gamePanel = new GamePanel();
            gamePanel.init(level);
            this.getContentPane().add(gamePanel, BorderLayout.CENTER);
            this.getContentPane().revalidate();
            this.getContentPane().repaint();
            gamePanel.startGame();
            gamePanel.requestFocusInWindow();
        })).exceptionally(ex -> {
            System.err.println(ex.getMessage());
            SwingUtilities.invokeLater(() -> {
                loadingLabel.setText("Failed to load level!");
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