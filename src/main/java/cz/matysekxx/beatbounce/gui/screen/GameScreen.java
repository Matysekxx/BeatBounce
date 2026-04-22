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
        final Dotenv dotenv = Dotenv.load();
        final AudioData audioData = AudioData.create(dotenv.get("AUDIO_FILE"));
        final Level level = LevelGenerator.generateLevel(
                new AudioAnalyzer(audioData, 1.f).analyze(), "ahoj svete");
        gamePanel = new GamePanel(level, audioData.clip(), audioData.samples(), audioData.format().getSampleRate());
        this.add(gamePanel, BorderLayout.CENTER);
    }

    @Override
    public void start() {
        gamePanel.startGame();
    }
}
