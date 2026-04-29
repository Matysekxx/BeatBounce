package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ScrollBarUI extends BasicScrollBarUI {

    private static final int THUMB_WIDTH = 4;
    private static final int THUMB_ARC = 4;
    private static final Color THUMB_COLOR = new Color(0, 255, 255, 180);
    private static final Color THUMB_HOVER_COLOR = new Color(0, 255, 255, 255);
    private static final Color TRACK_COLOR = new Color(0, 255, 255, 20);

    private boolean hovered = false;

    private static JButton zeroButton() {
        JButton b = new JButton();
        Dimension zero = new Dimension(0, 0);
        b.setPreferredSize(zero);
        b.setMinimumSize(zero);
        b.setMaximumSize(zero);
        return b;
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        scrollbar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                scrollbar.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                scrollbar.repaint();
            }
        });
    }

    @Override
    protected void configureScrollBarColors() {
        trackColor = new Color(0, 0, 0, 0);
    }

    @Override
    protected JButton createDecreaseButton(int o) {
        return zeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int o) {
        return zeroButton();
    }

    @Override
    protected Dimension getMinimumThumbSize() {
        return new Dimension(THUMB_WIDTH, 32);
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle bounds) {
        Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphic2D(g2);

        int x = bounds.x + bounds.width - THUMB_WIDTH - 2;
        g2.setColor(TRACK_COLOR);
        g2.fillRoundRect(x, bounds.y, THUMB_WIDTH, bounds.height, THUMB_ARC, THUMB_ARC);

        g2.dispose();
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle bounds) {
        if (bounds.isEmpty() || !scrollbar.isEnabled()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphic2D(g2);

        int x = bounds.x + bounds.width - THUMB_WIDTH - 2;
        int y = bounds.y + 2;
        int h = Math.max(bounds.height - 4, 10);

        g2.setColor(hovered ? THUMB_HOVER_COLOR : THUMB_COLOR);
        g2.fillRoundRect(x, y, THUMB_WIDTH, h, THUMB_ARC, THUMB_ARC);

        g2.dispose();
    }
}