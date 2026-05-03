package cz.matysekxx.beatbounce;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.configuration.SwingConfiguration;

import javax.swing.*;

public class Main {
    static void setProperties() {
        System.setProperty("sun.java2d.opengl", Settings.opengl ? "true" : "false");
        System.setProperty("sun.java2d.noddraw", Settings.opengl ? "true" : "false");
        System.setProperty("sun.awt.noerasebackground", "true");
        System.setProperty("sun.java2d.pmoffscreen", "false");
        System.setProperty("awt.useSystemAAFontSettings", Settings.graphicsQuality.equals("LOW") ? "off" : "on");
        System.setProperty("swing.aatext", Settings.graphicsQuality.equals("LOW") ? "false" : "true");
    }

    static void main() {
        SwingConfiguration.setup();
        Settings.load();
        setProperties();
        SwingUtilities.invokeLater(Execute.getSingleton());
    }
}