package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderUtils;

import javax.swing.*;
import java.awt.*;

public class CustomDialog extends JDialog {

    private final JPanel buttonPanel;

    public CustomDialog(Frame owner, String titleText, String message, Color borderColor) {
        super(owner, titleText, true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        final DialogContentPane dialogContentPane = new DialogContentPane(borderColor);
        final JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        dialogContentPane.add(titleLabel, BorderLayout.NORTH);

        final JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        messageLabel.setForeground(new Color(200, 200, 200));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dialogContentPane.add(messageLabel, BorderLayout.CENTER);

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        dialogContentPane.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(dialogContentPane);
    }

    public void addButton(JButton button) {
        buttonPanel.add(button);
    }

    private static class DialogContentPane extends JPanel {
        private final Color borderColor;

        private DialogContentPane(Color borderColor) {
            this.borderColor = borderColor;
            this.setOpaque(false);
            this.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
            this.setLayout(new BorderLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            RenderUtils.initGraphics2D(g2d);
            g2d.setColor(new Color(15, 15, 25, 240));
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
            g2d.dispose();
        }
    }
}