package cz.matysekxx.beatbounce;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.configuration.SwingConfiguration;

import javax.swing.*;

/**
 * The entry point of the BeatBounce application.
 * <p>
 * This class handles the initial bootstrapping of the application, including
 * loading configurations, setting up low-level JVM rendering properties,
 * and launching the UI on the Event Dispatch Thread (EDT).
 */
public class Main {

    /**
     * Configures the DPI awareness properties.
     */
    static void setupHighDPI() {
        System.setProperty("sun.java2d.dpiaware", "true");
        System.setProperty("sun.java2d.uiScale.enabled", "false");
        System.setProperty("sun.java2d.uiScale", "1");
    }

    /**
     * Configures low-level JVM system properties to optimize rendering.
     * <p>
     * The properties are set based on the current {@link Settings}, specifically
     * affecting OpenGL acceleration and text antialiasing.
     * <p>
     * {@code sun.java2d.opengl}  Enables/Disables OpenGL hardware acceleration. <p>
     * {@code sun.java2d.noddraw}  Disables DirectDraw to avoid conflicts with OpenGL. <p>
     * {@code sun.awt.noerasebackground}  Prevents flickering by not clearing the background. <p>
     * {@code awt.useSystemAAFontSettings}  Controls system-level font antialiasing. <p>
     */
    static void setupRenderingProperties() {
        System.setProperty("sun.java2d.opengl", Settings.opengl ? "true" : "false");
        System.setProperty("sun.java2d.noddraw", Settings.opengl ? "true" : "false");
        System.setProperty("sun.awt.noerasebackground", "true");
        System.setProperty("sun.java2d.pmoffscreen", "false");
        System.setProperty("awt.useSystemAAFontSettings", Settings.graphicsQuality.equals("LOW") ? "off" : "on");
        System.setProperty("swing.aatext", Settings.graphicsQuality.equals("LOW") ? "false" : "true");
    }

    /**
     * The main entry method that starts the application.
     * <p>
     * The startup sequence is as follows: <p>
     * 1. Initialize Swing Look and Feel via {@link SwingConfiguration}.<p>
     * 2. Load user settings from {@link Settings}.<p>
     * 3. Apply JVM rendering properties.<p>
     * 4. Hand over execution to {@link Execute} on the Swing Event Dispatch Thread.<p>
     */
    static void main() {
        setupHighDPI();
        SwingConfiguration.setup();
        Settings.load();
        setupRenderingProperties();
        SwingUtilities.invokeLater(Execute.getSingleton());
    }
}