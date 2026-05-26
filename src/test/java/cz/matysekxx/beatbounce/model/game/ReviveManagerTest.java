package cz.matysekxx.beatbounce.model.game;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.LongTile;
import cz.matysekxx.beatbounce.model.entity.NormalTile;
import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.game.state.GameState;
import cz.matysekxx.beatbounce.model.level.Level;
import cz.matysekxx.beatbounce.model.score.ScoreManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Manual unit test for ReviveManager that avoids Mockito to remain compatible with JDK 25+.
 */
class ReviveManagerTest {
    private GameEngine gameEngine;
    private Sphere sphere;
    private ReviveManager reviveManager;
    private Level level;
    private Camera3D cam;

    @BeforeEach
    void setUp() throws IOException {
        Path tempSave = Files.createTempFile("beatbounce_save", ".dat");
        Path tempCurrency = Files.createTempFile("beatbounce_currency", ".dat");
        tempSave.toFile().deleteOnExit();
        tempCurrency.toFile().deleteOnExit();
        ScoreManager.setStoragePaths(tempSave, tempCurrency);

        sphere = new Sphere(0, 150, 0, 25);
        cam = new Camera3D(0, 0, -500, 500);

        AbstractTile tile0 = new NormalTile(null, new Point(0, 150), 0);
        AbstractTile tile1 = new LongTile(null, 0, 150, 1000, 500);
        level = new Level(List.of(tile0, tile1), null, "testSong", 3);
        
        gameEngine = new GameEngine(level, sphere, cam, null);
        reviveManager = new ReviveManager(gameEngine, sphere);

        ScoreManager.addCurrency(100);
    }

    @Test
    void testReviveResetsSphereState() {
        sphere.startFalling();
        sphere.setCurrentY(600); 
        
        gameEngine.setCurrentTileIndex(0);
        reviveManager.revive();
        
        assertEquals(GameState.COUNTDOWN, gameEngine.getGameState());
        assertEquals(150, sphere.getCurrentY(), 0.01);
        assertFalse(sphere.isFalling());

        assertTrue(sphere.isJumping(), "isJumping should be true for NormalTile revive");

        sphere.update(gameEngine.getSmoothedAudioTime(), 0);
        assertEquals(150, sphere.getCurrentY(), 0.01, "Sphere must be on the ground at start of countdown");
    }

    @Test
    void testLongTileLocalizedRevive() {
        gameEngine.setCurrentTileIndex(1);
        gameEngine.setFallStartZ(1300); 
        
        reviveManager.revive();

        assertEquals(1150, gameEngine.getGameZProgress(), 0.01);
        assertFalse(sphere.isJumping(), "isJumping should be false for LongTile revive");
        assertEquals(150, sphere.getCurrentY(), 0.01);
    }

    @Test
    void testReviveCostDeduction() {
        int initialCurrency = ScoreManager.getCurrency();
        int expectedCost = reviveManager.getReviveCost();
        reviveManager.revive();
        assertEquals(initialCurrency - expectedCost, ScoreManager.getCurrency());
    }
}
