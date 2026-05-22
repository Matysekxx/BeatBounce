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
     * Configures system properties for High DPI support on Windows and other platforms.
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
        String os = System.getProperty("os.name").toLowerCase();
        final boolean isWindows = os.contains("win");
        final boolean isMac = os.contains("mac");
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
     * Main method that initializes settings, rendering, and starts the Swing event loop.
     */
    static void main() {
        setupHighDPI();
        Settings.load();

        setupRenderingProperties();

        SwingConfiguration.setup();

        SwingUtilities.invokeLater(Execute.getSingleton());
    }
}