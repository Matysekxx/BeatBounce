package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;

import java.awt.*;

public class SimulatedButton {
    private static final Color HINT_BG = new Color(255, 255, 255, 15);
    private static final Color HINT_BORDER = new Color(255, 255, 255, 30);
    private static final Color HINT_LABEL = new Color(220, 220, 220);

    private final String label;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final UIAction action;

    public SimulatedButton(String label, int x, int y, int width, int height, UIAction action) {
        this.label = label;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.action = action;
    }

    public boolean contains(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    public UIAction getAction() {
        return action;
    }

    public void draw(Graphics2D g2d, int mouseX, int mouseY, int translateY) {
        g2d.setFont(RenderCache.SANS_BOLD_18);
        final int labelW = g2d.getFontMetrics().stringWidth(label);

        final boolean hovered = contains(mouseX, mouseY - translateY);

        if (hovered) {
            g2d.setColor(new Color(255, 255, 255, 40));
        } else {
            g2d.setColor(HINT_BG);
        }
        g2d.fillRoundRect(x, y, width, height, 20, 20);

        g2d.setColor(hovered ? RenderUtils.cyan : HINT_BORDER);
        g2d.setStroke(hovered ? RenderCache.STROKE_2 : RenderCache.STROKE_1);
        g2d.drawRoundRect(x, y, width, height, 20, 20);
        g2d.setStroke(RenderCache.STROKE_1);

        g2d.setColor(hovered ? Color.WHITE : HINT_LABEL);
        g2d.drawString(label, x + (width - labelW) / 2, y + height / 2 + 5);
    }
}
