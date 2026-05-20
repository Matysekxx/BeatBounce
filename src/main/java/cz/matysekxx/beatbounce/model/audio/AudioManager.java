package cz.matysekxx.beatbounce.model.audio;

import cz.matysekxx.beatbounce.configuration.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages game audio, including sound effects (SFX) and background menu music.
 * Handles caching, volume application, and playback.
 */
public class AudioManager {
    private static final Logger LOG = LoggerFactory.getLogger(AudioManager.class);

    private static final Map<String, byte[]> sfxCache = new ConcurrentHashMap<>();
    private static final Map<String, AudioFormat> formatCache = new ConcurrentHashMap<>();
    
    private static Clip menuMusicClip;
    private static String currentMenuMusicPath;

    static {
        preloadSFX("/click-sound.mp3");
    }

    /**
     * Preloads and decodes a sound effect into memory for instantaneous playback.
     *
     * @param resourcePath The path to the sound effect resource.
     */
    public static void preloadSFX(String resourcePath) {
        if (sfxCache.containsKey(resourcePath)) return;
        
        Thread.ofVirtual().start(() -> {
            try {
                URL url = getResourceURL(resourcePath);
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
                        byte[] data = decodedAis.readAllBytes();
                        sfxCache.put(resourcePath, data);
                        formatCache.put(resourcePath, targetFormat);
                        LOG.info("Preloaded SFX: {}", resourcePath);
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
                byte[] data = sfxCache.get(resourcePath);
                AudioFormat format = formatCache.get(resourcePath);
                Clip clip = AudioSystem.getClip();
                clip.open(format, data, 0, data.length);
                applySFXVolume(clip);
                clip.start();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) clip.close();
                });
                return;
            } catch (Exception e) {
                LOG.warn("Failed to play cached SFX {}: {}", resourcePath, e.getMessage());
            }
        }

        preloadSFX(resourcePath);
        try {
            final Clip clip = AudioSystem.getClip();
            clip.open(getAudioInputStream(resourcePath));
            applySFXVolume(clip);
            clip.start();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) clip.close();
            });
        } catch (Exception e) {
            LOG.warn("Failed to play SFX {}: {}", resourcePath, e.getMessage());
        }
    }

    private static AudioInputStream getAudioInputStream(String resourcePath) throws UnsupportedAudioFileException, IOException {
        final URL url = getResourceURL(resourcePath);
        assert url != null;
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
            menuMusicClip = AudioSystem.getClip();
            menuMusicClip.open(getAudioInputStream(resourcePath));
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
     * Applies the current music volume setting from {@link Settings} to the given clip.
     *
     * @param clip The audio clip.
     */
    public static void applyMusicVolume(Clip clip) {
        applyVolume(clip, Settings.soundVolume);
    }

    /**
     * Applies the current SFX volume setting from {@link Settings} to the given clip.
     *
     * @param clip The audio clip.
     */
    public static void applySFXVolume(Clip clip) {
        applyVolume(clip, Settings.sfxVolume);
    }

    private static void applyVolume(Clip clip, int volumeLevel) {
        if (clip == null) return;
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float volume = (Settings.isMuted) ? 0 : (volumeLevel / 100f);
                float dB = (float) (Math.log(volume <= 0 ? 0.0001 : volume) / Math.log(10.0) * 20.0);
                gainControl.setValue(Math.clamp(dB, gainControl.getMinimum(), gainControl.getMaximum()));
            }
        } catch (Exception e) {
            LOG.warn("Failed to apply volume: {}", e.getMessage());
        }
    }

    private static URL getResourceURL(String path) {
        try {
            URL url = AudioManager.class.getResource(path);
            if (url == null) {
                File file = new File(path);
                if (!file.exists()) {
                    file = new File("src/main/resources" + (path.startsWith("/") ? "" : "/") + path);
                }
                if (file.exists()) url = file.toURI().toURL();
            }
            return url;
        } catch (Exception e) {
            return null;
        }
    }
}
