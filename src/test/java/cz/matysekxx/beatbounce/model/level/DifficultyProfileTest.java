package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.model.entity.TileType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link DifficultyProfile}.
 * Verifies that profiles are correctly generated for all difficulty levels.
 */
public class DifficultyProfileTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void testForStarsValidRanges(int stars) {
        DifficultyProfile profile = DifficultyProfile.forStars(stars);
        assertNotNull(profile);
        assertEquals(stars, profile.stars());
        assertTrue(profile.maxLanes() <= 2, "Max lanes should never exceed 2");
        assertTrue(profile.minBeatInterval() > 0, "Min beat interval must be positive");
        assertNotNull(profile.allowedTypes());
        assertFalse(profile.allowedTypes().isEmpty(), "Allowed types should not be empty");
    }

    @Test
    void testForStarsClamping() {
        assertEquals(1, DifficultyProfile.forStars(0).stars());
        assertEquals(10, DifficultyProfile.forStars(11).stars());
    }

    @Test
    void testProgressiveFeatureUnlock() {
        DifficultyProfile star1 = DifficultyProfile.forStars(1);
        DifficultyProfile star10 = DifficultyProfile.forStars(10);

        assertFalse(star1.allows(TileType.MOVING), "Star 1 should not allow moving tiles");
        assertTrue(star10.allows(TileType.MOVING), "Star 10 should allow moving tiles");

        assertTrue(star10.laneChangeFrequency() > star1.laneChangeFrequency(),
                "Higher difficulty should have higher lane change frequency");
        assertTrue(star1.minBeatInterval() > star10.minBeatInterval(),
                "Higher difficulty should have smaller minimum beat interval");
    }
}
