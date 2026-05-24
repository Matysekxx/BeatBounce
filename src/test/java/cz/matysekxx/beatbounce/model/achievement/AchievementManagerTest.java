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

    @Test
    void testFilteringAchievements() {
        List<Achievement> list = AchievementManager.getAchievements();
        List<Achievement> readyToClaim = AchievementManager.filterAchievements(list, "READY TO CLAIM");
        assertTrue(readyToClaim.isEmpty());

        AchievementManager.onLevelEnded();
        list = AchievementManager.getAchievements();

        readyToClaim = AchievementManager.filterAchievements(list, "READY TO CLAIM");
        assertEquals(1, readyToClaim.size());
        assertEquals("first_bounce", readyToClaim.getFirst().getId());

        Achievement firstBounce = readyToClaim.getFirst();
        assertTrue(AchievementManager.claimReward(firstBounce));

        list = AchievementManager.getAchievements();
        readyToClaim = AchievementManager.filterAchievements(list, "READY TO CLAIM");
        assertTrue(readyToClaim.isEmpty());

        List<Achievement> claimed = AchievementManager.filterAchievements(list, "CLAIMED");
        assertEquals(1, claimed.size());
        assertEquals("first_bounce", claimed.getFirst().getId());

        List<Achievement> inProgress = AchievementManager.filterAchievements(list, "IN PROGRESS");
        assertEquals(9, inProgress.size());

        List<Achievement> all = AchievementManager.filterAchievements(list, "ALL");
        assertEquals(10, all.size());
    }

    @Test
    void testSortingAchievements() {
        AchievementManager.onLevelEnded();
        ScoreManager.updateScore("s1", 10);
        ScoreManager.updateScore("s2", 10);
        ScoreManager.updateScore("s3", 10);
        List<Achievement> list = AchievementManager.getAchievements();
        Achievement explorer = findAchievement(list, "explorer");
        Achievement firstBounce = findAchievement(list, "first_bounce");
        assertTrue(explorer.isCompleted() && !explorer.isRewarded());
        assertTrue(firstBounce.isCompleted() && !firstBounce.isRewarded());

        ScoreManager.addCurrency(45);
        list = AchievementManager.getAchievements();

        List<Achievement> defaultSorted = AchievementManager.sortAchievements(list, "DEFAULT");
        assertEquals("first_bounce", defaultSorted.get(0).getId());
        assertEquals("explorer", defaultSorted.get(1).getId());

        List<Achievement> progressSorted = AchievementManager.sortAchievements(list, "PROGRESS");
        assertTrue(progressSorted.get(0).isCompleted());
        assertTrue(progressSorted.get(1).isCompleted());
        assertEquals("bronze_collector", progressSorted.get(2).getId());
        assertEquals("rising_star", progressSorted.get(3).getId());

        List<Achievement> rewardSorted = AchievementManager.sortAchievements(list, "REWARD");
        assertEquals("unstoppable", rewardSorted.get(0).getId());
        assertTrue(rewardSorted.get(1).getId().equals("music_guru") || rewardSorted.get(1).getId().equals("rhythm_addict"));
        assertTrue(rewardSorted.get(2).getId().equals("music_guru") || rewardSorted.get(2).getId().equals("rhythm_addict"));
        assertEquals("wealthy", rewardSorted.get(3).getId());
    }

    private Achievement findAchievement(List<Achievement> list, String id) {
        return list.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
