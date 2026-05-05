package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.configuration.Settings;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;

import javax.swing.*;
import java.awt.*;

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
    private JLabel infoLabel;

    public SettingsPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;
        setOpaque(false);
        setLayout(new BorderLayout());

        final JLabel mainTitle = new JLabel("SETTINGS");
        mainTitle.setFont(new Font("SansSerif", Font.BOLD, 56));
        mainTitle.setForeground(RenderUtils.cyan);
        mainTitle.setHorizontalAlignment(SwingConstants.CENTER);
        mainTitle.setBorder(BorderFactory.createEmptyBorder(40, 0, 30, 0));
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
        qualityCycle = new CycleButton(new String[]{"LOW", "MEDIUM", "HIGH"}, getQualityIndex());
        fpsSelector = new StepSelector(new int[]{30, 60, 90, 120, 165, 240}, Settings.targetFps);

        displayGroup.add(createLabeledComponent("Monitor:", monitorCycle));
        displayGroup.add(createLabeledComponent("Quality:", qualityCycle));
        displayGroup.add(createLabeledComponent("Target FPS:", fpsSelector));
        displayGroup.add(Box.createRigidArea(new Dimension(0, 10)));
        displayGroup.add(fullscreenCheck = new CustomCheckBox("Fullscreen (Borderless)", Settings.fullscreen));
        displayGroup.add(openglCheck = new CustomCheckBox("OpenGL Hardware Acceleration", Settings.opengl));
        displayGroup.add(vsyncCheck = new CustomCheckBox("V-Sync", Settings.vsync));
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
        soundSlider.addChangeListener(_ -> soundLabel.setText("Music Volume: " + soundSlider.getValue() + "%"));
        audioGroup.add(createLabeledComponent(soundLabel, soundSlider));
        audioGroup.add(focusLossCheck = new CustomCheckBox("Mute on Focus Loss", Settings.muteOnFocusLoss));

        final JPanel gameplayGroup = createGroupPanel("GAMEPLAY & EFFECTS");
        gameplayGroup.add(particlesCheck = new CustomCheckBox("Enable Background Particles", Settings.particlesEnabled));
        gameplayGroup.add(bloomCheck = new CustomCheckBox("Bloom Post-Processing", Settings.bloomEnabled));

        rightColumn.add(audioGroup);
        rightColumn.add(Box.createRigidArea(new Dimension(0, 20)));
        rightColumn.add(gameplayGroup);
        rightColumn.add(Box.createVerticalGlue());

        gbc.gridx = 0;
        mainContent.add(leftColumn, gbc);
        gbc.gridx = 1;
        mainContent.add(rightColumn, gbc);

        final JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerWrapper.setOpaque(false);
        centerWrapper.add(mainContent);
        add(centerWrapper, BorderLayout.CENTER);

        add(createBottomPanel(), BorderLayout.SOUTH);
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
        p.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        final JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 20));
        t.setForeground(RenderUtils.cyan);
        t.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        p.add(t);
        return p;
    }

    private JPanel createBottomPanel() {
        final JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 40, 0));

        final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonsPanel.setOpaque(false);

        final JButton saveBtn = getStyledButton("SAVE & APPLY", RenderUtils.cyan, Color.BLACK);
        final JButton resetBtn = getStyledButton("RESET DEFAULTS", Color.DARK_GRAY, Color.WHITE);

        saveBtn.addActionListener(_ -> saveSettings());
        resetBtn.addActionListener(_ -> showResetDialog());

        buttonsPanel.add(resetBtn);
        buttonsPanel.add(saveBtn);
        bottomPanel.add(buttonsPanel);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        bottomPanel.add(infoLabel = new JLabel(" "));
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
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
            dialog.dispose();
            infoLabel.setText("Changes saved. Restart for full effect!");
            infoLabel.setForeground(Color.ORANGE);
        });

        final JButton restartBtn = getStyledButton("RESTART NOW", RenderUtils.cyan, Color.BLACK);
        restartBtn.setPreferredSize(new Dimension(180, 45));
        restartBtn.addActionListener(_ -> {
            dialog.dispose();
            //TODO: pridat restart aplikace pres cmd
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
        cancelBtn.addActionListener(_ -> dialog.dispose());

        final JButton resetConfirmBtn = getStyledButton("RESET", new Color(220, 50, 50), Color.WHITE);
        resetConfirmBtn.setPreferredSize(new Dimension(150, 45));
        resetConfirmBtn.addActionListener(_ -> {
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
        particlesCheck.setSelected(true);
        bloomCheck.setSelected(true);
        focusLossCheck.setSelected(false);

        Settings.fullscreen = true;
        Settings.vsync = false;
        Settings.opengl = true;
        Settings.showFps = false;
        Settings.graphicsQuality = "HIGH";
        Settings.monitorIndex = 0;
        Settings.targetFps = 60;
        Settings.soundVolume = 100;
        Settings.particlesEnabled = true;
        Settings.bloomEnabled = true;
        Settings.muteOnFocusLoss = false;
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
        l.setFont(new Font("SansSerif", Font.PLAIN, 18));
        l.setForeground(Color.WHITE);
        l.setPreferredSize(new Dimension(190, 35));
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
        p.add(Box.createRigidArea(new Dimension(15, 0)));
        p.add(comp);
        p.setMaximumSize(new Dimension(800, 50));
        return p;
    }

    private JButton getStyledButton(String text, Color bg, Color fg) {
        final JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphics2D(g2);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(fg);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
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