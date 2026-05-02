package cz.matysekxx.beatbounce.controller;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.model.entity.Sphere;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class GameController implements MouseMotionListener {
    private final static int LANE_WIDTH = 120;
    private final static int MAX_LANE_ABS = 2 * LANE_WIDTH;
    
    private final Camera3D cam;
    private final Sphere sphere;

    public GameController(Camera3D cam, Sphere sphere) {
        this.cam = cam;
        this.sphere = sphere;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.mouseMoved(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        final int mouseX = e.getX();
        final int width = e.getComponent().getWidth();
        final double scale = cam.getScale(sphere.getZ());
        if (scale <= 0) return;
        final double rawTargetX = cam.getX() + (mouseX - width / 2.0) / scale;
        sphere.setTargetX(Math.max(-MAX_LANE_ABS, Math.min(MAX_LANE_ABS, rawTargetX)));
    }
}