package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.NormalTile;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GenerationContextTest {

    @Test
    void testGenerateLevelFromEvents() {
        List<BeatEvent> events = Arrays.asList(
                BeatEvent.of(1.0, 0.8),
                BeatEvent.of(2.0, 0.9),
                BeatEvent.of(3.0, 0.7)
        );

        GenerationContext context = new GenerationContext(events, "TestSong", "TestArtist", null, 3);
        Level level = context.generate();

        assertNotNull(level);
        assertEquals("TestSong", level.songName());
        assertEquals("TestArtist", level.artist());

        List<AbstractTile> tiles = level.tiles();
        assertTrue(tiles.size() >= 3, "Level should have at least as many tiles as beats.");
        for (int i = 1; i < tiles.size(); i++) {
            assertTrue(tiles.get(i).getZ() >= tiles.get(i - 1).getZ(), "Tiles should be sorted by Z coordinate.");
        }
    }

    @Test
    void testGenerateRowTiles() {
        List<BeatEvent> events = Arrays.asList(
                BeatEvent.of(0.5, EventType.INTENSITY_HIGH_START, 1.0),
                BeatEvent.of(1.0, EventType.BEAT, 1.0),
                BeatEvent.of(1.5, EventType.BEAT, 1.0),
                BeatEvent.of(2.0, EventType.BEAT, 1.0)
        );

        GenerationContext context = new GenerationContext(events, "RowSong", "RowArtist", null, 6);
        Level level = context.generate();

        boolean hasMultiSegmentTile = false;
        for (AbstractTile tile : level.tiles()) {
            if (tile instanceof NormalTile nt) {
                if (nt.getRealLaneOffsets().size() + nt.getFakeLaneOffsets().size() > 1) {
                    hasMultiSegmentTile = true;
                    assertTrue(nt.getRealLaneOffsets().size() <= 2, "Real segments should be at most 2.");
                    assertEquals(5, nt.getRealLaneOffsets().size() + nt.getFakeLaneOffsets().size());
                }
            }
        }
    }

    @Test
    void testGenerateEmptyLevel() {
        List<BeatEvent> events = List.of();
        GenerationContext context = new GenerationContext(events, "EmptySong", "EmptyArtist", null, 1);
        Level level = context.generate();

        assertNotNull(level);
        assertTrue(level.tiles().isEmpty(), "Empty events should result in empty tiles.");
    }
}
