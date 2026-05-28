package cz.matysekxx.beatbounce.model.score;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matysekxx.beatbounce.model.achievement.AchievementManager;
import cz.matysekxx.beatbounce.system.FileSystem;
import cz.matysekxx.beatbounce.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages game scores, currency, and persistence.
 * <p>
 * Handles loading and saving of high scores and total currency from/to JSON files.
 */
public class ScoreManager {
    /**
     * Logger for the ScoreManager class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ScoreManager.class);
    /**
     * Jackson ObjectMapper for JSON processing.
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Path to the file where high scores are saved.
     */
    private static Path savePath = FileSystem.getSaveDataFile();

    /**
     * Path to the file where total currency is saved.
     */
    private static Path currencyPath = FileSystem.getCurrencyFile();

    /**
     * Mapping of song identifiers to the user's best achieved score.
     */
    private static Map<String, Integer> scores = new ConcurrentHashMap<>();

    /**
     * The total count of orbs (currency) collected by the user.
     */
    private static int totalCurrency = 0;

    static {
        loadScores();
        loadCurrency();
    }

    /**
     * Overrides the storage paths for testing purposes.
     *
     * @param newSavePath     new path for save data
     * @param newCurrencyPath new path for currency data
     */
    public static synchronized void setStoragePaths(Path newSavePath, Path newCurrencyPath) {
        savePath = newSavePath;
        currencyPath = newCurrencyPath;
        loadScores();
        loadCurrency();
    }

    /**
     * Loads high scores from the local storage file.
     */
    public static synchronized void loadScores() {
        final File file = savePath.toFile();
        if (file.exists()) {
            try {
                final String encrypted = Files.readString(savePath);
                final String decrypted = SecurityUtils.decrypt(encrypted);
                if (decrypted == null) {
                    LOG.warn("Security Warning: Scores save file tampered or corrupted! Resetting high scores.");
                    scores = new ConcurrentHashMap<>();
                    saveScores();
                } else {
                    final Map<String, Integer> loadedScores = mapper.readValue(decrypted, new TypeReference<HashMap<String, Integer>>() {
                    });
                    scores = new ConcurrentHashMap<>(loadedScores);
                }
            } catch (IOException e) {
                LOG.warn("Failed to load scores: {}", e.getMessage());
                scores = new ConcurrentHashMap<>();
            }
        } else {
            scores = new ConcurrentHashMap<>();
            saveScores();
        }
    }

    /**
     * Saves current high scores to the local storage file.
     */
    public static synchronized void saveScores() {
        try {
            final String json = mapper.writeValueAsString(new HashMap<>(scores));
            final String encrypted = SecurityUtils.encrypt(json);
            Files.writeString(savePath, encrypted);
        } catch (IOException e) {
            LOG.warn("Failed to save scores: {}", e.getMessage());
        }
    }

    /**
     * Retrieves the best score for a specific song.
     *
     * @param songId the identifier of the song
     * @return the best score achieved for the song, or 0 if not played
     */
    public static int getBestScore(String songId) {
        return scores.getOrDefault(songId, 0);
    }

    /**
     * Updates the best score for a specific song if the new score is higher.
     *
     * @param songId the identifier of the song
     * @param score  the new score to record
     */
    public static synchronized void updateScore(String songId, int score) {
        if (score > getBestScore(songId)) {
            scores.put(songId, score);
            saveScores();
        }
        AchievementManager.onLevelEnded();
    }

    /**
     * Calculates the highest score across all played songs.
     *
     * @return the global high score
     */
    public static int getGlobalHighScore() {
        return scores.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
    }

    /**
     * Calculates the total cumulative score across all songs.
     *
     * @return the total cumulative score
     */
    public static int getTotalScore() {
        return scores.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * Returns the total number of unique songs that have been played.
     *
     * @return the count of played songs
     */
    public static int getSongsPlayedCount() {
        return scores.size();
    }

    /**
     * Loads the total currency from the local storage file.
     */
    public static synchronized void loadCurrency() {
        final File file = currencyPath.toFile();
        if (file.exists()) {
            try {
                final String encrypted = Files.readString(currencyPath);
                final String decrypted = SecurityUtils.decrypt(encrypted);
                if (decrypted == null) {
                    LOG.warn("Security Warning: Currency save file tampered or corrupted! Resetting orbs.");
                    totalCurrency = 0;
                    saveCurrency();
                } else {
                    final Map<String, Integer> data = mapper.readValue(decrypted, new TypeReference<HashMap<String, Integer>>() {
                    });
                    totalCurrency = data.getOrDefault("currency", 0);
                }
            } catch (IOException e) {
                LOG.warn("Failed to load currency: {}", e.getMessage());
                totalCurrency = 0;
            }
        } else {
            totalCurrency = 0;
            saveCurrency();
        }
    }

    /**
     * Saves the total currency to the local storage file.
     */
    public static synchronized void saveCurrency() {
        try {
            final Map<String, Integer> data = new HashMap<>();
            data.put("currency", totalCurrency);
            final String json = mapper.writeValueAsString(data);
            final String encrypted = SecurityUtils.encrypt(json);
            Files.writeString(currencyPath, encrypted);
        } catch (IOException e) {
            LOG.warn("Failed to save currency: {}", e.getMessage());
        }
    }

    /**
     * Retrieves the total amount of currency collected by the player.
     *
     * @return the total currency
     */
    public static synchronized int getCurrency() {
        return totalCurrency;
    }

    /**
     * Adds a specified amount to the total currency and saves it.
     * Negative amounts can be used for spending currency.
     *
     * @param amount the amount of currency to add
     */
    public static synchronized void addCurrency(int amount) {
        totalCurrency += amount;
        saveCurrency();
        AchievementManager.checkAchievements();
    }

    /**
     * Checks if the given score is a new high score for a specific song.
     *
     * @param songId the identifier of the song
     * @param score  the score to check
     * @return true if it's a new high score, false otherwise
     */
    public static boolean isHighScore(String songId, int score) {
        return score > getBestScore(songId);
    }
}