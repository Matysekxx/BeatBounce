package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.audio.AudioManager;

import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import java.awt.*;

/**
 * A stylized {@link JCheckBox} with a custom toggle switch icon.
 * Features a rounded track and a sliding knob with smooth color transitions.
 *
 * @author Matysekxx
 */
public class CustomCheckBox extends JCheckBox {

    /**
     * Constructs a new CustomCheckBox with the specified text and initial selection state.
     *
     * @param text     the text to display next to the checkbox
     * @param selected the initial selection state
     */
    public CustomCheckBox(String text, boolean selected) {
        super(text, selected);
        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.LIGHT_GRAY);
        setFont(UIScale.scaleFont(RenderCache.SANS_PLAIN_20));
        setAlignmentX(LEFT_ALIGNMENT);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addActionListener(_ -> {
            AudioManager.playSFX("/click-sound.mp3");
            setForeground(isSelected() ? Color.WHITE : Color.LIGHT_GRAY);
        });
        setIcon(new CheckBoxIcon());
    }

    /**
     * Overrides preferred size to ensure the full text and custom icon fit.
     * This prevents truncation with ellipsis (...) in constrained layouts.
     */
    @Override
    public Dimension getPreferredSize() {
        final Dimension base = super.getPreferredSize();
        final FontMetrics fm = getFontMetrics(getFont());
        final int textWidth = fm.stringWidth(getText());
        final int iconWidth = getIcon().getIconWidth();
        final int gap = getIconTextGap();
        final int totalWidth = iconWidth + gap + textWidth + UIScale.scale(10);
        final int totalHeight = Math.max(base.height, getIcon().getIconHeight() + UIScale.scale(4));
        return new Dimension(totalWidth, totalHeight);
    }

    /**
     * A custom icon implementation that renders a toggle switch.
     */
    private class CheckBoxIcon implements Icon {
        /**
         * Paints the custom toggle switch icon.
         *
         * @param c the component to paint on
         * @param g the graphics context
         * @param x the x-coordinate
         * @param y the y-coordinate
         */
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            RenderUtils.initGraphics2D(g2);
            g2.setColor(isSelected() ? RenderUtils.cyan : new Color(60, 60, 65));
            g2.fillRoundRect(x, y + UIScale.scale(2), UIScale.scale(40), UIScale.scale(20), UIScale.scale(20), UIScale.scale(20));
            g2.setColor(Color.WHITE);
            final int knobX = isSelected() ? x + UIScale.scale(22) : x + UIScale.scale(2);
            g2.fillOval(knobX, y + UIScale.scale(4), UIScale.scale(16), UIScale.scale(16));
            g2.dispose();
        }

        /**
         * Returns the width of the custom toggle switch icon.
         *
         * @return the icon width in scaled pixels
         */
        @Override
        public int getIconWidth() {
            return UIScale.scale(55);
        }

        /**
         * Returns the height of the custom toggle switch icon.
         *
         * @return the icon height in scaled pixels
         */
        @Override
        public int getIconHeight() {
            return UIScale.scale(26);
        }
    }
}