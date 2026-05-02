package cz.matysekxx.beatbounce.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ScoreManager {
    private static final String SAVE_FILE = "save_data.json";
    private static final String CURRENCY_FILE = "currency.json";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Path savePath;
    private static final Path currencyPath;
    private static Map<String, Integer> scores = new HashMap<>();
    private static int totalCurrency = 0;

    static {
        final String userHome = System.getProperty("user.home");
        savePath = Paths.get(userHome, ".beatbounce", SAVE_FILE);
        currencyPath = Paths.get(userHome, ".beatbounce", CURRENCY_FILE);
        loadScores();
        loadCurrency();
    }

    public static void loadScores() {
        final File file = savePath.toFile();
        if (file.exists()) {
            try {
                scores = mapper.readValue(file, new TypeReference<HashMap<String, Integer>>() {
                });
            } catch (IOException e) {
                System.err.println("Failed to load scores: " + e.getMessage());
                scores = new HashMap<>();
            }
        } else {
            scores = new HashMap<>();
            saveScores();
        }
    }

    public static void saveScores() {
        try {
            File file = savePath.toFile();
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            mapper.writeValue(file, scores);
        } catch (IOException e) {
            System.err.println("Failed to save scores: " + e.getMessage());
        }
    }

    public static int getBestScore(String songId) {
        return scores.getOrDefault(songId, 0);
    }

    public static void updateScore(String songId, int score) {
        if (score > getBestScore(songId)) {
            scores.put(songId, score);
            saveScores();
        }
    }

    public static int getGlobalHighScore() {
        return scores.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
    }

    public static int getSongsPlayedCount() {
        return scores.size();
    }

    public static void loadCurrency() {
        final File file = currencyPath.toFile();
        if (file.exists()) {
            try {
                final var data = mapper.readValue(file, new TypeReference<HashMap<String, Integer>>() {});
                totalCurrency = data.getOrDefault("currency", 0);
            } catch (IOException e) {
                System.err.println("Failed to load currency: " + e.getMessage());
                totalCurrency = 0;
            }
        } else {
            totalCurrency = 0;
            saveCurrency();
        }
    }

    public static void saveCurrency() {
        try {
            final File file = currencyPath.toFile();
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            final Map<String, Integer> data = new HashMap<>();
            data.put("currency", totalCurrency);
            mapper.writeValue(file, data);
        } catch (IOException e) {
            System.err.println("Failed to save currency: " + e.getMessage());
        }
    }

    public static int getCurrency() {
        return totalCurrency;
    }

    public static void addCurrency(int amount) {
        if (amount > 0) {
            totalCurrency += amount;
            saveCurrency();
        }
    }
}