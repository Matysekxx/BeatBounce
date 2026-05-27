package cz.matysekxx.beatbounce.model.achievement;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matysekxx.beatbounce.system.FileSystem;
import cz.matysekxx.beatbounce.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for loading and saving achievement-related data.
 * It manages both the static definitions from resources and the player's persistent progress on disk.
 */
public class AchievementRepository {
    private static final Logger LOG = LoggerFactory.getLogger(AchievementRepository.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Path savePath = FileSystem.getAppRoot().resolve("achievements_save.json");

    /**
     * Loads achievement definitions from the classpath resource 'achievements.json'.
     * If the resource is not found, it initializes a hardcoded fallback list.
     *
     * @return a list of all defined {@link Achievement}s
     */
    public List<Achievement> loadDefinitions() {
        try (InputStream is = AchievementRepository.class.getResourceAsStream("/achievements.json")) {
            if (is != null) {
                return mapper.readValue(is, new TypeReference<>() {
                });
            } else {
                LOG.warn("achievements.json not found in resources. Initializing fallback list.");
                return initFallbacks();
            }
        } catch (IOException e) {
            LOG.error("Failed to load achievements definitions: {}", e.getMessage());
            return initFallbacks();
        }
    }

    /**
     * Provides a set of default achievement definitions if the external JSON is missing.
     */
    private List<Achievement> initFallbacks() {
        final List<Achievement> achievements = new ArrayList<>();
        achievements.add(new Achievement("first_bounce", "First Bounce", "Complete your first level play.", AchievementType.TOTAL_PLAYS, 1, 10));
        achievements.add(new Achievement("rising_star", "Rising Star", "Play 5 games in total.", AchievementType.TOTAL_PLAYS, 5, 25));
        achievements.add(new Achievement("rhythm_addict", "Rhythm Addict", "Play 20 games in total.", AchievementType.TOTAL_PLAYS, 20, 100));
        achievements.add(new Achievement("explorer", "Explorer", "Play at least 3 unique songs.", AchievementType.UNIQUE_SONGS, 3, 30));
        achievements.add(new Achievement("music_guru", "Music Guru", "Play at least 8 unique songs.", AchievementType.UNIQUE_SONGS, 8, 100));
        achievements.add(new Achievement("bronze_collector", "Bronze Collector", "Hold at least 50 orbs at once.", AchievementType.ORBS_HELD, 50, 15));
        achievements.add(new Achievement("wealthy", "Wealthy", "Hold at least 250 orbs at once.", AchievementType.ORBS_HELD, 250, 75));
        achievements.add(new Achievement("first_success", "First Success", "Achieve a high score of 5,000 points in any song.", AchievementType.HIGH_SCORE, 5000, 20));
        achievements.add(new Achievement("super_score", "Super Score", "Achieve a high score of 15,000 points in any song.", AchievementType.HIGH_SCORE, 15000, 50));
        achievements.add(new Achievement("unstoppable", "Unstoppable", "Achieve a high score of 30,000 points in any song.", AchievementType.HIGH_SCORE, 30000, 150));
        return achievements;
    }

    /**
     * Loads the player's persistent achievement progress from the local file system.
     * Decrypts the content using {@link SecurityUtils} and validates integrity.
     *
     * @return the loaded progress data, or a fresh object if file is missing/corrupted
     */
    public AchievementSaveData loadSaveData() {
        final File file = savePath.toFile();
        if (file.exists()) {
            try {
                final String encryptedContent = Files.readString(savePath);
                final String decrypted = SecurityUtils.decrypt(encryptedContent);
                if (decrypted == null) {
                    LOG.warn("Security Warning: Achievements save file tampered or corrupted! Resetting progress.");
                    final AchievementSaveData empty = new AchievementSaveData();
                    saveSaveData(empty);
                    return empty;
                }
                return mapper.readValue(decrypted, AchievementSaveData.class);
            } catch (IOException e) {
                LOG.warn("Failed to load achievements save data: {}", e.getMessage());
                return new AchievementSaveData();
            }
        } else {
            final AchievementSaveData data = new AchievementSaveData();
            saveSaveData(data);
            return data;
        }
    }

    /**
     * Persists the player's achievement progress to disk.
     * Encrypts the JSON representation before writing to ensure security.
     *
     * @param saveData the progress data to save
     */
    public void saveSaveData(AchievementSaveData saveData) {
        try {
            final String json = mapper.writeValueAsString(saveData);
            final String encrypted = SecurityUtils.encrypt(json);
            Files.writeString(savePath, encrypted);
        } catch (IOException e) {
            LOG.error("Failed to save achievements progress: {}", e.getMessage());
        }
    }
}
