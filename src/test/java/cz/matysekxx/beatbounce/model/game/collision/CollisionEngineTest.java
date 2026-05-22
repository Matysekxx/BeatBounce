package cz.matysekxx.beatbounce.model.game.collision;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.NormalTile;
import cz.matysekxx.beatbounce.model.entity.Sphere;
import cz.matysekxx.beatbounce.model.game.GameEngine;
import cz.matysekxx.beatbounce.model.level.Level;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.*;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Test class for {@link CollisionEngine}.
 * Verifies that collisions and misses are correctly detected.
 */
public class CollisionEngineTest {

    private GameEngine mockEngine;
    private Sphere mockSphere;
    private Level mockLevel;
    private CollisionEngine collisionEngine;

    @BeforeEach
    void setUp() {
        mockEngine = mock(GameEngine.class);
        mockSphere = mock(Sphere.class);
        mockLevel = mock(Level.class);

        when(mockEngine.getSphere()).thenReturn(mockSphere);
        when(mockEngine.getLevel()).thenReturn(mockLevel);
        when(mockSphere.getRadius()).thenReturn(25);

        collisionEngine = new CollisionEngine(mockEngine);
    }

    @Test
    void testSuccessfulCollision() {
        final NormalTile nextTile = new NormalTile(null, new Point(0, 150), 500);
        when(mockEngine.getCurrentTileIndex()).thenReturn(0);
        when(mockLevel.tiles()).thenReturn(List.of(mock(AbstractTile.class), nextTile));

        when(mockSphere.getX()).thenReturn(0);

        when(mockEngine.getGameZProgress()).thenReturn(490.0);
        collisionEngine.handleCollisions();

        when(mockEngine.getGameZProgress()).thenReturn(510.0);
        collisionEngine.handleCollisions();

        verify(mockEngine, never()).startFalling();
    }

    @Test
    void testMissedCollisionDetection() {
        NormalTile nextTile = new NormalTile(null, new Point(0, 150), 500);
        when(mockEngine.getCurrentTileIndex()).thenReturn(0);
        when(mockLevel.tiles()).thenReturn(List.of(mock(AbstractTile.class), nextTile));

        when(mockSphere.getX()).thenReturn(100);

        when(mockEngine.getGameZProgress()).thenReturn(490.0);
        collisionEngine.handleCollisions();

        when(mockEngine.getGameZProgress()).thenReturn(510.0);
        collisionEngine.handleCollisions();

        verify(mockEngine, times(1)).startFalling();
    }

    @Test
    void testCCDPreventsSkipping() {
        NormalTile tileAt500 = new NormalTile(null, new Point(0, 150), 500);
        when(mockEngine.getCurrentTileIndex()).thenReturn(0);
        when(mockLevel.tiles()).thenReturn(List.of(mock(AbstractTile.class), tileAt500));

        when(mockSphere.getX()).thenReturn(200);

        when(mockEngine.getGameZProgress()).thenReturn(400.0);
        collisionEngine.handleCollisions();

        when(mockEngine.getGameZProgress()).thenReturn(600.0);
        collisionEngine.handleCollisions();

        verify(mockEngine, times(1)).startFalling();
    }
}
