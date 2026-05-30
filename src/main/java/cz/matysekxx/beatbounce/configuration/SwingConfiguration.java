package cz.matysekxx.beatbounce.configuration;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Global UI configuration class for the Swing-based interface.
 * <p>
 * This class handles the initialization of the <b>Look and Feel</b> and
 * overrides standard {@link UIManager} properties to create a cohesive dark theme
 * across all application components.
 * </p>
 *
 * @author Matysekxx
 */
public class SwingConfiguration {
    /**
     * Initializes the application's look and feel and applies theme overrides.
     * <p>
     * This method sets FlatLaf Dark as a base
     * and then applies custom color properties to the {@link UIManager}.
     * </p>
     */
    public static void setup() {
        try {
            FlatDarkLaf.setup();
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
            UIManager.put("TitlePane.unifiedBackground", false);
            UIManager.put("TitlePane.background", new Color(30, 30, 30));
            UIManager.put("TitlePane.foreground", new Color(200, 200, 200));
        } catch (Exception _) {
        }
    }
}
