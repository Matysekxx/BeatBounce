package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import javax.swing.*;
import java.awt.*;


public class FlatButton extends JButton {
    private final boolean outlined;
    private Color fill;
    private Color textColor;
    private float pulseFactor = 0f;

    public FlatButton(String text, Color fill, Color textColor, boolean outlined) {
        super(text);
        this.fill = fill;
        this.textColor = textColor;
        this.outlined = outlined;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void setFill(Color fill) {
        this.fill = fill;
        repaint();
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
        repaint();
    }

    public void setPulseFactor(float pulseFactor) {
        this.pulseFactor = pulseFactor;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphic2D(g2);
        int w = getWidth();
        int h = getHeight();
        int arc = 40;

        if (!outlined && isVisible()) {
            float spread = 12f * (float) (Math.sin(pulseFactor * Math.PI) + 1.0) / 2.0f;
            g2.setColor(new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 150));
            g2.fillRoundRect((int) (-spread / 2), (int) (-spread / 2),
                    (int) (w + spread), (int) (h + spread),
                    arc + (int) spread, arc + (int) spread);
        }

        if (outlined) {
            g2.setColor(new Color(fill.getRed(), fill.getGreen(), fill.getBlue(),
                    getModel().isRollover() ? 40 : 10));
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.setColor(fill);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
        } else {
            g2.setColor(getModel().isRollover() ? fill.brighter() : fill);
            g2.fillRoundRect(0, 0, w, h, arc, arc);
        }

        FontMetrics fm = g2.getFontMetrics(getFont());
        int tx = (w - fm.stringWidth(getText())) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();

        g2.setColor(textColor);
        g2.drawString(getText(), tx, ty);
        g2.dispose();
    }
}