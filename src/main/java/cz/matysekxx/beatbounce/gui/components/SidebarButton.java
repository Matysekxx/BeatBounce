package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

public class SidebarButton extends JButton {
    private boolean active;

    public SidebarButton(String text, String targetCard, boolean defaultActive, Consumer<String> cardSwitcher) {
        this(text, targetCard, defaultActive, cardSwitcher, null);
    }

    public SidebarButton(String text, String targetCard, boolean defaultActive, Consumer<String> cardSwitcher, ActionListener extraAction) {
        super(text);
        this.active = defaultActive;
        setupStyle();

        addActionListener(e -> {
            if (targetCard != null) {
                Container parent = getParent();
                if (parent != null) {
                    for (Component c : parent.getComponents()) {
                        if (c instanceof SidebarButton) ((SidebarButton) c).setActive(false);
                    }
                }
                setActive(true);
                if (cardSwitcher != null) cardSwitcher.accept(targetCard);
            }
            if (extraAction != null) extraAction.actionPerformed(e);
        });
    }

    private void setupStyle() {
        setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 16));
        setForeground(active ? Color.WHITE : new Color(180, 180, 180));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setHorizontalAlignment(SwingConstants.LEFT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        setBorder(new EmptyBorder(0, 20, 0, 0));
    }

    public void setActive(boolean active) {
        this.active = active;
        setupStyle();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (getModel().isRollover() || active) {
            Graphics2D g2 = (Graphics2D) g.create();
            RenderUtils.initGraphic2D(g2);
            g2.setColor(new Color(255, 255, 255, active ? 25 : 10));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            if (active) {
                g2.setColor(RenderUtils.cyan);
                g2.fillRoundRect(0, 10, 4, getHeight() - 20, 4, 4);
            }
            g2.dispose();
        }
        super.paintComponent(g);
    }
}