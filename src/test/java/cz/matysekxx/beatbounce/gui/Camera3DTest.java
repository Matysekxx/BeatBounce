package cz.matysekxx.beatbounce.gui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Camera3DTest {

    @Test
    void testGetDistanceTo() {
        Camera3D cam = new Camera3D(0, 0, -500, 500);
        assertEquals(500, cam.getDistanceTo(0));
        assertEquals(-100, cam.getDistanceTo(-600));
    }

    @Test
    void testGetScaleDecreasesWithDistance() {
        Camera3D cam = new Camera3D(0, 0, -500, 500);
        
        double scaleClose = cam.getScale(0);
        double scaleFar = cam.getScale(1000);
        
        assertTrue(scaleClose > scaleFar, "Scale should be larger for closer objects.");
        assertTrue(scaleFar > 0, "Scale should be positive for objects in front of camera.");
    }

    @Test
    void testGetScaleBehindCamera() {
        Camera3D cam = new Camera3D(0, 0, -500, 500);
        double scaleBehind = cam.getScale(-600);
        assertEquals(500, scaleBehind);
    }
}
