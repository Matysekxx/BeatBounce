package cz.matysekxx.beatbounce.model.achievement;

import cz.matysekxx.beatbounce.model.score.ScoreManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying the loading, progress tracking, unlocking, and rewards behavior of the AchievementManager.
 */
public class AchievementManagerTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Path testSavePath = tempDir.resolve("test_save_data.json");
        Path testCurrencyPath = tempDir.resolve("test_currency.json");
        ScoreManager.setStoragePaths(testSavePath, testCurrencyPath);
        AchievementManager.reset();
    }

    @Test
    void testLoadDefinitions() {
        List<Achievement> list = AchievementManager.getAchievements();
        assertNotNull(list);
        assertFalse(list.isEmpty());
        assertEquals(10, list.size());
    }

    @Test
    void testPlayAchievementUnlocks() {
        List<Achievement> list = AchievementManager.getAchievements();
        Achievement firstBounce = findAchievement(list, "first_bounce");
        assertNotNull(firstBounce);
        assertFalse(firstBounce.isCompleted());
        assertEquals(0, firstBounce.getCurrentProgress());

        AchievementManager.onLevelEnded();
        list = AchievementManager.getAchievements();
        firstBounce = findAchievement(list, "first_bounce");
        assertTrue(firstBounce.isCompleted());
        assertEquals(1, firstBounce.getCurrentProgress());
        assertFalse(firstBounce.isRewarded());
        assertEquals(0, ScoreManager.getCurrency());

        assertTrue(AchievementManager.claimReward(firstBounce));
        assertTrue(firstBounce.isRewarded());
        assertEquals(10, ScoreManager.getCurrency());
    }

    @Test
    void testUniqueSongsAchievement() {
        List<Achievement> list = AchievementManager.getAchievements();
        Achievement explorer = findAchievement(list, "explorer");
        assertNotNull(explorer);
        assertFalse(explorer.isCompleted());

        ScoreManager.updateScore("song1", 100);
        ScoreManager.updateScore("song2", 150);
        ScoreManager.updateScore("song3", 200);

        list = AchievementManager.getAchievements();
        explorer = findAchievement(list, "explorer");
        assertTrue(explorer.isCompleted());
        assertEquals(3, explorer.getCurrentProgress());
    }

    @Test
    void testHighScoreAchievement() {
        List<Achievement> list = AchievementManager.getAchievements();
        Achievement firstSuccess = findAchievement(list, "first_success");
        assertNotNull(firstSuccess);
        assertFalse(firstSuccess.isCompleted());

        ScoreManager.updateScore("song1", 6000);

        list = AchievementManager.getAchievements();
        firstSuccess = findAchievement(list, "first_success");
        assertTrue(firstSuccess.isCompleted());
        assertEquals(6000, firstSuccess.getCurrentProgress());
    }

    @Test
    void testOrbsHeldAchievement() {
        List<Achievement> list = AchievementManager.getAchievements();
        Achievement bronzeColl = findAchievement(list, "bronze_collector");
        assertNotNull(bronzeColl);
        assertFalse(bronzeColl.isCompleted());

        ScoreManager.addCurrency(50);

        list = AchievementManager.getAchievements();
        bronzeColl = findAchievement(list, "bronze_collector");
        assertTrue(bronzeColl.isCompleted());
    }

    private Achievement findAchievement(List<Achievement> list, String id) {
        return list.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
