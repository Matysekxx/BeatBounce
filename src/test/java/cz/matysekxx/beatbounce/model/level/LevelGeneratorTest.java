package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.model.audio.AudioAnalyzer;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.NormalTile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import javax.sound.sampled.Clip;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class LevelGeneratorTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    private AudioData audioData;

    @BeforeEach
    void setUp() {
        Clip clip = mock(Clip.class);
        File file = mock(File.class);

        when(file.getAbsolutePath()).thenReturn("test.wav");
        when(file.getName()).thenReturn("test.wav");
        when(clip.getMicrosecondLength()).thenReturn(10_000_000L);
        audioData = new AudioData(new short[0], null, clip, file);

        LevelCacheManager.clear();
    }

    @Test
    public void testNormalTileGeneration() {
        List<BeatEvent> events = new ArrayList<>();
        events.add(BeatEvent.of(1.0, 10.0));
        events.add(BeatEvent.of(2.0, 10.0));

        try (MockedStatic<LevelFileCache> fileCache = mockStatic(LevelFileCache.class);
             MockedConstruction<AudioAnalyzer> analyzer = mockConstruction(AudioAnalyzer.class,
                     (mock, context) -> when(mock.analyze()).thenReturn(events))) {

            fileCache.when(() -> LevelFileCache.fromFile(any(), anyFloat())).thenReturn(Optional.empty());

            Level level = LevelGenerator.generateLevel(audioData, 1.0f, 5);

            assertEquals("test.wav", level.songName());
            assertTrue(level.tiles().size() >= 2);
            assertInstanceOf(NormalTile.class, level.tiles().get(0));
            assertInstanceOf(NormalTile.class, level.tiles().get(1));

            assertEquals(500.0, level.tiles().get(0).getZ(), 0.1);
            assertEquals(1000.0, level.tiles().get(1).getZ(), 0.1);
        }
    }

    @Test
    public void testBeatCooldown() {
        List<BeatEvent> events = new ArrayList<>();
        events.add(BeatEvent.of(1.0, 10.0));
        events.add(BeatEvent.of(1.05, 10.0));

        try (MockedStatic<LevelFileCache> fileCache = mockStatic(LevelFileCache.class);
             MockedConstruction<AudioAnalyzer> analyzer = mockConstruction(AudioAnalyzer.class,
                     (mock, context) -> when(mock.analyze()).thenReturn(events))) {

            fileCache.when(() -> LevelFileCache.fromFile(any(), anyFloat())).thenReturn(Optional.empty());

            Level level = LevelGenerator.generateLevel(audioData, 1.0f, 5);

            assertEquals(1, level.tiles().size());
        }
    }

    @Test
    public void testLaneVariety() {
        List<BeatEvent> events = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            events.add(BeatEvent.of(1.0 + i * 0.5, 10.0));
        }

        try (MockedStatic<LevelFileCache> fileCache = mockStatic(LevelFileCache.class);
             MockedConstruction<AudioAnalyzer> analyzer = mockConstruction(AudioAnalyzer.class,
                     (mock, context) -> when(mock.analyze()).thenReturn(events))) {

            fileCache.when(() -> LevelFileCache.fromFile(any(), anyFloat())).thenReturn(Optional.empty());

            Level level = LevelGenerator.generateLevel(audioData, 1.0f, 5);

            long distinctLanes = level.tiles().stream()
                    .map(AbstractTile::getX)
                    .distinct()
                    .count();

            assertTrue(distinctLanes > 1, "Should use multiple lanes");
        }
    }
}
