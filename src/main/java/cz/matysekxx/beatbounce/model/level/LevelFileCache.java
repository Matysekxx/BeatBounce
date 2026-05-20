package cz.matysekxx.beatbounce.model.level;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matysekxx.beatbounce.system.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class LevelFileCache {
    private static final Logger LOG = LoggerFactory.getLogger(LevelFileCache.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Path CACHE_DIR = FileSystem.getCacheDir();
    private static final int CACHE_VERSION = 2;

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
            LOG.warn("Failed to load level from cache: {}", e.getMessage());
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
            final LevelCacheData cacheData = new LevelCacheData(
                    level.tiles(), level.songName(), level.stars(),
                    LevelCacheData.CURRENT_VERSION, 0.0, 0);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(cacheFile, cacheData);
            LOG.info("Level saved to cache: {}", cacheFile.getAbsolutePath());
        } catch (IOException e) {
            LOG.warn("Failed to save level to cache: {}", e.getMessage());
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
        final double zSpeed = LevelGenerator.getZSpeed();
        final String fileName = String.format("%s-sm%.1f-zs%.0f-v%d.json",
                sanitizedName, speedMultiplier, zSpeed, CACHE_VERSION);
        return CACHE_DIR.resolve(fileName).toFile();
    }
}
