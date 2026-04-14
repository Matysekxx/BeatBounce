package cz.matysekxx.beatbounce.gui.screen;


import cz.matysekxx.beatbounce.model.audio.AudioAnalyzer;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.level.Level;
import cz.matysekxx.beatbounce.model.level.LevelGenerator;
import io.github.cdimascio.dotenv.Dotenv;

import java.awt.*;

public class GameScreen extends Screen {
    private final GamePanel gamePanel;
    public GameScreen() {
        super();
        this.setLayout(new BorderLayout());
        Dotenv dotenv = Dotenv.load();
        LevelGenerator levelGenerator = new LevelGenerator();
        AudioData audioData = AudioData.create(dotenv.get("AUDIO_FILE"));
        Level level = levelGenerator.generateLevel(new AudioAnalyzer(audioData, 1.f).analyze(), null);
        gamePanel = new GamePanel(level, audioData.clip());
        this.add(gamePanel, BorderLayout.CENTER);
        gamePanel.startGame();
    }
}
