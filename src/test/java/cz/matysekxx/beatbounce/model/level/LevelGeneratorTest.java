package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.NormalTile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LevelGeneratorTest {

    @Test
    public void testNormalTileGeneration() {
        List<BeatEvent> events = new ArrayList<>();
        events.add(BeatEvent.of(1.0, 10.0));
        events.add(BeatEvent.of(2.0, 10.0));

        Level level = LevelGenerator.generateLevel(events, "Test Song");

        assertEquals("Test Song", level.songName());
        assertEquals(2, level.tiles().size());
        assertInstanceOf(NormalTile.class, level.tiles().get(0));
        assertInstanceOf(NormalTile.class, level.tiles().get(1));

        assertEquals(1000.0, level.tiles().get(0).getZ(), 0.1);
        assertEquals(2000.0, level.tiles().get(1).getZ(), 0.1);
    }

    @Test
    public void testBeatCooldown() {
        List<BeatEvent> events = new ArrayList<>();
        events.add(BeatEvent.of(1.0, 10.0));
        events.add(BeatEvent.of(1.05, 10.0));

        Level level = LevelGenerator.generateLevel(events, "Test Cooldown");

        assertEquals(1, level.tiles().size());
    }

    @Test
    public void testLaneVariety() {
        List<BeatEvent> events = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            events.add(BeatEvent.of(1.0 + i * 0.5, 10.0));
        }

        Level level = LevelGenerator.generateLevel(events, "Test Lanes");

        long distinctLanes = level.tiles().stream()
                .map(AbstractTile::getX)
                .distinct()
                .count();

        assertTrue(distinctLanes > 1, "Should use multiple lanes");
    }
}
