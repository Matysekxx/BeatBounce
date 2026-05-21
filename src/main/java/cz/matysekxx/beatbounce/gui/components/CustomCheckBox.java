package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.audio.AudioManager;

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
        setFont(RenderCache.SANS_PLAIN_20);
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
        private final static int iconWidth = 55;
        private final static int iconHeight = 26;
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            RenderUtils.initGraphics2D(g2);
            g2.setColor(isSelected() ? RenderUtils.cyan : new Color(60, 60, 65));
            g2.fillRoundRect(x, y + 2, 40, 20, 20, 20);
            g2.setColor(Color.WHITE);
            final int knobX = isSelected() ? x + 22 : x + 2;
            g2.fillOval(knobX, y + 4, 16, 16);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return iconWidth;
        }

        @Override
        public int getIconHeight() {
            return iconHeight;
        }
    }
}