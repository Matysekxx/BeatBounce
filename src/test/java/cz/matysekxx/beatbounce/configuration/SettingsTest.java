package cz.matysekxx.beatbounce.configuration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class SettingsTest {

    private static final String CONFIG_FILE = "config.properties";
    private File backupFile;

    @BeforeEach
    void setUp() throws IOException {
        final File original = new File(CONFIG_FILE);
        if (original.exists()) {
            backupFile = new File(CONFIG_FILE + ".bak");
            Files.copy(original.toPath(), backupFile.toPath());
            original.delete();
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        final File original = new File(CONFIG_FILE);
        if (original.exists()) {
            original.delete();
        }
        if (backupFile != null && backupFile.exists()) {
            Files.move(backupFile.toPath(), original.toPath());
        }
    }

    @Test
    void testSaveAndLoad() {
        Settings.opengl = false;
        Settings.fullscreen = false;
        Settings.soundVolume = 50;
        Settings.targetFps = 120;
        Settings.showFps = true;
        Settings.graphicsQuality = "MEDIUM";
        Settings.monitorIndex = 1;
        Settings.particlesEnabled = false;
        Settings.bloomEnabled = false;
        Settings.muteOnFocusLoss = true;

        Settings.save();
        Settings.load();

        assertFalse(Settings.opengl);
        assertFalse(Settings.fullscreen);
        assertEquals(50, Settings.soundVolume);
        assertEquals(120, Settings.targetFps);
        assertTrue(Settings.showFps);
        assertEquals("MEDIUM", Settings.graphicsQuality);
        assertEquals(1, Settings.monitorIndex);
        assertFalse(Settings.particlesEnabled);
        assertFalse(Settings.bloomEnabled);
        assertTrue(Settings.muteOnFocusLoss);
    }

    @Test
    void testDefaultValues() {
        final File file = new File(CONFIG_FILE);
        if (file.exists()) file.delete();

        Settings.load();

        assertTrue(file.exists());
        assertTrue(Settings.opengl);
        assertTrue(Settings.fullscreen);
        assertEquals(100, Settings.soundVolume);
        assertEquals(60, Settings.targetFps);
    }
}
