package cz.matysekxx.beatbounce.model.score;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link ScoreManager}.
 * Verifies that scores and currency are correctly managed and persisted.
 */
public class ScoreManagerTest {

    @TempDir
    Path tempDir;

    private Path testSavePath;
    private Path testCurrencyPath;

    @BeforeEach
    void setUp() {
        testSavePath = tempDir.resolve("test_save_data.json");
        testCurrencyPath = tempDir.resolve("test_currency.json");
        ScoreManager.setStoragePaths(testSavePath, testCurrencyPath);
    }

    @Test
    void testScorePersistence() throws IOException {
        String songId = "test_song";
        int score = 1500;

        ScoreManager.updateScore(songId, score);
        assertEquals(1500, ScoreManager.getBestScore(songId));
        assertTrue(Files.exists(testSavePath), "Save file should be created");
        ScoreManager.setStoragePaths(testSavePath, testCurrencyPath);
        assertEquals(1500, ScoreManager.getBestScore(songId), "Score should persist after reload");
    }

    @Test
    void testCurrencyManagement() {
        int initialCurrency = ScoreManager.getCurrency();
        ScoreManager.addCurrency(100);
        assertEquals(initialCurrency + 100, ScoreManager.getCurrency());

        ScoreManager.addCurrency(-50);
        assertEquals(initialCurrency + 50, ScoreManager.getCurrency());
    }

    @Test
    void testGlobalHighScore() {
        ScoreManager.updateScore("song1", 1000);
        ScoreManager.updateScore("song2", 2000);
        ScoreManager.updateScore("song3", 500);

        assertEquals(2000, ScoreManager.getGlobalHighScore());
        assertEquals(3, ScoreManager.getSongsPlayedCount());
    }

    @Test
    void testHighScoreDetection() {
        ScoreManager.updateScore("song1", 1000);
        assertTrue(ScoreManager.isHighScore("song1", 1100));
        assertFalse(ScoreManager.isHighScore("song1", 900));
    }
}
