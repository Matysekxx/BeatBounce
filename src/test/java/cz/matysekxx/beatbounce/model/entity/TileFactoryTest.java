package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TileFactoryTest {

    private final BeatEvent mockEvent = BeatEvent.of(1.0, EventType.BEAT, 1.0);

    @Test
    void testCreateNormalTile() {
        NormalTile tile = TileFactory.createNormalTile(mockEvent, 100, 200, 300.0);
        assertNotNull(tile);
        assertEquals(100, tile.getX());
        assertEquals(200, tile.getY());
        assertEquals(300.0, tile.getZ());
        assertEquals(mockEvent, tile.getBeatEvent());
    }

    @Test
    void testCreateNormalTileWithFakes() {
        List<Integer> fakes = List.of(-200, 200);
        NormalTile tile = TileFactory.createNormalTileWithFakes(mockEvent, 0, 0, 500.0, fakes);
        assertNotNull(tile);
        assertEquals(fakes, tile.getFakeLaneOffsets());
    }

    @Test
    void testCreateMovingTile() {
        MovingTile tile = TileFactory.createMovingTile(mockEvent, 0, 0, 1000.0, 150, 2.0);
        assertNotNull(tile);
        assertEquals(150, tile.getAmplitude());
        assertEquals(2.0, tile.getSpeed());
    }

    @Test
    void testCreateLongTile() {
        LongTile tile = TileFactory.createLongTile(mockEvent, 0, 0, 1500.0, 400.0);
        assertNotNull(tile);
        assertEquals(400.0, tile.getLengthInZ());
    }

    @Test
    void testCreateSmallTile() {
        SmallTile tile = TileFactory.createSmallTile(mockEvent, 0, 0, 2000.0);
        assertNotNull(tile);
    }

    @Test
    void testCreateBreakableTile() {
        BreakableTile tile = TileFactory.createBreakableTile(mockEvent, 0, 0, 2500.0);
        assertNotNull(tile);
    }
}
