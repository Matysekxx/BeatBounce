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
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Path savePath;
    private static Map<String, Integer> scores = new HashMap<>();

    static {
        String userHome = System.getProperty("user.home");
        savePath = Paths.get(userHome, ".beatbounce", SAVE_FILE);
        loadScores();
    }

    public static void loadScores() {
        File file = savePath.toFile();
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
        return scores.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    public static int getSongsPlayedCount() {
        return scores.size();
    }
}