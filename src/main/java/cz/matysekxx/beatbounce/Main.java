package cz.matysekxx.beatbounce;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.configuration.SwingConfiguration;

import javax.swing.*;

/**
 * The entry point for the BeatBounce application.
 * This class handles low-level system property configuration for rendering,
 * High DPI support, and bootstraps the application execution.
 */
public class Main {

    /**
     * Flag indicating if the current operating system is Windows.
     * Used for choosing appropriate rendering pipelines (Direct3D vs OpenGL).
     */
    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    /**
     * Flag indicating if the current operating system is macOS.
     * Used for enabling platform-specific UI features like the screen menu bar.
     */
    private static final boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");

    /**
     * Configures system properties for High DPI support on Windows and other platforms.
     * Ensures that the UI scales correctly on high-resolution displays.
     */
    static void setupHighDPI() {
        System.setProperty("sun.java2d.dpiaware", "true");
        System.setProperty("sun.java2d.uiScale", "1");
    }

    /**
     * Configures JVM system properties to optimize rendering based on OS and settings.
     * Enables hardware acceleration (OpenGL/Direct3D) and adjusts anti-aliasing.
     */
    static void setupRenderingProperties() {
        System.setProperty("sun.awt.noerasebackground", "true");
        System.setProperty("sun.java2d.noerasebackground", "true");
        System.setProperty("sun.java2d.erasedirtyregions", "false");
        System.setProperty("sun.java2d.transaccel", "true");

        if (Settings.opengl) {

            if (isWindows) {
                System.setProperty("sun.java2d.d3d", "true");
                System.setProperty("sun.java2d.opengl", "false");
            } else {
                System.setProperty("sun.java2d.opengl", "true");
                System.setProperty("sun.java2d.xrender", "true");
            }
            System.setProperty("sun.java2d.noddraw", "true");
        }

        System.setProperty("sun.awt.noerasebackground", "true");
        System.setProperty("sun.java2d.transaccel", "true");
        System.setProperty("sun.java2d.accthreshold", "0");
        System.setProperty("sun.java2d.pmoffscreen", "true");

        if (Settings.graphicsQuality.equals("LOW")) {
            System.setProperty("awt.useSystemAAFontSettings", "off");
            System.setProperty("swing.aatext", "false");
        } else {
            System.setProperty("awt.useSystemAAFontSettings", "lcd");
            System.setProperty("swing.aatext", "true");
        }

        if (isMac) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", "BeatBounce");
            System.setProperty("apple.awt.application.appearance", "system");
        }
    }

    /**
     * The primary entry point for the JVM.
     * Initializes the environment, loads settings, and starts the application orchestrator.
     */
    static void main() {
        setupHighDPI();
        Settings.load();

        setupRenderingProperties();

        SwingConfiguration.setup();

        SwingUtilities.invokeLater(Execute.getSingleton());
    }
}