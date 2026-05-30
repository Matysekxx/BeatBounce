package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.components.GamePanel;
import cz.matysekxx.beatbounce.gui.components.LoadingPanel;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.level.LevelGenerator;
import cz.matysekxx.beatbounce.util.ExceptionHandler;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

/**
 * The screen where the actual gameplay takes place.
 * It manages the loading of the level and the game panel.
 *
 * @author Matysekxx
 */
public class GameScreen extends Screen {

    /**
     * Panel displayed while the level is loading.
     */
    private final LoadingPanel loadingPanel;

    /**
     * Manager for switching between different screens.
     */
    private final ScreenManager screenManager;

    /**
     * The active game panel containing the gameplay loop.
     */
    private GamePanel gamePanel;

    /**
     * Constructs a new GameScreen.
     *
     * @param screenManager the screen manager used for navigation
     */
    public GameScreen(ScreenManager screenManager) {
        super();
        this.screenManager = screenManager;
        this.setLayout(new BorderLayout());
        this.setBackground(Color.BLACK);
        loadingPanel = new LoadingPanel();
    }

    /**
     * Sets up the game panel with the specified audio file and difficulty (stars).
     * It loads the level asynchronously and shows a loading panel in the meantime.
     *
     * @param audioPath  the path to the audio file
     * @param stars      the difficulty level represented by stars
     * @param songTitle  the title of the song
     * @param songArtist the artist of the song
     */
    public void setupGamePanel(Path audioPath, int stars, String songTitle, String songArtist) {
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
            return LevelGenerator.generateLevel(audioData, speedMultiplier, stars, songArtist);
        }).thenAccept(level -> SwingUtilities.invokeLater(() -> {
            this.getContentPane().removeAll();
            final Runnable onExit = () -> screenManager.showScreen(MainMenuScreen.class);
            gamePanel = new GamePanel(onExit);
            gamePanel.init(level, songTitle, songArtist);
            this.getContentPane().add(gamePanel, BorderLayout.CENTER);
            this.getContentPane().revalidate();
            this.getContentPane().repaint();
            gamePanel.startGame();
            gamePanel.requestFocusInWindow();
            loadingPanel.stopAnimation();
        })).exceptionally(ex -> {
            ExceptionHandler.handle("Failed to load level", ex);
            SwingUtilities.invokeLater(() -> loadingPanel.setText("Failed to load level!"));
            return null;
        });
    }

    /**
     * Starts the game if the game panel is initialized.
     */
    @Override
    public void start() {
        if (gamePanel != null) {
            gamePanel.startGame();
        }
    }

    /**
     * Stops the game if the game panel is initialized.
     */
    @Override
    public void stop() {
        if (gamePanel != null) {
            gamePanel.stopGame();
        }
    }

    /**
     * Pauses the game when the window loses focus if the setting is enabled.
     */
    @Override
    public void onFocusLost() {
        if (Settings.pauseOnFocusLoss && gamePanel != null) {
            gamePanel.pause();
        }
    }
}