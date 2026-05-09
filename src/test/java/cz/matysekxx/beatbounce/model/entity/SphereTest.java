package cz.matysekxx.beatbounce.model.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SphereTest {

    private Sphere sphere;

    @BeforeEach
    void setUp() {
        sphere = new Sphere(0, 150, 0, 25);
    }

    @Test
    void testInitialPosition() {
        assertEquals(0, sphere.getX());
        assertEquals(150, sphere.getCurrentY());
        assertEquals(0, sphere.getZ());
    }

    @Test
    void testHorizontalInterpolation() {
        sphere.setTargetX(100);
        sphere.update(0);
        assertTrue(sphere.getX() > 0, "X position should have moved towards target.");
        assertTrue(sphere.getX() < 100, "X position should not have reached target instantly.");
        for (int i = 0; i < 20; i++) sphere.update(0);
        assertTrue(sphere.getX() >= 99, "X position should eventually reach target (approximate due to int truncation).");
    }

    @Test
    void testJumpPhysics() {
        sphere.startJump(0.0, 1.0, 100);
        assertTrue(sphere.isJumping());
        sphere.update(0.5);
        assertEquals(50, sphere.getCurrentY(), 1.0, "At peak, Y should be 50 (floor 150 - peak 100).");
        sphere.update(1.1);
        assertFalse(sphere.isJumping(), "Jump should be finished after duration.");
        assertEquals(150, sphere.getCurrentY(), "After jump, sphere should be back on floor (y=150).");
    }

    @Test
    void testFallingPhysics() {
        sphere.startFalling();
        double initialY = sphere.getCurrentY();
        
        sphere.update(0);
        assertTrue(sphere.getCurrentY() > initialY, "Sphere should fall downwards (Y increases).");
        
        double yAfterOneUpdate = sphere.getCurrentY();
        sphere.update(0);
        assertTrue(sphere.getCurrentY() > yAfterOneUpdate, "Sphere should continue to fall.");
    }

    @Test
    void testReset() {
        sphere.setTargetX(500);
        sphere.setZ(1000);
        sphere.startFalling();
        
        sphere.reset();
        
        assertEquals(0, sphere.getX());
        assertEquals(150, sphere.getCurrentY());
        assertEquals(0, sphere.getZ());
        assertFalse(sphere.isJumping());
    }
}
