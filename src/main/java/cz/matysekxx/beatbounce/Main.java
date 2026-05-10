package cz.matysekxx.beatbounce;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.configuration.SwingConfiguration;

import javax.swing.*;

public class Main {

    static void setupHighDPI() {
        System.setProperty("sun.java2d.dpiaware", "true");
        System.setProperty("sun.java2d.uiScale.enabled", "false");
        System.setProperty("sun.java2d.uiScale", "1");
    }

    static void setupRenderingProperties() {
        String os = System.getProperty("os.name").toLowerCase();
        final boolean isWindows = os.contains("win");
        final boolean isMac = os.contains("mac");
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

    static void main() {
        setupHighDPI();
        SwingConfiguration.setup();
        Settings.load();
        setupRenderingProperties();

        SwingUtilities.invokeLater(Execute.getSingleton());
    }
}