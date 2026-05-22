package cz.matysekxx.beatbounce.gui;

import cz.matysekxx.beatbounce.util.UIScale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Camera3DTest {

    @BeforeEach
    void setUp() {
        UIScale.update(1920, 1080);
    }

    @Test
    void testGetDistanceTo() {
        final Camera3D cam = new Camera3D(0, 0, -500, 500);
        assertEquals(500, cam.getDistanceTo(0), 0.001);
        assertEquals(-100, cam.getDistanceTo(-600), 0.001);
    }

    @Test
    void testGetScaleDecreasesWithDistance() {
        final Camera3D cam = new Camera3D(0, 0, -500, 500);

        final double scaleClose = cam.getScale(0);
        final double scaleFar = cam.getScale(1000);

        assertTrue(scaleClose > scaleFar, "Scale should be larger for closer objects.");
        assertTrue(scaleFar > 0, "Scale should be positive for objects in front of camera.");
    }

    @Test
    void testGetScaleBehindCamera() {
        final Camera3D cam = new Camera3D(0, 0, -500, 500);
        final double scaleBehind = cam.getScale(-600);
        assertEquals(500, scaleBehind, 0.001);
    }
}
