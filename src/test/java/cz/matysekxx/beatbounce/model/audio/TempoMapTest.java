package cz.matysekxx.beatbounce.model.audio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link TempoMap}.
 */
public class TempoMapTest {

    @Test
    void testGetBeatInterval() {
        TempoMap tempoMap = new TempoMap(120.0, 0.0);
        assertEquals(0.5, tempoMap.getBeatInterval(), 0.001);

        tempoMap = new TempoMap(60.0, 0.0);
        assertEquals(1.0, tempoMap.getBeatInterval(), 0.001);
    }

    @Test
    void testQuantizeToBeat() {
        TempoMap tempoMap = new TempoMap(120.0, 0.0);
        assertEquals(0.0, tempoMap.quantizeToBeat(0.1));
        assertEquals(0.5, tempoMap.quantizeToBeat(0.4));
        assertEquals(0.5, tempoMap.quantizeToBeat(0.6));
        assertEquals(1.0, tempoMap.quantizeToBeat(0.9));

        tempoMap = new TempoMap(120.0, 0.1);
        assertEquals(0.1, tempoMap.quantizeToBeat(0.2));
        assertEquals(0.6, tempoMap.quantizeToBeat(0.5));
    }

    @Test
    void testIsValidBpm() {
        assertTrue(new TempoMap(60.0, 0.0).isValidBpm());
        assertTrue(new TempoMap(240.0, 0.0).isValidBpm());
        assertFalse(new TempoMap(59.9, 0.0).isValidBpm());
        assertFalse(new TempoMap(240.1, 0.0).isValidBpm());
    }
}
