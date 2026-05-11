package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;

import javax.swing.*;
import java.awt.*;

/**
 * A custom undecorated {@link JDialog} with a stylized background and border.
 * Provides a consistent look for in-game dialogs.
 */
public class CustomDialog extends JDialog {

    private final JPanel buttonPanel;

    /**
     * Constructs a new CustomDialog.
     *
     * @param owner       the {@link Frame} from which the dialog is displayed
     * @param titleText   the title text shown at the top of the dialog
     * @param message     the message text shown in the center
     * @param borderColor the color used for the dialog's border
     */
    public CustomDialog(Frame owner, String titleText, String message, Color borderColor) {
        super(owner, titleText, true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        final DialogContentPane dialogContentPane = new DialogContentPane(borderColor);
        final JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(RenderCache.SANS_BOLD_28);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        dialogContentPane.add(titleLabel, BorderLayout.NORTH);

        final JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(RenderCache.SANS_PLAIN_18);
        messageLabel.setForeground(new Color(200, 200, 200));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dialogContentPane.add(messageLabel, BorderLayout.CENTER);

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        dialogContentPane.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(dialogContentPane);
    }

    /**
     * Adds a button to the dialog's button panel.
     *
     * @param button the {@link JButton} to add
     */
    public void addButton(JButton button) {
        buttonPanel.add(button);
    }
}