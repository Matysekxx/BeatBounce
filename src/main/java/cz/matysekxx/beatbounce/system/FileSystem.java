package cz.matysekxx.beatbounce.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Centralized file system management for the application.
 * Handles path resolution and directory creation for game data.
 *
 * @author Matysekxx
 */
public class FileSystem {
    private static final Logger LOG = LoggerFactory.getLogger(FileSystem.class);

    /**
     * Name of the application data directory in the user's home folder.
     */
    private static final String APP_DIR_NAME = ".beatbounce";

    /**
     * Root path of the application's persistent storage.
     */
    private static final Path APP_ROOT;

    /**
     * Path where downloaded music files are kept.
     */
    private static final Path MUSIC_DIR;

    /**
     * Path where generated level data is cached.
     */
    private static final Path CACHE_DIR;

    /**
     * Path where configuration properties are stored.
     */
    private static final Path CONFIG_DIR;

    static {
        final String userHome = System.getProperty("user.home");
        APP_ROOT = Paths.get(userHome, APP_DIR_NAME);
        MUSIC_DIR = APP_ROOT.resolve("music");
        CACHE_DIR = APP_ROOT.resolve("cache").resolve("levels");
        CONFIG_DIR = APP_ROOT.resolve("config");

        ensureDirectories();
    }

    /**
     * Ensures all necessary application directories exist.
     */
    private static void ensureDirectories() {
        createDirectory(APP_ROOT);
        createDirectory(MUSIC_DIR);
        createDirectory(CACHE_DIR);
        createDirectory(CONFIG_DIR);
    }

    /**
     * Creates a directory at the specified path if it doesn't already exist.
     *
     * @param path The path of the directory to create.
     */
    private static void createDirectory(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            LOG.error("Failed to create directory: {} - {}", path, e.getMessage());
        }
    }

    /**
     * @return The root directory of the application.
     */
    public static Path getAppRoot() {
        return APP_ROOT;
    }

    /**
     * @return The directory where music files are stored.
     */
    public static Path getMusicDir() {
        return MUSIC_DIR;
    }

    /**
     * @return The directory where level cache is stored.
     */
    public static Path getCacheDir() {
        return CACHE_DIR;
    }

    /**
     * @return The directory where configuration files are stored.
     */
    public static Path getConfigDir() {
        return CONFIG_DIR;
    }

    /**
     * @return The path to the main configuration file.
     */
    public static Path getConfigFile() {
        return CONFIG_DIR.resolve("config.properties");
    }

    /**
     * @return The path to the save data file.
     */
    public static Path getSaveDataFile() {
        return APP_ROOT.resolve("save_data.json");
    }

    /**
     * @return The path to the currency/economy data file.
     */
    public static Path getCurrencyFile() {
        return APP_ROOT.resolve("currency.json");
    }

    /**
     * @return The path to the achievements save file.
     */
    public static Path getAchievementsSaveFile() {
        return APP_ROOT.resolve("achievements_save.json");
    }

    /**
     * Lists all music files in the music directory, sorted by last modified time.
     * Supported formats: .mp3, .wav, .ogg, .flac.
     *
     * @return A list of paths to audio files.
     */
    public static List<Path> listMusicFiles() {
        if (!Files.exists(MUSIC_DIR)) return Collections.emptyList();

        try (Stream<Path> stream = Files.list(MUSIC_DIR)) {
            return stream.filter(p -> {
                final String name = p.getFileName().toString().toLowerCase();
                return name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".ogg") || name.endsWith(".flac");
            }).sorted((p1, p2) -> {
                try {
                    return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
                } catch (IOException e) {
                    return 0;
                }
            }).toList();
        } catch (IOException e) {
            LOG.error("Failed to list music files: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Deletes all files and subdirectories within the specified directory.
     *
     * @param directory The directory to clear.
     */
    private static void deleteDirectoryContents(Path directory) {
        if (!Files.exists(directory)) return;

        try (Stream<Path> walk = Files.walk(directory)) {
            walk.sorted(Comparator.reverseOrder())
                    .filter(path -> !path.equals(directory))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            LOG.warn("Failed to delete: {} - {}", path, e.getMessage());
                        }
                    });
        } catch (IOException e) {
            LOG.error("Error walking directory: {} - {}", directory, e.getMessage());
        }
    }


    /**
     * Clears all downloaded music files and cache files.
     *
     * @return A {@link CompletableFuture} that completes when the cache is cleared.
     */
    public static CompletableFuture<Void> clearCache() {
        return CompletableFuture.runAsync(() -> {
            deleteDirectoryContents(MUSIC_DIR);
            deleteDirectoryContents(CACHE_DIR);
        });
    }
}
