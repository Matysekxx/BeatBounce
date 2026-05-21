package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;

/**
 * A stylized {@link JSlider} with a custom UI.
 * Features a rounded track and a custom thumb with glow effects.
 */
public class CustomSlider extends JSlider {
    private final int w;
    private final int h;

    private final int thumbSize;


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
        thumbSize = UIScale.scale(24);
        w = UIScale.scale(350);
        h = UIScale.scale(55);
        setMinimumSize(new Dimension(w, h));
        setPreferredSize(new Dimension(w, h));
        setMaximumSize(new Dimension(w, h));
        setUI(new CustomSliderUI(this));
    }

    private class CustomSliderUI extends BasicSliderUI {
        /**
         * Constructs a {@code BasicSliderUI}.
         *
         * @param b a slider
         */
        public CustomSliderUI(JSlider b) {
            super(b);
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return new Dimension(w, h);
        }

        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RenderUtils.initGraphics2D(g2);
            Rectangle t = trackRect;
            final int trackH = UIScale.scale(8);
            final int ty = t.y + t.height / 2 - trackH / 2;

            g2.setColor(new Color(40, 40, 45));
            g2.fillRoundRect(t.x, ty, t.width, trackH, trackH, trackH);
            g2.setColor(RenderUtils.cyan);
            final int fillWidth = thumbRect.x + thumbRect.width / 2 - t.x;
            if (fillWidth > 0) {
                g2.fillRoundRect(t.x, ty, fillWidth, trackH, trackH, trackH);
            }
            g2.dispose();
        }

        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RenderUtils.initGraphics2D(g2);
            Rectangle t = thumbRect;
            final int innerSize = UIScale.scale(12);
            g2.setColor(RenderUtils.cyan);
            g2.fillOval(t.x, t.y + t.height / 2 - thumbSize / 2, thumbSize, thumbSize);
            g2.setColor(Color.WHITE);
            g2.fillOval(t.x + (thumbSize - innerSize) / 2, t.y + t.height / 2 - innerSize / 2, innerSize, innerSize);
            g2.dispose();
        }

        @Override
        protected Dimension getThumbSize() {
            return new Dimension(thumbSize, thumbSize);
        }

    }
}
