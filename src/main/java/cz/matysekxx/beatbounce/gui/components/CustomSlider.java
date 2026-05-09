package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import javax.swing.*;
import java.awt.*;

/**
 * A stylized {@link JSlider} with a custom UI.
 * Features a rounded track and a custom thumb with glow effects.
 */
public class CustomSlider extends JSlider {

    /**
     * Constructs a new CustomSlider with the specified range and initial value.
     *
     * @param min the minimum value of the slider
     * @param max the maximum value of the slider
     * @param val the initial value of the slider
     */
    public CustomSlider(int min, int max, int val) {
        super(min, max, val);
        setOpaque(false);
        setFocusable(false);
        setMinimumSize(new Dimension(350, 55));
        setPreferredSize(new Dimension(350, 55));
        setMaximumSize(new Dimension(350, 55));
        setUI(new javax.swing.plaf.basic.BasicSliderUI(this) {
            @Override
            public Dimension getPreferredSize(JComponent c) {
                return new Dimension(350, 55);
            }

            @Override
            public Dimension getMinimumSize(JComponent c) {
                return new Dimension(350, 55);
            }

            @Override
            public Dimension getMaximumSize(JComponent c) {
                return new Dimension(350, 55);
            }

            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphics2D(g2);
                Rectangle t = trackRect;
                g2.setColor(new Color(40, 40, 45));
                g2.fillRoundRect(t.x, t.y + t.height / 2 - 4, t.width, 8, 8, 8);
                g2.setColor(RenderUtils.cyan);
                int width = thumbRect.x - t.x;
                if (width > 0) {
                    g2.fillRoundRect(t.x, t.y + t.height / 2 - 4, width, 8, 8, 8);
                }
                g2.dispose();
            }

            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphics2D(g2);
                Rectangle t = thumbRect;
                g2.setColor(RenderUtils.cyan);
                g2.fillOval(t.x, t.y + t.height / 2 - 12, 24, 24);
                g2.setColor(Color.WHITE);
                g2.fillOval(t.x + 6, t.y + t.height / 2 - 6, 12, 12);
                g2.dispose();
            }
        });
    }
}
