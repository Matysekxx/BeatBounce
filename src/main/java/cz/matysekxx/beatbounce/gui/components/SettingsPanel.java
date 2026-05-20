package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;
import cz.matysekxx.beatbounce.system.FileSystem;

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

    private final ScreenManager screenManager;
    private final JCheckBox fullscreenCheck;
    private final JCheckBox openglCheck;
    private final JCheckBox vsyncCheck;
    private final JCheckBox showFpsCheck;
    private final JCheckBox particlesCheck;
    private final JCheckBox bloomCheck;
    private final JCheckBox focusLossCheck;
    private final CycleButton qualityCycle;
    private final CycleButton monitorCycle;
    private final StepSelector fpsSelector;
    private final CustomSlider soundSlider;
    private final CustomSlider sfxSlider;
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
        mainTitle.setFont(RenderCache.SANS_BOLD_36);
        mainTitle.setForeground(RenderUtils.cyan);
        mainTitle.setHorizontalAlignment(SwingConstants.CENTER);
        mainTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(mainTitle, BorderLayout.NORTH);

        final JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 15, 0, 15);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;

        final JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.setOpaque(false);

        final JPanel displayGroup = createGroupPanel("DISPLAY & GRAPHICS");

        final GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        final String[] monitorNames = new String[devices.length];
        for (int i = 0; i < devices.length; i++) {
            monitorNames[i] = "Monitor " + (i + 1) + " (" + devices[i].getDisplayMode().getWidth() + "x" + devices[i].getDisplayMode().getHeight() + ")";
        }
        monitorCycle = new CycleButton(monitorNames, Math.min(Settings.monitorIndex, monitorNames.length - 1));
        monitorCycle.setMinimumSize(new Dimension(240, 40));
        monitorCycle.setPreferredSize(new Dimension(240, 40));
        monitorCycle.setMaximumSize(new Dimension(240, 40));

        qualityCycle = new CycleButton(new String[]{"LOW", "MEDIUM", "HIGH"}, getQualityIndex());
        qualityCycle.setMinimumSize(new Dimension(240, 40));
        qualityCycle.setPreferredSize(new Dimension(240, 40));
        qualityCycle.setMaximumSize(new Dimension(240, 40));

        fpsSelector = new StepSelector(new int[]{30, 60, 90, 120, 165, 240}, Settings.targetFps);
        fpsSelector.setMinimumSize(new Dimension(300, 45));
        fpsSelector.setPreferredSize(new Dimension(300, 45));
        fpsSelector.setMaximumSize(new Dimension(300, 45));

        displayGroup.add(createLabeledComponent("Monitor:", monitorCycle));
        displayGroup.add(Box.createRigidArea(new Dimension(0, 25)));
        displayGroup.add(createLabeledComponent("Quality:", qualityCycle));
        displayGroup.add(Box.createRigidArea(new Dimension(0, 25)));
        displayGroup.add(createLabeledComponent("Target FPS:", fpsSelector));
        displayGroup.add(Box.createRigidArea(new Dimension(0, 35)));

        displayGroup.add(fullscreenCheck = new CustomCheckBox("Fullscreen (Borderless)", Settings.fullscreen));
        displayGroup.add(Box.createRigidArea(new Dimension(0, 15)));
        displayGroup.add(openglCheck = new CustomCheckBox("OpenGL Hardware Acceleration", Settings.opengl));
        displayGroup.add(Box.createRigidArea(new Dimension(0, 15)));
        displayGroup.add(vsyncCheck = new CustomCheckBox("V-Sync", Settings.vsync));
        displayGroup.add(Box.createRigidArea(new Dimension(0, 15)));
        displayGroup.add(showFpsCheck = new CustomCheckBox("Show FPS Overlay", Settings.showFps));

        leftColumn.add(displayGroup);
        leftColumn.add(Box.createVerticalGlue());

        final JPanel rightColumn = new JPanel();
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.setOpaque(false);

        final JPanel audioGroup = createGroupPanel("AUDIO");
        final JLabel soundLabel = new JLabel("Music Volume: " + Settings.soundVolume + "%");
        styleLabel(soundLabel);
        soundSlider = new CustomSlider(0, 100, Settings.soundVolume);
        soundSlider.setMinimumSize(new Dimension(250, 45));
        soundSlider.setPreferredSize(new Dimension(250, 45));
        soundSlider.setMaximumSize(new Dimension(250, 45));
        soundSlider.addChangeListener(_ -> soundLabel.setText("Music Volume: " + soundSlider.getValue() + "%"));

        final JLabel sfxLabel = new JLabel("SFX Volume: " + Settings.sfxVolume + "%");
        styleLabel(sfxLabel);
        sfxSlider = new CustomSlider(0, 100, Settings.sfxVolume);
        sfxSlider.setMinimumSize(new Dimension(250, 45));
        sfxSlider.setPreferredSize(new Dimension(250, 45));
        sfxSlider.setMaximumSize(new Dimension(250, 45));
        sfxSlider.addChangeListener(_ -> sfxLabel.setText("SFX Volume: " + sfxSlider.getValue() + "%"));

        audioGroup.add(createLabeledComponent(soundLabel, soundSlider));
        audioGroup.add(Box.createRigidArea(new Dimension(0, 15)));
        audioGroup.add(createLabeledComponent(sfxLabel, sfxSlider));
        audioGroup.add(Box.createRigidArea(new Dimension(0, 20)));
        audioGroup.add(focusLossCheck = new CustomCheckBox("Mute on Focus Loss", Settings.muteOnFocusLoss));
        
        final JButton clearCacheBtn = getStyledButton("CLEAR CACHE", new Color(180, 40, 40), Color.WHITE);
        clearCacheBtn.setPreferredSize(new Dimension(200, 40));
        clearCacheBtn.setMaximumSize(new Dimension(200, 40));
        clearCacheBtn.addActionListener(_ -> {
            cz.matysekxx.beatbounce.configuration.Settings.playSFX("/click-sound.mp3");
            FileSystem.clearCache().thenRun(() -> SwingUtilities.invokeLater(() -> {
                infoLabel.setText("Cache cleared successfully!");
                infoLabel.setForeground(RenderUtils.green);
            }));
        });
        audioGroup.add(Box.createRigidArea(new Dimension(0, 20)));
        audioGroup.add(createLabeledComponent("Downloaded Music:", clearCacheBtn));

        final JPanel gameplayGroup = createGroupPanel("GAMEPLAY & EFFECTS");
        gameplayGroup.add(particlesCheck = new CustomCheckBox("Enable Background Particles", Settings.particlesEnabled));
        gameplayGroup.add(Box.createRigidArea(new Dimension(0, 20)));
        gameplayGroup.add(bloomCheck = new CustomCheckBox("Bloom Post-Processing", Settings.bloomEnabled));
        audioGroup.setAlignmentX(Component.LEFT_ALIGNMENT);
        gameplayGroup.setAlignmentX(Component.LEFT_ALIGNMENT);

        rightColumn.add(audioGroup);
        rightColumn.add(Box.createRigidArea(new Dimension(0, 40)));
        rightColumn.add(gameplayGroup);
        rightColumn.add(Box.createVerticalGlue());
        gbc.gridx = 0;
        mainContent.add(leftColumn, gbc);
        gbc.gridx = 1;
        mainContent.add(rightColumn, gbc);
        add(mainContent, BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

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

    private JPanel createGroupPanel(String title) {
        final JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphics2D(g2);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(0, 255, 220, 30));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        final JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);

        final JLabel t = new JLabel(title);
        t.setFont(RenderCache.SANS_BOLD_22);
        t.setForeground(RenderUtils.cyan);
        t.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        titlePanel.add(t);

        titlePanel.setMinimumSize(new Dimension(0, 45));
        titlePanel.setPreferredSize(new Dimension(520, 45));
        titlePanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 45));
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(titlePanel);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        return p;
    }

    private JPanel createBottomPanel() {
        final JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));

        final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        buttonsPanel.setOpaque(false);

        final JButton saveBtn = getStyledButton("SAVE & APPLY", RenderUtils.cyan, Color.BLACK);
        final JButton resetBtn = getStyledButton("RESET DEFAULTS", Color.DARK_GRAY, Color.WHITE);

        saveBtn.addActionListener(_ -> {
            Settings.playSFX("/click-sound.mp3");
            saveSettings();
        });
        resetBtn.addActionListener(_ -> {
            Settings.playSFX("/click-sound.mp3");
            showResetDialog();
        });

        buttonsPanel.add(resetBtn);
        buttonsPanel.add(saveBtn);
        bottomPanel.add(buttonsPanel);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        bottomPanel.add(infoLabel = new JLabel(" "));
        infoLabel.setFont(RenderCache.SANS_PLAIN_20);
        infoLabel.setForeground(Color.YELLOW);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return bottomPanel;
    }

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
        Settings.sfxVolume = sfxSlider.getValue();
        Settings.particlesEnabled = particlesCheck.isSelected();
        Settings.bloomEnabled = bloomCheck.isSelected();
        Settings.muteOnFocusLoss = focusLossCheck.isSelected();
        Settings.save();
        screenManager.applySettings();

        if (restartReq) {
            showRestartDialog();
        } else {
            infoLabel.setText("Settings applied successfully!");
            infoLabel.setForeground(RenderUtils.green);
        }
    }

    private void showRestartDialog() {
        final CustomDialog dialog = new CustomDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Restart Required",
                "<html><center>Some settings require a restart<br>to take full effect.</center></html>",
                RenderUtils.cyan
        );

        final JButton laterBtn = getStyledButton("LATER", Color.DARK_GRAY, Color.WHITE);
        laterBtn.setPreferredSize(new Dimension(150, 45));
        laterBtn.addActionListener(_ -> {
            Settings.playSFX("/click-sound.mp3");
            dialog.dispose();
            infoLabel.setText("Changes saved. Restart for full effect!");
            infoLabel.setForeground(Color.ORANGE);
        });

        final JButton restartBtn = getStyledButton("RESTART NOW", RenderUtils.cyan, Color.BLACK);
        restartBtn.setPreferredSize(new Dimension(180, 45));
        restartBtn.addActionListener(_ -> {
            Settings.playSFX("/click-sound.mp3");
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

    private void showResetDialog() {
        final CustomDialog dialog = new CustomDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Reset to Defaults",
                "<html><center>Are you sure you want to reset all<br>settings to their defaults?</center></html>",
                Color.RED
        );

        final JButton cancelBtn = getStyledButton("CANCEL", Color.DARK_GRAY, Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(150, 45));
        cancelBtn.addActionListener(_ -> {
            Settings.playSFX("/click-sound.mp3");
            dialog.dispose();
        });

        final JButton resetConfirmBtn = getStyledButton("RESET", new Color(220, 50, 50), Color.WHITE);
        resetConfirmBtn.setPreferredSize(new Dimension(150, 45));
        resetConfirmBtn.addActionListener(_ -> {
            Settings.playSFX("/click-sound.mp3");
            dialog.dispose();
            resetToDefaults();
        });

        dialog.addButton(cancelBtn);
        dialog.addButton(resetConfirmBtn);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

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

    private void styleLabel(JLabel l) {
        l.setFont(RenderCache.SANS_PLAIN_18);
        l.setForeground(Color.WHITE);
        l.setPreferredSize(new Dimension(200, 40));
        l.setMinimumSize(new Dimension(200, 40));
        l.setMaximumSize(new Dimension(200, 40));
    }

    private JPanel createLabeledComponent(String labelText, JComponent comp) {
        final JLabel l = new JLabel(labelText);
        styleLabel(l);
        return createLabeledComponent(l, comp);
    }

    private JPanel createLabeledComponent(JLabel l, JComponent comp) {
        final JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setOpaque(false);
        p.add(l);
        p.add(Box.createRigidArea(new Dimension(10, 0)));
        p.add(comp);

        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMinimumSize(new Dimension(0, 50));
        p.setPreferredSize(new Dimension(520, 50));
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
        return p;
    }

    private JButton getStyledButton(String text, Color bg, Color fg) {
        final JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphics2D(g2);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(fg);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        btn.setFont(RenderCache.SANS_BOLD_18);
        btn.setPreferredSize(new Dimension(200, 50));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private int getQualityIndex() {
        if (Settings.graphicsQuality.equals("LOW")) return 0;
        if (Settings.graphicsQuality.equals("MEDIUM")) return 1;
        return 2;
    }
}