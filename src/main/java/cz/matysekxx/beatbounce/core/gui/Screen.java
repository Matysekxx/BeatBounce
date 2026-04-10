package cz.matysekxx.beatbounce.core.gui;

import javax.swing.*;
import java.awt.*;

public abstract class Screen extends JFrame {

    public Screen() {
        this.setTitle("BeatBounce");
        this.setBackground(Color.BLACK);
        this.setSize(800, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);
    }
}
