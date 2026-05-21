package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;

import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import java.awt.*;

/**
 * A custom content pane for dialogs, featuring a rounded, semi-transparent background and a colored border.
 */
public class DialogContentPane extends JPanel {
    /**
     * The color used for the component's border.
     */
    private final Color borderColor;

    /**
     * Constructs a new DialogContentPane.
     *
     * @param borderColor the color of the border
     */
    public DialogContentPane(Color borderColor) {
        this.borderColor = borderColor;
        this.setOpaque(false);
        this.setBorder(BorderFactory.createEmptyBorder(UIScale.scale(30), UIScale.scale(40), UIScale.scale(30), UIScale.scale(40)));
        this.setLayout(new BorderLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2d);
        g2d.setColor(new Color(15, 15, 25, 240));
        final int arc = UIScale.scale(20);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2d.setColor(borderColor);
        g2d.setStroke(RenderCache.STROKE_2);
        g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
        g2d.dispose();
    }
}
