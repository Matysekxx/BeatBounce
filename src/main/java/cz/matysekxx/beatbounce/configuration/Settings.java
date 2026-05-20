package cz.matysekxx.beatbounce.configuration;

import cz.matysekxx.beatbounce.system.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

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
     * Whether the application audio is currently muted.
     */
    public static boolean isMuted = false;

    private static final Map<String, byte[]> sfxCache = new ConcurrentHashMap<>();
    private static final Map<String, AudioFormat> formatCache = new ConcurrentHashMap<>();
    private static Clip menuMusicClip;
    private static String currentMenuMusicPath;

    static {
        load();
        preloadSFX("/click-sound.mp3");
    }

    /**
     * Applies the current sound volume settings to the given {@link Clip}.
     *
     * @param clip The audio clip to which the volume should be applied.
     */
    public static void applyMusicVolume(Clip clip) {
        applyVolume(clip, soundVolume);
    }

    /**
     * Applies the current effects volume settings to the given {@link Clip}.
     *
     * @param clip The audio clip to which the volume should be applied.
     */
    public static void applySFXVolume(Clip clip) {
        applyVolume(clip, sfxVolume);
    }

    private static void applyVolume(Clip clip, int volumeLevel) {
        if (clip == null) return;
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                final FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                final float volume = (isMuted) ? 0 : (volumeLevel / 100f);
                final float dB = (float) (Math.log(volume <= 0 ? 0.0001 : volume) / Math.log(10.0) * 20.0);
                gainControl.setValue(Math.clamp(dB, gainControl.getMinimum(), gainControl.getMaximum()));
            }
        } catch (Exception e) {
            LOG.warn("Failed to apply volume: {}", e.getMessage());
        }
    }

    /**
     * Preloads and decodes a sound effect into memory.
     *
     * @param resourcePath The path to the sound effect resource.
     */
    public static void preloadSFX(String resourcePath) {
        if (sfxCache.containsKey(resourcePath)) return;
        Thread.ofVirtual().start(() -> {
            try {
                final URL url = Settings.class.getResource(resourcePath);
                if (url == null) return;
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
                    AudioFormat baseFormat = ais.getFormat();
                    AudioFormat targetFormat = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            baseFormat.getSampleRate(),
                            16,
                            baseFormat.getChannels(),
                            baseFormat.getChannels() * 2,
                            baseFormat.getSampleRate(),
                            false
                    );

                    try (AudioInputStream decodedAis = AudioSystem.getAudioInputStream(targetFormat, ais)) {
                        final byte[] data = decodedAis.readAllBytes();
                        sfxCache.put(resourcePath, data);
                        formatCache.put(resourcePath, targetFormat);
                    }
                }
            } catch (Exception e) {
                LOG.warn("Failed to preload SFX {}: {}", resourcePath, e.getMessage());
            }
        });
    }

    /**
     * Plays a sound effect from the resources, using cache if available.
     *
     * @param resourcePath The path to the sound effect resource.
     */
    public static void playSFX(String resourcePath) {
        if (sfxCache.containsKey(resourcePath)) {
            try {
                final byte[] data = sfxCache.get(resourcePath);
                final AudioFormat format = formatCache.get(resourcePath);
                final Clip clip = AudioSystem.getClip();
                clip.open(format, data, 0, data.length);
                applySFXVolume(clip);
                clip.start();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                return;
            } catch (Exception e) {
                LOG.warn("Failed to play cached SFX {}: {}", resourcePath, e.getMessage());
            }
        }

        preloadSFX(resourcePath);
        try {
            final URL url = Settings.class.getResource(resourcePath);
            if (url == null) return;
            final Clip clip = AudioSystem.getClip();
            clip.open(getAudioInputStream(url));
            applySFXVolume(clip);
            clip.start();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        } catch (Exception e) {
            LOG.warn("Failed to play SFX {}: {}", resourcePath, e.getMessage());
        }
    }

    private static AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        final AudioInputStream ais = AudioSystem.getAudioInputStream(url);
        final AudioFormat baseFormat = ais.getFormat();
        final AudioFormat targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false
        );
        return AudioSystem.getAudioInputStream(targetFormat, ais);
    }

    /**
     * Starts playing menu music if not already playing.
     *
     * @param resourcePath The path to the music resource.
     */
    public static void playMenuMusic(String resourcePath) {
        if (menuMusicClip != null && menuMusicClip.isRunning() && resourcePath.equals(currentMenuMusicPath)) return;

        stopMenuMusic();

        try {
            final URL url = Settings.class.getResource(resourcePath);
            if (url == null) return;
            menuMusicClip = AudioSystem.getClip();
            menuMusicClip.open(getAudioInputStream(url));
            menuMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            applyMusicVolume(menuMusicClip);
            menuMusicClip.start();
            currentMenuMusicPath = resourcePath;
        } catch (Exception e) {
            LOG.warn("Failed to play menu music {}: {}", resourcePath, e.getMessage());
        }
    }

    /**
     * Stops the menu music.
     */
    public static void stopMenuMusic() {
        if (menuMusicClip != null) {
            if (menuMusicClip.isRunning()) menuMusicClip.stop();
            menuMusicClip.close();
            menuMusicClip = null;
            currentMenuMusicPath = null;
        }
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
                sfxVolume = Integer.parseInt(properties.getProperty("sfxVolume", "100"));
                targetFps = Integer.parseInt(properties.getProperty("targetFps", "60"));
                audioOffset = Integer.parseInt(properties.getProperty("audioOffset", "0"));
                showFps = Boolean.parseBoolean(properties.getProperty("showFps", "false"));
                graphicsQuality = properties.getProperty("graphicsQuality", "HIGH");
                monitorIndex = Integer.parseInt(properties.getProperty("monitorIndex", "0"));
                particlesEnabled = Boolean.parseBoolean(properties.getProperty("particlesEnabled", "true"));
                bloomEnabled = Boolean.parseBoolean(properties.getProperty("bloomEnabled", "true"));
                muteOnFocusLoss = Boolean.parseBoolean(properties.getProperty("muteOnFocusLoss", "false"));
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
        properties.setProperty("sfxVolume", String.valueOf(sfxVolume));
        properties.setProperty("targetFps", String.valueOf(targetFps));
        properties.setProperty("audioOffset", String.valueOf(audioOffset));
        properties.setProperty("showFps", String.valueOf(showFps));
        properties.setProperty("graphicsQuality", graphicsQuality);
        properties.setProperty("monitorIndex", String.valueOf(monitorIndex));
        properties.setProperty("particlesEnabled", String.valueOf(particlesEnabled));
        properties.setProperty("bloomEnabled", String.valueOf(bloomEnabled));
        properties.setProperty("muteOnFocusLoss", String.valueOf(muteOnFocusLoss));

        final Path path = FileSystem.getConfigFile();

        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            properties.store(fos, "BeatBounce Configuration");
        } catch (IOException e) {
            LOG.warn("Failed to save settings: " + e.getMessage());
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
        Settings.sfxVolume = 100;
        Settings.particlesEnabled = true;
        Settings.bloomEnabled = true;
        Settings.muteOnFocusLoss = false;
    }
}
