package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import javax.swing.*;
import java.awt.*;

public class SidebarPanel extends JPanel {

    public SidebarPanel() {
        this.setPreferredSize(new Dimension(280, 0));
        this.setOpaque(false);
    }

    protected void paintComponent(Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphic2D(g2);
        g2.setColor(new Color(10, 10, 14, 210));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(new Color(255, 255, 255, 20));
        g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
        g2.dispose();
    }
}
