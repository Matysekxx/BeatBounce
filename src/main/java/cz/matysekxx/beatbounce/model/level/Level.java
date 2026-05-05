package cz.matysekxx.beatbounce.model.level;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Represents a game level, containing tile data, audio data, and metadata.
 *
 * @param tiles     the list of tiles in the level
 * @param audioData the audio data associated with the level (ignored in JSON)
 * @param songName  the name of the song
 * @param stars     the difficulty rating in stars
 */
public record Level(List<AbstractTile> tiles, @JsonIgnore AudioData audioData, String songName, int stars) {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Path CACHE_DIR;

    static {
        final String userHome = System.getProperty("user.home");
        CACHE_DIR = Paths.get(userHome, ".beatbounce", "cache", "levels");
        try {
            if (!Files.exists(CACHE_DIR)) Files.createDirectories(CACHE_DIR);
        } catch (IOException e) {
            System.err.println("Could not create level cache directory: " + e.getMessage());
        }
    }

    /**
     * Attempts to load level data from a cache file.
     *
     * @param audioFile       the original audio file
     * @param speedMultiplier the speed multiplier used for generation
     * @return an {@link Optional} containing {@link LevelCacheData} if found, otherwise empty
     */
    public static Optional<LevelCacheData> fromFile(File audioFile, float speedMultiplier) {
        try {
            final File cacheFile = getCacheFile(audioFile, speedMultiplier);
            if (cacheFile.exists()) {
                final LevelCacheData cacheData = objectMapper.readValue(cacheFile, LevelCacheData.class);
                return Optional.of(cacheData);
            }
            return Optional.empty();
        } catch (IOException e) {
            System.err.println("Failed to load level from cache: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Saves the level data to a cache file.
     *
     * @param level           the level to save
     * @param speedMultiplier the speed multiplier used for generation
     */
    public static void toFile(Level level, float speedMultiplier) {
        try {
            final File cacheFile = getCacheFile(level.audioData().file(), speedMultiplier);
            final LevelCacheData cacheData = new LevelCacheData(level.tiles(), level.songName(), level.stars());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(cacheFile, cacheData);
            System.out.println("Level saved to cache: " + cacheFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save level to cache: " + e.getMessage());
        }
    }

    /**
     * Generates a cache file reference for a given audio file and speed.
     *
     * @param audioFile       the audio file
     * @param speedMultiplier the speed multiplier
     * @return the cache {@link File}
     */
    private static File getCacheFile(File audioFile, float speedMultiplier) {
        final String baseName = audioFile.getName();
        final String nameWithoutExt = baseName.contains(".") ? baseName.substring(0, baseName.lastIndexOf('.')) : baseName;
        final String sanitizedName = nameWithoutExt.replaceAll("[^a-zA-Z0-9.-]", "_");
        final String fileName = sanitizedName + "-" + speedMultiplier + ".json";
        return CACHE_DIR.resolve(fileName).toFile();
    }
}