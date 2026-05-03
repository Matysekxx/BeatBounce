package cz.matysekxx.beatbounce.configuration;

import javax.swing.*;
import java.awt.*;

public class SwingConfiguration {
    private static final Color mainBg = new Color(40, 44, 52);
    private static final Color listBg = new Color(33, 37, 43);
    private static final Color lightText = new Color(220, 220, 220);
    private static final Color accentBlue = new Color(97, 175, 239);
    private static final Color buttonBg = new Color(60, 65, 75);
    public static void setup() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        UIManager.put("Panel.background", mainBg);
        UIManager.put("Label.foreground", lightText);
        setupButtonProperties();
        setupTextFieldProperties();
        setupListProperties();
        setupComboBoxProperties();
        setupFileChooserProperties();
    }

    private static void setupButtonProperties() {
        UIManager.put("Button.background", buttonBg);
        UIManager.put("Button.foreground", Color.WHITE);
    }

    private static void setupTextFieldProperties() {
        UIManager.put("TextField.background", listBg);
        UIManager.put("TextField.foreground", lightText);
        UIManager.put("TextField.caretForeground", lightText);
    }

    private static void setupListProperties() {
        UIManager.put("List.background", listBg);
        UIManager.put("List.foreground", lightText);
        UIManager.put("List.selectionBackground", accentBlue);
        UIManager.put("List.selectionForeground", Color.WHITE);
    }

    private static void setupComboBoxProperties() {
        UIManager.put("ComboBox.background", buttonBg);
        UIManager.put("ComboBox.foreground", lightText);
        UIManager.put("ComboBox.selectionBackground", accentBlue);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
    }

    private static void setupFileChooserProperties() {
        UIManager.put("FileChooser.background", mainBg);
        UIManager.put("FileChooser.foreground", lightText);
        UIManager.put("FileChooser.listViewBackground", listBg);
        UIManager.put("FileChooser.listViewForeground", lightText);
        UIManager.put("FileChooser.openButtonText", "Open");
        UIManager.put("FileChooser.saveButtonText", "Save");
        UIManager.put("FileChooser.cancelButtonText", "Cancel");
        UIManager.put("FileChooser.fileNameLabelText", "File Name:");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Files of Type:");
        UIManager.put("FileChooser.lookInLabelText", "Look In:");
        UIManager.put("FileChooser.upFolderToolTipText", "Up One Level");
        UIManager.put("FileChooser.homeFolderToolTipText", "Home");
        UIManager.put("FileChooser.newFolderToolTipText", "Create New Folder");
        UIManager.put("FileChooser.listViewButtonToolTipText", "List View");
        UIManager.put("FileChooser.detailsViewButtonToolTipText", "Details View");
    }
}