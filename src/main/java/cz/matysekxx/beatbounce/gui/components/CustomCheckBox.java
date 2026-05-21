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
     * A custom icon implementation that renders a toggle switch.
     */
    private class CheckBoxIcon implements Icon {
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

        @Override
        public int getIconWidth() {
            return UIScale.scale(55);
        }

        @Override
        public int getIconHeight() {
            return UIScale.scale(26);
        }
    }
}