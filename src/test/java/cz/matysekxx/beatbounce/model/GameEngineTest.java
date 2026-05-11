package cz.matysekxx.beatbounce.model;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.NormalTile;
import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.game.GameEngine;
import cz.matysekxx.beatbounce.model.game.GameState;
import cz.matysekxx.beatbounce.model.level.Level;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.Clip;
import java.awt.*;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameEngineTest {

    private GameEngine gameEngine;
    private Level mockLevel;
    private Sphere mockSphere;
    private Camera3D mockCam;
    private Clip mockClip;
    private List<AbstractTile> tiles;
    private boolean clipStarted = false;
    private boolean clipStopped = false;

    @BeforeEach
    void setUp() {
        clipStarted = false;
        clipStopped = false;
        tiles = new ArrayList<>();
        tiles.add(new NormalTile(null, new Point(0, 150), 0));
        tiles.add(new NormalTile(null, new Point(0, 150), 500));

        mockLevel = new Level(tiles, null, "test.mp3", 3);
        mockSphere = new Sphere(0, 150, 0, 25);
        mockCam = new Camera3D(0, 0, -500, 500);
        mockClip = (Clip) Proxy.newProxyInstance(
                Clip.class.getClassLoader(),
                new Class[]{Clip.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getMicrosecondLength")) return 100_000_000L;
                    if (method.getName().equals("getMicrosecondPosition")) return 0L;
                    if (method.getName().equals("isRunning")) return false;
                    if (method.getName().equals("start")) {
                        clipStarted = true;
                        return null;
                    }
                    if (method.getName().equals("stop")) {
                        clipStopped = true;
                        return null;
                    }
                    if (method.getReturnType().equals(void.class)) return null;
                    if (method.getReturnType().equals(boolean.class)) return false;
                    if (method.getReturnType().equals(int.class)) return 0;
                    if (method.getReturnType().equals(float.class)) return 0f;
                    return null;
                }
        );

        gameEngine = new GameEngine(mockLevel, mockSphere, mockCam, mockClip);
    }

    @Test
    void testInitialStateIsCountdown() {
        assertEquals(GameState.COUNTDOWN, gameEngine.getGameState(), "Game should start in COUNTDOWN state.");
        assertTrue(gameEngine.getCountdownTime() > 0, "Countdown time should be positive.");
    }

    @Test
    void testCountdownToPlayingTransition() {
        gameEngine.update(0, 4.0);

        assertEquals(GameState.PLAYING, gameEngine.getGameState(), "Game should transition to PLAYING after countdown ends.");
        assertTrue(clipStarted, "Clip should be started when entering PLAYING state.");
    }

    @Test
    void testTogglePause() {
        gameEngine.update(0, 4.0);
        assertEquals(GameState.PLAYING, gameEngine.getGameState());
        gameEngine.togglePause();
        assertEquals(GameState.PAUSED, gameEngine.getGameState(), "GameState should be PAUSED after toggle.");
        assertTrue(clipStopped, "Clip should be stopped when pausing.");

        gameEngine.togglePause();
        assertEquals(GameState.COUNTDOWN, gameEngine.getGameState(), "GameState should go back to COUNTDOWN when unpausing.");
        assertTrue(gameEngine.getCountdownTime() > 3.0);
    }

    @Test
    void testScoreInitialization() {
        assertEquals(0, gameEngine.getScore(), "Initial score should be 0.");
    }

    @Test
    void testInitResetsEverything() {
        gameEngine.update(0, 4.0);
        mockSphere.setZ(100);

        gameEngine.init();
        assertEquals(GameState.COUNTDOWN, gameEngine.getGameState());
        assertEquals(0, gameEngine.getScore());
        assertEquals(0, mockSphere.getZ(), "Sphere should be reset to initial Z.");
        assertEquals(-500, mockCam.getZ());
    }
}
