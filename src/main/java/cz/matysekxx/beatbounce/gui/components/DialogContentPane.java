package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;

import javax.swing.*;
import java.awt.*;

public class DialogContentPane extends JPanel {
    private final Color borderColor;

    public DialogContentPane(Color borderColor) {
        this.borderColor = borderColor;
        this.setOpaque(false);
        this.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        this.setLayout(new BorderLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2d);
        g2d.setColor(new Color(15, 15, 25, 240));
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        g2d.setColor(borderColor);
        g2d.setStroke(RenderCache.STROKE_2);
        g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
        g2d.dispose();
    }
}