package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;
import javax.swing.*;
import java.awt.*;

/**
 * A panel that displays the available skins for the game.
 */
public class SkinsPanel extends JPanel {
    /**
     * Constructs a new SkinsPanel.
     */
    public SkinsPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        JLabel title = new JLabel("SKINS");
        title.setFont(new Font("SansSerif", Font.BOLD, 56));
        title.setForeground(RenderUtils.cyan);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(40, 0, 30, 0));
        add(title, BorderLayout.NORTH);
    }

    /**
     * Paints the skins panel background with a gradient and rounded corners.
     *
     * @param g the graphics context to paint on
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2);
        g2.setPaint(new LinearGradientPaint(0, 0, getWidth(), getHeight(),
            new float[]{0f, 1f},
            new Color[]{new Color(15, 15, 35, 180), new Color(10, 10, 25, 100)}));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
        g2.setColor(new Color(0, 255, 255, 30));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
        g2.dispose();
        super.paintComponent(g);
    }
}