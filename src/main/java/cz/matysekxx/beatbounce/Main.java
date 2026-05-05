package cz.matysekxx.beatbounce;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.configuration.SwingConfiguration;

import javax.swing.*;

/**
 * The entry point of the BeatBounce application.
 *
 * This class handles the initial bootstrapping of the application, including
 * loading configurations, setting up low-level JVM rendering properties,
 * and launching the UI on the Event Dispatch Thread (EDT).
 */
public class Main {

    /**
     * Configures low-level JVM system properties to optimize rendering.
     *
     * The properties are set based on the current {@link Settings}, specifically
     * affecting OpenGL acceleration and text anti-aliasing.
     *
     * | Property | Description |
     * | :--- | :--- |
     * | {@code sun.java2d.opengl} | Enables/Disables OpenGL hardware acceleration. |
     * | {@code sun.java2d.noddraw} | Disables DirectDraw to avoid conflicts with OpenGL. |
     * | {@code sun.awt.noerasebackground} | Prevents flickering by not clearing the background. |
     * | {@code awt.useSystemAAFontSettings} | Controls system-level font anti-aliasing. |
     */
    static void setupProperties() {
        System.setProperty("sun.java2d.opengl", Settings.opengl ? "true" : "false");
        System.setProperty("sun.java2d.noddraw", Settings.opengl ? "true" : "false");
        System.setProperty("sun.awt.noerasebackground", "true");
        System.setProperty("sun.java2d.pmoffscreen", "false");
        System.setProperty("awt.useSystemAAFontSettings", Settings.graphicsQuality.equals("LOW") ? "off" : "on");
        System.setProperty("swing.aatext", Settings.graphicsQuality.equals("LOW") ? "false" : "true");
    }

    /**
     * The main entry method that starts the application.
     *
     * The startup sequence is as follows:
     * 1. Initialize Swing Look and Feel via {@link SwingConfiguration}.
     * 2. Load user settings from {@link Settings}.
     * 3. Apply JVM rendering properties.
     * 4. Hand over execution to {@link Execute} on the Swing Event Dispatch Thread.
     */
    static void main() {
        SwingConfiguration.setup();
        Settings.load();
        setupProperties();
        SwingUtilities.invokeLater(Execute.getSingleton());
    }
}