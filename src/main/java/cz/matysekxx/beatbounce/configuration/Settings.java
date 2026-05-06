package cz.matysekxx.beatbounce.configuration;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Manages the application settings, including graphics, sound, and general preferences.
 * <p>
 * Settings are persisted in a {@code config.properties} file and can be loaded or saved
 * using the provided static methods.
 * </p>
 */
public class Settings {
    /**
     * The name of the configuration file where settings are stored.
     */
    private static final String CONFIG_FILE = System.getProperty("user.home")
            + File.separator + ".beatbounce" + File.separator + "config" + File.separator + "config.properties";

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
     * The target frames per second (FPS) for the rendering loop.
     */
    public static int targetFps = 60;

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
     * Whether the application audio is currently muted.
     */
    public static boolean isMuted = false;

    static {
        load();
    }

    /**
     * Applies the current sound volume settings to the given {@link Clip}.
     *
     * @param clip The audio clip to which the volume should be applied.
     */
    public static void applyMusicVolume(Clip clip) {
        if (clip == null) return;
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                final FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                final float volume = (isMuted) ? 0 : (soundVolume / 100f);
                final float dB = (float) (Math.log(volume <= 0 ? 0.0001 : volume) / Math.log(10.0) * 20.0);
                gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
            }
        } catch (Exception e) {
            System.err.println("Failed to apply volume: " + e.getMessage());
        }
    }

    /**
     * Loads settings from the configuration file.
     * <p>
     * If the configuration file does not exist, default settings are saved.
     * </p>
     */
    public static void load() {
        final File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);
                vsync = Boolean.parseBoolean(properties.getProperty("vsync", "false"));
                opengl = Boolean.parseBoolean(properties.getProperty("opengl", "true"));
                fullscreen = Boolean.parseBoolean(properties.getProperty("fullscreen", "true"));
                soundVolume = Integer.parseInt(properties.getProperty("soundVolume", "100"));
                targetFps = Integer.parseInt(properties.getProperty("targetFps", "60"));
                showFps = Boolean.parseBoolean(properties.getProperty("showFps", "false"));
                graphicsQuality = properties.getProperty("graphicsQuality", "HIGH");
                monitorIndex = Integer.parseInt(properties.getProperty("monitorIndex", "0"));
                particlesEnabled = Boolean.parseBoolean(properties.getProperty("particlesEnabled", "true"));
                bloomEnabled = Boolean.parseBoolean(properties.getProperty("bloomEnabled", "true"));
                muteOnFocusLoss = Boolean.parseBoolean(properties.getProperty("muteOnFocusLoss", "false"));
            } catch (Exception e) {
                System.err.println("Failed to load settings: " + e.getMessage());
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
        properties.setProperty("targetFps", String.valueOf(targetFps));
        properties.setProperty("showFps", String.valueOf(showFps));
        properties.setProperty("graphicsQuality", graphicsQuality);
        properties.setProperty("monitorIndex", String.valueOf(monitorIndex));
        properties.setProperty("particlesEnabled", String.valueOf(particlesEnabled));
        properties.setProperty("bloomEnabled", String.valueOf(bloomEnabled));
        properties.setProperty("muteOnFocusLoss", String.valueOf(muteOnFocusLoss));

        final File configFile = new File(CONFIG_FILE);
        final File parentDir = configFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            properties.store(fos, "BeatBounce Configuration");
        } catch (IOException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }
}
