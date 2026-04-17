package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.LongTile;
import cz.matysekxx.beatbounce.model.entity.NormalTile;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class LevelGeneratorTest {

    @Test
    public void testNormalTileGeneration() {
        List<BeatEvent> events = new ArrayList<>();
        events.add(BeatEvent.of(1.0, 10.0));
        events.add(BeatEvent.of(2.0, 10.0));

        Level level = LevelGenerator.generateLevel(events, "Test Song");

        assertEquals("Test Song", level.getSongName());
        assertEquals(2, level.getTiles().size());
        assertTrue(level.getTiles().get(0) instanceof NormalTile);
        assertTrue(level.getTiles().get(1) instanceof NormalTile);

        assertEquals(1000.0, level.getTiles().get(0).getZ(), 0.1);
        assertEquals(2000.0, level.getTiles().get(1).getZ(), 0.1);
    }

    @Test
    public void testLongTileGenerationInHighIntensity() {
        List<BeatEvent> events = new ArrayList<>();
        events.add(BeatEvent.of(0.5, EventType.INTENSITY_HIGH_START, 0.5));
        events.add(BeatEvent.of(1.0, 10.0));
        events.add(BeatEvent.of(1.2, 10.0));
        events.add(BeatEvent.of(1.5, 10.0));
        events.add(BeatEvent.of(2.0, EventType.INTENSITY_HIGH_END, 0.5));

        Level level = LevelGenerator.generateLevel(events, "Test Long");

        long longTileCount = level.getTiles().stream()
                .filter(t -> t instanceof LongTile)
                .count();
        
        assertEquals(1, longTileCount);
        AbstractTile longTile = level.getTiles().stream()
                .filter(t -> t instanceof LongTile)
                .findFirst()
                .orElseThrow();
        
        assertEquals(1000.0, longTile.getZ(), 0.1);
        assertEquals(500.0, longTile.getLengthInZ(), 0.1);
    }

    @Test
    public void testBeatCooldown() {
        List<BeatEvent> events = new ArrayList<>();
        events.add(BeatEvent.of(1.0, 10.0));
        events.add(BeatEvent.of(1.05, 10.0));

        Level level = LevelGenerator.generateLevel(events, "Test Cooldown");

        assertEquals(1, level.getTiles().size());
    }

    @Test
    public void testLaneVariety() {
        List<BeatEvent> events = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            events.add(BeatEvent.of(1.0 + i * 0.5, 10.0));
        }

        Level level = LevelGenerator.generateLevel(events, "Test Lanes");

        long distinctLanes = level.getTiles().stream()
                .map(AbstractTile::getX)
                .distinct()
                .count();
        
        assertTrue("Should use multiple lanes", distinctLanes > 1);
    }
}
