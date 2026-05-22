package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;
import cz.matysekxx.beatbounce.model.audio.AudioManager;
import cz.matysekxx.beatbounce.system.FileSystem;
import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * A panel that allows users to configure various game settings.
 * It includes options for display, graphics, audio, and gameplay.
 */
public class SettingsPanel extends JPanel {

    /**
     * Manager used for applying settings and switching screens.
     */
    private final ScreenManager screenManager;

    /**
     * Checkbox for toggling fullscreen mode.
     */
    private final JCheckBox fullscreenCheck;

    /**
     * Checkbox for toggling OpenGL hardware acceleration.
     */
    private final JCheckBox openglCheck;

    /**
     * Checkbox for toggling vertical synchronization.
     */
    private final JCheckBox vsyncCheck;

    /**
     * Checkbox for toggling the FPS overlay visibility.
     */
    private final JCheckBox showFpsCheck;

    /**
     * Checkbox for toggling background particle effects.
     */
    private final JCheckBox particlesCheck;

    /**
     * Checkbox for toggling the bloom post-processing effect.
     */
    private final JCheckBox bloomCheck;

    /**
     * Checkbox for toggling automatic muting when focus is lost.
     */
    private final JCheckBox focusLossCheck;

    /**
     * Cycle button for selecting graphical quality presets.
     */
    private final CycleButton qualityCycle;

    /**
     * Cycle button for selecting the target monitor.
     */
    private final CycleButton monitorCycle;

    /**
     * Selector for picking the target frame rate.
     */
    private final StepSelector fpsSelector;

    /**
     * Slider for controlling in-game music volume.
     */
    private final CustomSlider soundSlider;

    /**
     * Slider for controlling menu background music volume.
     */
    private final CustomSlider menuSlider;

    /**
     * Slider for controlling sound effect (SFX) volume.
     */
    private final CustomSlider sfxSlider;

    /**
     * Label used for displaying status information to the user.
     */
    private JLabel infoLabel;

    /**
     * Constructs a new SettingsPanel.
     *
     * @param screenManager the screen manager used to apply settings
     */
    public SettingsPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;
        setOpaque(false);
        setLayout(new BorderLayout());

        final JLabel mainTitle = new JLabel("SETTINGS");
        mainTitle.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_36));
        mainTitle.setForeground(RenderUtils.cyan);
        mainTitle.setHorizontalAlignment(SwingConstants.CENTER);
        mainTitle.setBorder(BorderFactory.createEmptyBorder(UIScale.scale(20), 0, UIScale.scale(20), 0));
        add(mainTitle, BorderLayout.NORTH);

        final JPanel contentColumn = new JPanel();
        contentColumn.setLayout(new BoxLayout(contentColumn, BoxLayout.Y_AXIS));
        contentColumn.setOpaque(false);
        contentColumn.setBorder(BorderFactory.createEmptyBorder(0, UIScale.scale(20), UIScale.scale(20), UIScale.scale(20)));

        final JPanel displayGroup = createGroupPanel("DISPLAY & GRAPHICS");

        final GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        final String[] monitorNames = new String[devices.length];
        for (int i = 0; i < devices.length; i++) {
            monitorNames[i] = "Monitor " + (i + 1) + " (" + devices[i].getDisplayMode().getWidth() + "x" + devices[i].getDisplayMode().getHeight() + ")";
        }
        monitorCycle = new CycleButton(monitorNames, Math.min(Settings.monitorIndex, monitorNames.length - 1));
        qualityCycle = new CycleButton(new String[]{"LOW", "MEDIUM", "HIGH"}, getQualityIndex());
        fpsSelector = new StepSelector(new int[]{30, 60, 90, 120, 165, 240}, Settings.targetFps);

        setFixedSize(monitorCycle, 320, 42);
        setFixedSize(qualityCycle, 320, 42);
        setFixedSize(fpsSelector, 320, 48);

        displayGroup.add(createPerfectlyCenteredComponent("Monitor:", monitorCycle));
        displayGroup.add(Box.createRigidArea(new Dimension(0, UIScale.scale(15))));
        displayGroup.add(createPerfectlyCenteredComponent("Quality:", qualityCycle));
        displayGroup.add(Box.createRigidArea(new Dimension(0, UIScale.scale(15))));
        displayGroup.add(createPerfectlyCenteredComponent("FPS:", fpsSelector));
        displayGroup.add(Box.createRigidArea(new Dimension(0, UIScale.scale(25))));

        displayGroup.add(createLeftAlignedComponent(fullscreenCheck = new CustomCheckBox("Fullscreen (Borderless)", Settings.fullscreen)));
        displayGroup.add(Box.createRigidArea(new Dimension(0, UIScale.scale(10))));
        displayGroup.add(createLeftAlignedComponent(openglCheck = new CustomCheckBox("OpenGL HW Acceleration", Settings.opengl)));
        displayGroup.add(Box.createRigidArea(new Dimension(0, UIScale.scale(10))));
        displayGroup.add(createLeftAlignedComponent(vsyncCheck = new CustomCheckBox("V-Sync (Triple Buffering)", Settings.vsync)));
        displayGroup.add(Box.createRigidArea(new Dimension(0, UIScale.scale(10))));
        displayGroup.add(createLeftAlignedComponent(showFpsCheck = new CustomCheckBox("Show FPS Counter", Settings.showFps)));

        contentColumn.add(displayGroup);
        contentColumn.add(Box.createRigidArea(new Dimension(0, UIScale.scale(30))));

        final JPanel audioGroup = createGroupPanel("AUDIO");

        final JLabel soundLabel = new JLabel("Music Volume: " + Settings.soundVolume + "%");
        styleLabel(soundLabel);
        soundSlider = new CustomSlider(0, 100, Settings.soundVolume);
        soundSlider.addChangeListener(_ -> soundLabel.setText("Music Volume: " + soundSlider.getValue() + "%"));

        final JLabel menuLabel = new JLabel("Menu Music: " + Settings.menuVolume + "%");
        styleLabel(menuLabel);
        menuSlider = new CustomSlider(0, 100, Settings.menuVolume);
        menuSlider.addChangeListener(_ -> {
            menuLabel.setText("Menu Music: " + menuSlider.getValue() + "%");
            Settings.menuVolume = menuSlider.getValue();
            AudioManager.applyMenuVolume(null);
        });

        final JLabel sfxLabel = new JLabel("SFX Volume: " + Settings.sfxVolume + "%");
        styleLabel(sfxLabel);
        sfxSlider = new CustomSlider(0, 100, Settings.sfxVolume);
        sfxSlider.addChangeListener(_ -> sfxLabel.setText("SFX Volume: " + sfxSlider.getValue() + "%"));

        setFixedSize(soundSlider, 300, 48);
        setFixedSize(menuSlider, 300, 48);
        setFixedSize(sfxSlider, 300, 48);

        audioGroup.add(createPerfectlyCenteredComponent(soundLabel, soundSlider));
        audioGroup.add(Box.createRigidArea(new Dimension(0, UIScale.scale(10))));
        audioGroup.add(createPerfectlyCenteredComponent(menuLabel, menuSlider));
        audioGroup.add(Box.createRigidArea(new Dimension(0, UIScale.scale(10))));
        audioGroup.add(createPerfectlyCenteredComponent(sfxLabel, sfxSlider));
        audioGroup.add(Box.createRigidArea(new Dimension(0, UIScale.scale(20))));
        audioGroup.add(createLeftAlignedComponent(focusLossCheck = new CustomCheckBox("Mute on Focus Loss", Settings.muteOnFocusLoss)));

        contentColumn.add(audioGroup);
        contentColumn.add(Box.createRigidArea(new Dimension(0, UIScale.scale(30))));

        final JPanel cacheGroup = createGroupPanel("CACHE & DATA");
        final JButton clearCacheBtn = getStyledButton("CLEAR CACHE", new Color(180, 40, 40), Color.WHITE);
        setFixedSize(clearCacheBtn, 180, 40);
        clearCacheBtn.addActionListener(_ -> {
            AudioManager.playSFX("/click-sound.mp3");
            FileSystem.clearCache().thenRun(() -> SwingUtilities.invokeLater(() -> {
                infoLabel.setText("Cache cleared successfully!");
                infoLabel.setForeground(RenderUtils.green);
            }));
        });
        cacheGroup.add(createPerfectlyCenteredComponent("Downloaded Tracks:", clearCacheBtn));

        contentColumn.add(cacheGroup);
        contentColumn.add(Box.createRigidArea(new Dimension(0, UIScale.scale(30))));

        final JPanel gameplayGroup = createGroupPanel("VISUAL EFFECTS");
        gameplayGroup.add(createLeftAlignedComponent(particlesCheck = new CustomCheckBox("Enable Background Particles", Settings.particlesEnabled)));
        gameplayGroup.add(Box.createRigidArea(new Dimension(0, UIScale.scale(10))));
        gameplayGroup.add(createLeftAlignedComponent(bloomCheck = new CustomCheckBox("Bloom Post-Processing", Settings.bloomEnabled)));

        contentColumn.add(gameplayGroup);

        final JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(contentColumn);

        final JScrollPane scrollPane = new JScrollPane(wrapperPanel);
        final JScrollBar vsb = scrollPane.getVerticalScrollBar();
        vsb.setUI(new ScrollBarUI());
        vsb.setOpaque(false);
        vsb.setBackground(new Color(0, 0, 0, 0));
        vsb.setPreferredSize(new Dimension(UIScale.scale(16), 0));
        vsb.setUnitIncrement(UIScale.scale(40));
        vsb.setBlockIncrement(UIScale.scale(120));

        final JScrollBar hsb = scrollPane.getHorizontalScrollBar();
        hsb.setUI(new ScrollBarUI());
        hsb.setOpaque(false);
        hsb.setPreferredSize(new Dimension(0, 0));

        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(new Color(0, 0, 0, 0));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    /**
     * Restarts the application by spawning a new Java process.
     */
    private static void restart() throws IOException {
        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final File jarFile;
        try {
            jarFile = new File(SettingsPanel.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        if (!jarFile.getName().endsWith(".jar")) return;
        ProcessBuilder pb = new ProcessBuilder(javaBin, "-jar", jarFile.getPath());
        pb.directory(jarFile.getParentFile());
        pb.start();
        System.exit(0);
    }

    private void setFixedSize(JComponent comp, int width, int height) {
        final Dimension dim = new Dimension(UIScale.scale(width), UIScale.scale(height));
        comp.setMinimumSize(dim);
        comp.setPreferredSize(dim);
        comp.setMaximumSize(dim);
    }

    /**
     * Paints the settings panel background with a gradient and rounded corners.
     *
     * @param g the graphics context to paint on
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2);
        RenderUtils.drawMenuBackground(g2, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }

    /**
     * Creates a styled panel for grouping related settings.
     */
    private JPanel createGroupPanel(String title) {
        final JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphics2D(g2);
                g2.setColor(new Color(255, 255, 255, 10));
                final int arc = UIScale.scale(15);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.setColor(new Color(0, 255, 220, 30));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(UIScale.scale(20), UIScale.scale(30), UIScale.scale(20), UIScale.scale(30)));

        final JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);

        final JLabel t = new JLabel(title);
        t.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_22));
        t.setForeground(RenderUtils.cyan);
        t.setBorder(BorderFactory.createEmptyBorder(0, 0, UIScale.scale(20), 0));
        titlePanel.add(t);

        titlePanel.setMinimumSize(new Dimension(0, UIScale.scale(45)));
        titlePanel.setPreferredSize(new Dimension(UIScale.scale(520), UIScale.scale(45)));
        titlePanel.setMaximumSize(new Dimension(Short.MAX_VALUE, UIScale.scale(45)));
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(titlePanel);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        return p;
    }

    /**
     * Creates the bottom panel containing the Save and Reset buttons.
     */
    private JPanel createBottomPanel() {
        final JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(UIScale.scale(20), 0, UIScale.scale(30), 0));

        final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, UIScale.scale(30), 0));
        buttonsPanel.setOpaque(false);

        final JButton saveBtn = getStyledButton("SAVE & APPLY", RenderUtils.cyan, Color.BLACK);
        final JButton resetBtn = getStyledButton("RESET DEFAULTS", Color.DARK_GRAY, Color.WHITE);

        saveBtn.addActionListener(_ -> {
            AudioManager.playSFX("/click-sound.mp3");
            saveSettings();
        });
        resetBtn.addActionListener(_ -> {
            AudioManager.playSFX("/click-sound.mp3");
            showResetDialog();
        });

        buttonsPanel.add(resetBtn);
        buttonsPanel.add(saveBtn);
        bottomPanel.add(buttonsPanel);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, UIScale.scale(25))));
        bottomPanel.add(infoLabel = new JLabel(" "));
        infoLabel.setFont(UIScale.scaleFont(RenderCache.SANS_PLAIN_20));
        infoLabel.setForeground(Color.YELLOW);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return bottomPanel;
    }

    /**
     * Saves the current UI states into global settings and applies them.
     */
    private void saveSettings() {
        final boolean restartReq = (Settings.opengl != openglCheck.isSelected()) || !Settings.graphicsQuality.equals(qualityCycle.getSelectedOption());
        Settings.fullscreen = fullscreenCheck.isSelected();
        Settings.vsync = vsyncCheck.isSelected();
        Settings.opengl = openglCheck.isSelected();
        Settings.showFps = showFpsCheck.isSelected();
        Settings.graphicsQuality = qualityCycle.getSelectedOption();
        Settings.monitorIndex = monitorCycle.getSelectedIndex();
        Settings.targetFps = fpsSelector.getSelectedValue();
        Settings.soundVolume = soundSlider.getValue();
        Settings.menuVolume = menuSlider.getValue();
        Settings.sfxVolume = sfxSlider.getValue();
        Settings.particlesEnabled = particlesCheck.isSelected();
        Settings.bloomEnabled = bloomCheck.isSelected();
        Settings.muteOnFocusLoss = focusLossCheck.isSelected();
        Settings.save();
        AudioManager.updateMenuVolume();
        screenManager.applySettings();

        if (restartReq) {
            showRestartDialog();
        } else {
            infoLabel.setText("Settings applied successfully!");
            infoLabel.setForeground(RenderUtils.green);
        }
    }

    /**
     * Displays a dialog informing the user that a restart is required.
     */
    private void showRestartDialog() {
        final CustomDialog dialog = new CustomDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Restart Required",
                "<html><center>Some settings require a restart<br>to take full effect.</center></html>",
                RenderUtils.cyan
        );

        final JButton laterBtn = getStyledButton("LATER", Color.DARK_GRAY, Color.WHITE);
        laterBtn.setPreferredSize(new Dimension(UIScale.scale(150), UIScale.scale(45)));
        laterBtn.addActionListener(_ -> {
            AudioManager.playSFX("/click-sound.mp3");
            dialog.dispose();
            infoLabel.setText("Changes saved. Restart for full effect!");
            infoLabel.setForeground(Color.ORANGE);
        });

        final JButton restartBtn = getStyledButton("RESTART NOW", RenderUtils.cyan, Color.BLACK);
        restartBtn.setPreferredSize(new Dimension(UIScale.scale(180), UIScale.scale(45)));
        restartBtn.addActionListener(_ -> {
            AudioManager.playSFX("/click-sound.mp3");
            dialog.dispose();
            try {
                restart();
            } catch (IOException _) {
            }
        });

        dialog.addButton(laterBtn);
        dialog.addButton(restartBtn);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Displays a confirmation dialog for resetting settings to defaults.
     */
    private void showResetDialog() {
        final CustomDialog dialog = new CustomDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Reset to Defaults",
                "<html><center>Are you sure you want to reset all<br>settings to their defaults?</center></html>",
                Color.RED
        );

        final JButton cancelBtn = getStyledButton("CANCEL", Color.DARK_GRAY, Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(UIScale.scale(150), UIScale.scale(45)));
        cancelBtn.addActionListener(_ -> {
            AudioManager.playSFX("/click-sound.mp3");
            dialog.dispose();
        });

        final JButton resetConfirmBtn = getStyledButton("RESET", new Color(220, 50, 50), Color.WHITE);
        resetConfirmBtn.setPreferredSize(new Dimension(UIScale.scale(150), UIScale.scale(45)));
        resetConfirmBtn.addActionListener(_ -> {
            AudioManager.playSFX("/click-sound.mp3");
            dialog.dispose();
            resetToDefaults();
        });

        dialog.addButton(cancelBtn);
        dialog.addButton(resetConfirmBtn);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Restores all settings to their default values and saves them.
     */
    private void resetToDefaults() {
        final boolean restartReq = (!Settings.opengl) || !Settings.graphicsQuality.equals("HIGH");
        fullscreenCheck.setSelected(true);
        vsyncCheck.setSelected(false);
        openglCheck.setSelected(true);
        showFpsCheck.setSelected(false);
        qualityCycle.currentIndex = 2;
        qualityCycle.setText("HIGH");
        monitorCycle.currentIndex = 0;
        monitorCycle.setText(monitorCycle.options[0]);
        fpsSelector.setSelectedIndexByValue(60);
        soundSlider.setValue(100);
        menuSlider.setValue(80);
        sfxSlider.setValue(100);
        particlesCheck.setSelected(true);
        bloomCheck.setSelected(true);
        focusLossCheck.setSelected(false);
        Settings.reset();
        Settings.save();
        screenManager.applySettings();

        if (restartReq) {
            showRestartDialog();
        } else {
            infoLabel.setText("Defaults restored and saved.");
            infoLabel.setForeground(RenderUtils.green);
        }
    }

    /**
     * Styles a label for consistent look across settings groups.
     */
    private void styleLabel(JLabel l) {
        l.setFont(UIScale.scaleFont(RenderCache.SANS_PLAIN_18));
        l.setForeground(Color.WHITE);
        final Dimension labelDim = new Dimension(UIScale.scale(210), UIScale.scale(40));
        l.setPreferredSize(labelDim);
        l.setMinimumSize(labelDim);
        l.setMaximumSize(labelDim);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    /**
     * Creates a horizontal panel with a label and a component.
     */
    private JPanel createLabeledComponent(String labelText, JComponent comp) {
        final JLabel l = new JLabel(labelText);
        styleLabel(l);
        return createLabeledComponent(l, comp);
    }

    /**
     * Creates a horizontal panel with a label and a component.
     */
    private JPanel createLabeledComponent(JLabel l, JComponent comp) {
        final JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setOpaque(false);
        p.add(l);
        p.add(Box.createRigidArea(new Dimension(UIScale.scale(20), 0)));
        p.add(comp);

        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        final int h = UIScale.scale(50);
        p.setMinimumSize(new Dimension(0, h));
        p.setPreferredSize(new Dimension(UIScale.scale(540), h));
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, h));
        return p;
    }

    /**
     * Creates a horizontal panel to perfectly center the component by adding a balancing rigid area on the right.
     */
    private JPanel createPerfectlyCenteredComponent(String labelText, JComponent comp) {
        final JLabel l = new JLabel(labelText);
        styleLabel(l);
        return createPerfectlyCenteredComponent(l, comp);
    }

    /**
     * Creates a horizontal panel to perfectly center the component by adding a balancing rigid area on the right.
     */
    private JPanel createPerfectlyCenteredComponent(JLabel l, JComponent comp) {
        final JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setOpaque(false);

        p.add(Box.createHorizontalGlue());
        p.add(l);
        p.add(Box.createRigidArea(new Dimension(UIScale.scale(20), 0)));
        p.add(comp);
        p.add(Box.createRigidArea(new Dimension(UIScale.scale(230), 0)));
        p.add(Box.createHorizontalGlue());

        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    /**
     * Creates a horizontal panel to align a single component (like a checkbox) to the left.
     */
    private JPanel createLeftAlignedComponent(JComponent comp) {
        final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, UIScale.scale(30), 0));
        p.setOpaque(false);
        p.add(comp);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    /**
     * Returns a styled JButton with custom background and foreground colors.
     */
    private JButton getStyledButton(String text, Color bg, Color fg) {
        final JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphics2D(g2);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UIScale.scale(16), UIScale.scale(16));
                g2.setColor(fg);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        btn.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_18));
        btn.setPreferredSize(new Dimension(UIScale.scale(200), UIScale.scale(50)));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Map graphical quality string to internal index.
     */
    private int getQualityIndex() {
        if (Settings.graphicsQuality.equals("LOW")) return 0;
        if (Settings.graphicsQuality.equals("MEDIUM")) return 1;
        return 2;
    }
}
