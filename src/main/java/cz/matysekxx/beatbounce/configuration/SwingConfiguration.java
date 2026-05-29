package cz.matysekxx.beatbounce.configuration;

import javax.swing.*;
import java.awt.*;

/**
 * Global UI configuration class for the Swing-based interface.
 * <p>
 * This class handles the initialization of the <b>Look and Feel</b> and
 * overrides standard {@link UIManager} properties to create a cohesive dark theme
 * across all application components.
 * </p>
 */
public class SwingConfiguration {

    /**
     * Primary text color (Light Grey) to ensure high readability on dark backgrounds.
     */
    private static final Color lightText = new Color(220, 220, 220);

    /**
     * Accent color (Soft Blue) used for selection highlights and focus indicators.
     */
    private static final Color accentBlue = new Color(97, 175, 239);

    /**
     * Default background color for button components.
     */
    private static final Color buttonBg = new Color(60, 65, 75);

    /**
     * Initializes the application's look and feel and applies theme overrides.
     * <p>
     * This method sets FlatLaf Dark as a base
     * and then applies custom color properties to the {@link UIManager} for:
     * </p>
     * <ul>
     *   <li>Panels and Labels</li>
     *   <li>Buttons and Text Fields</li>
     *   <li>Lists and Combo Boxes</li>
     *   <li>File Chooser dialogs</li>
     * </ul>
     */
    public static void setup() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            UIManager.put("TitlePane.background", Color.BLACK);
            UIManager.put("TitlePane.foreground", Color.WHITE);
            UIManager.put("TitlePane.buttonHoverBackground", new Color(40, 40, 40));
            UIManager.put("TitlePane.buttonPressedBackground", new Color(60, 60, 60));

            setupButtonProperties();
            setupComboBoxProperties();
        } catch (Exception _) {
        }
    }

    /**
     * Configures standard button colors.
     */
    private static void setupButtonProperties() {
        UIManager.put("Button.foreground", Color.WHITE);
    }

    /**
     * Configures JComboBox and its dropdown selection colors.
     */
    private static void setupComboBoxProperties() {
        UIManager.put("ComboBox.background", buttonBg);
        UIManager.put("ComboBox.foreground", lightText);
        UIManager.put("ComboBox.selectionBackground", accentBlue);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
    }
}
