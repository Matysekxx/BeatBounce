package cz.matysekxx.beatbounce.util;

import cz.matysekxx.beatbounce.Execute;
import cz.matysekxx.beatbounce.gui.ButtonFactory;
import cz.matysekxx.beatbounce.gui.components.CustomDialog;
import cz.matysekxx.beatbounce.gui.screen.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Centralized exception handling utility that logs errors and displays them to the player.
 */
public class ExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

    /**
     * Handles an exception by logging it and showing a custom in-game dialog.
     *
     * @param message a user-friendly message describing the context of the error
     * @param t       the throwable that occurred
     */
    public static void handle(String message, Throwable t) {
        LOG.error(message, t);

        SwingUtilities.invokeLater(() -> {
            final Screen activeScreen = Execute.getSingleton().getScreenManager().getActiveScreen();
            if (activeScreen != null) {
                final CustomDialog dialog = new CustomDialog(
                        activeScreen,
                        "Oops! Something went wrong",
                        message + (t != null ? ": " + t.getMessage() : ""),
                        new Color(255, 50, 50)
                );

                final JButton closeButton = ButtonFactory.createSecondaryButton("Close", (e) -> dialog.dispose());
                dialog.addButton(closeButton);

                dialog.pack();
                dialog.setLocationRelativeTo(activeScreen);
                dialog.setVisible(true);
            }
        });
    }

    /**
     * Handles an error message without a specific throwable.
     *
     * @param message the error message to display
     */
    public static void handle(String message) {
        handle(message, null);
    }
}
