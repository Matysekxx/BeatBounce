package cz.matysekxx.beatbounce.controller;

import cz.matysekxx.beatbounce.gui.Camera3D;
import cz.matysekxx.beatbounce.model.entity.Sphere;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class GameController extends KeyAdapter implements MouseMotionListener {
    private final static int LANE_WIDTH = 120;
    private final static int MAX_LANE_ABS = 2 * LANE_WIDTH;
    
    private final Camera3D cam;
    private final Sphere sphere;
    private boolean lastInputWasMouse = false;

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
        this.lastInputWasMouse = true;
        final int mouseX = e.getX();
        final int width = e.getComponent().getWidth();
        final double scale = cam.getScale(sphere.getZ());
        if (scale <= 0) return;
        final double rawTargetX = cam.getX() + (mouseX - width / 2.0) / scale;
        sphere.setTargetX(Math.max(-MAX_LANE_ABS, Math.min(MAX_LANE_ABS, rawTargetX)));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (this.lastInputWasMouse) {
            sphere.setTargetX(snapToNearestLane(sphere.getTargetX()));
            this.lastInputWasMouse = false;
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> sphere.setTargetX(Math.max(-MAX_LANE_ABS, sphere.getTargetX() - LANE_WIDTH));
            case KeyEvent.VK_RIGHT -> sphere.setTargetX(Math.min(MAX_LANE_ABS, sphere.getTargetX() + LANE_WIDTH));
        }
    }

    private double snapToNearestLane(double x) {
        return Math.max(-MAX_LANE_ABS, Math.min(MAX_LANE_ABS, Math.round(x / LANE_WIDTH) * LANE_WIDTH));
    }
}