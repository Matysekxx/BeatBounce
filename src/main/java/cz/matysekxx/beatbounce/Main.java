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

    static void setTestingProperties() {
        System.setProperty("sun.java2d.trace", "count");
    }

    static void main() {
        setProperties();
        //setTestingProperties();
        SwingUtilities.invokeLater(Execute.getSingleton());
    }
}