package cz.matysekxx.beatbounce.configuration;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Settings {
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties properties = new Properties();
    public static boolean vsync = false;
    public static boolean opengl = true;
    public static boolean fullscreen = true;
    public static int soundVolume = 100;
    public static int targetFps = 60;
    public static boolean showFps = false;
    public static String graphicsQuality = "HIGH";
    public static int monitorIndex = 0;
    public static boolean particlesEnabled = true;
    public static boolean bloomEnabled = true;
    public static boolean muteOnFocusLoss = false;
    public static boolean isMuted = false;

    static {
        load();
    }

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

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "BeatBounce Configuration");
        } catch (IOException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }
}
