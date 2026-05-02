package cz.matysekxx.beatbounce;

import javax.swing.*;

public class Main {
    static void setProperties() {
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.java2d.noddraw", "true");
        System.setProperty("sun.awt.noerasebackground", "true");
        System.setProperty("sun.java2d.pmoffscreen", "false");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
    }

    static void main() {
        setProperties();
        SwingUtilities.invokeLater(Execute.getSingleton());
    }
}