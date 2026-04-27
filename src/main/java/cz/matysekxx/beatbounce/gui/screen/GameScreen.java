package cz.matysekxx.beatbounce.gui.screen;


import cz.matysekxx.beatbounce.gui.components.GamePanel;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.level.Level;
import cz.matysekxx.beatbounce.model.level.LevelGenerator;

import java.awt.*;
import java.nio.file.Path;

public class GameScreen extends Screen {
    private final GamePanel gamePanel;

    public GameScreen() {
        super();
        this.setLayout(new BorderLayout());
        gamePanel = new GamePanel();
        this.add(gamePanel, BorderLayout.CENTER);
    }

    public void setupGamePanel(Path audioPath) {
        final String path = audioPath.toFile().getPath();
        final AudioData audioData = AudioData.create(path);
        final Level level = LevelGenerator.generateLevel(audioData, 1.f);
        gamePanel.init(level);
    }

    @Override
    public void start() {
        gamePanel.startGame();
    }
}
