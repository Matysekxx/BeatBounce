package cz.matysekxx.beatbounce.configuration;

import cz.matysekxx.beatbounce.system.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Manages the application settings, including graphics, sound, and general preferences.
 * <p>
 * Settings are persisted in a {@code config.properties} file and can be loaded or saved
 * using the provided static methods.
 * </p>
 */
public class Settings {
    private static final Logger LOG = LoggerFactory.getLogger(Settings.class);

    /**
     * The {@link Properties} object used to manage configuration key-value pairs.
     */
    private static final Properties properties = new Properties();

    /**
     * Whether vertical synchronization is enabled to prevent screen tearing.
     */
    public static boolean vsync = false;

    /**
     * Whether OpenGL hardware acceleration should be used for rendering.
     */
    public static boolean opengl = true;

    /**
     * Whether the application should run in fullscreen mode.
     */
    public static boolean fullscreen = true;

    /**
     * The master sound volume level, from 0 to 100.
     */
    public static int soundVolume = 100;

    /**
     * The menu background music volume level, from 0 to 100.
     */
    public static int menuVolume = 80;

    /**
     * The effects sound volume level, from 0 to 100.
     */
    public static int sfxVolume = 100;

    /**
     * The target frames per second (FPS) for the rendering loop.
     */
    public static int targetFps = 60;

    /**
     * The audio offset in milliseconds for calibration.
     */
    public static int audioOffset = 0;

    /**
     * Whether to display the current FPS on the screen.
     */
    public static boolean showFps = false;

    /**
     * The graphical quality preset (e.g., "LOW", "MEDIUM", "HIGH").
     */
    public static String graphicsQuality = "HIGH";

    /**
     * The index of the monitor on which the application should be displayed.
     */
    public static int monitorIndex = 0;

    /**
     * Whether particle effects are enabled.
     */
    public static boolean particlesEnabled = true;

    /**
     * Whether the bloom post-processing effect is enabled.
     */
    public static boolean bloomEnabled = true;

    /**
     * Whether the audio should be muted when the window loses focus.
     */
    public static boolean muteOnFocusLoss = false;

    /**
     * Whether the game should be paused when the window loses focus.
     */
    public static boolean pauseOnFocusLoss = true;

    /**
     * Whether the application audio is currently muted.
     */
    public static boolean isMuted = false;

    static {
        load();
    }

    /**
     * Loads settings from the configuration file.
     * <p>
     * If the configuration file does not exist, default settings are saved.
     * </p>
     */
    public static void load() {
        final Path path = FileSystem.getConfigFile();
        if (Files.exists(path)) {
            try (FileInputStream fis = new FileInputStream(path.toFile())) {
                properties.load(fis);
                vsync = Boolean.parseBoolean(properties.getProperty("vsync", "false"));
                opengl = Boolean.parseBoolean(properties.getProperty("opengl", "true"));
                fullscreen = Boolean.parseBoolean(properties.getProperty("fullscreen", "true"));
                soundVolume = Integer.parseInt(properties.getProperty("soundVolume", "100"));
                menuVolume = Integer.parseInt(properties.getProperty("menuVolume", "80"));
                sfxVolume = Integer.parseInt(properties.getProperty("sfxVolume", "100"));
                targetFps = Integer.parseInt(properties.getProperty("targetFps", "60"));
                audioOffset = Integer.parseInt(properties.getProperty("audioOffset", "0"));
                showFps = Boolean.parseBoolean(properties.getProperty("showFps", "false"));
                graphicsQuality = properties.getProperty("graphicsQuality", "HIGH");
                monitorIndex = Integer.parseInt(properties.getProperty("monitorIndex", "0"));
                particlesEnabled = Boolean.parseBoolean(properties.getProperty("particlesEnabled", "true"));
                bloomEnabled = Boolean.parseBoolean(properties.getProperty("bloomEnabled", "true"));
                muteOnFocusLoss = Boolean.parseBoolean(properties.getProperty("muteOnFocusLoss", "false"));
                pauseOnFocusLoss = Boolean.parseBoolean(properties.getProperty("pauseOnFocusLoss", "true"));
            } catch (Exception e) {
                LOG.warn("Failed to load settings: {}", e.getMessage());
            }
        } else {
            save();
        }
    }

    /**
     * Saves the current settings to the configuration file.
     */
    public static void save() {
        properties.setProperty("vsync", Boolean.toString(vsync));
        properties.setProperty("opengl", String.valueOf(opengl));
        properties.setProperty("fullscreen", String.valueOf(fullscreen));
        properties.setProperty("soundVolume", String.valueOf(soundVolume));
        properties.setProperty("menuVolume", String.valueOf(menuVolume));
        properties.setProperty("sfxVolume", String.valueOf(sfxVolume));
        properties.setProperty("targetFps", String.valueOf(targetFps));
        properties.setProperty("audioOffset", String.valueOf(audioOffset));
        properties.setProperty("showFps", String.valueOf(showFps));
        properties.setProperty("graphicsQuality", graphicsQuality);
        properties.setProperty("monitorIndex", String.valueOf(monitorIndex));
        properties.setProperty("particlesEnabled", String.valueOf(particlesEnabled));
        properties.setProperty("bloomEnabled", String.valueOf(bloomEnabled));
        properties.setProperty("muteOnFocusLoss", String.valueOf(muteOnFocusLoss));
        properties.setProperty("pauseOnFocusLoss", String.valueOf(pauseOnFocusLoss));

        final Path path = FileSystem.getConfigFile();

        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            properties.store(fos, "BeatBounce Configuration");
        } catch (IOException e) {
            LOG.warn("Failed to save settings: {}", e.getMessage());
        }
    }

    /**
     * Resets the application settings to their default values.
     */
    public static void reset() {
        Settings.fullscreen = true;
        Settings.vsync = false;
        Settings.opengl = true;
        Settings.showFps = false;
        Settings.graphicsQuality = "HIGH";
        Settings.monitorIndex = 0;
        Settings.targetFps = 60;
        Settings.audioOffset = 0;
        Settings.soundVolume = 100;
        Settings.menuVolume = 80;
        Settings.sfxVolume = 100;
        Settings.particlesEnabled = true;
        Settings.bloomEnabled = true;
        Settings.muteOnFocusLoss = false;
    }
}
