package cz.matysekxx.beatbounce.gui;

public class Star {
    public double x;
    public double y;
    public double z;

    public Star() {
        reset(true);
    }

    public void reset(boolean randomZ) {
        x = (Math.random() - 0.5) * 2000;
        y = (Math.random() - 0.5) * 2000;
        z = randomZ ? Math.random() * 500 : 500;
    }

    public void update() {
        z -= 3.0;
        if (z <= 1) reset(false);
    }
}