package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.achievement.Achievement;
import cz.matysekxx.beatbounce.model.achievement.AchievementManager;
import cz.matysekxx.beatbounce.util.UIScale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AchievementRowPanel extends JPanel {
    private final Achievement achievement;
    private final Runnable claimCallback;
    private boolean hovered = false;
    private float claimAnimationProgress = 0f;

    public AchievementRowPanel(Achievement achievement, Runnable claimCallback) {
        this.achievement = achievement;
        this.claimCallback = claimCallback;

        setOpaque(false);
        setLayout(new BorderLayout(UIScale.scale(15), 0));
        setBorder(new EmptyBorder(UIScale.scale(12), UIScale.scale(24), UIScale.scale(12), UIScale.scale(24)));
        setPreferredSize(new Dimension(0, UIScale.scale(92)));
        setMaximumSize(new Dimension(Short.MAX_VALUE, UIScale.scale(92)));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });

        add(new AchievementIconPanel(achievement), BorderLayout.WEST);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createRightPanel(), BorderLayout.EAST);
    }

    private JPanel createCenterPanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        final JLabel titleLabel = new JLabel(achievement.getTitle());
        titleLabel.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_16));
        titleLabel.setForeground(achievement.isCompleted() ? RenderUtils.cyan : Color.WHITE);

        final JLabel descLabel = new JLabel(achievement.getDescription());
        descLabel.setFont(UIScale.scaleFont(RenderCache.SANS_PLAIN_13));
        descLabel.setForeground(new Color(145, 145, 150));

        final JLabel rewardLabel = new JLabel();
        rewardLabel.setFont(UIScale.scaleFont(RenderCache.MONO_BOLD_12));
        if (achievement.isRewarded()) {
            rewardLabel.setText("+" + achievement.getReward() + " ORBS (CLAIMED)");
            rewardLabel.setForeground(new Color(110, 110, 120));
        } else if (achievement.isCompleted()) {
            rewardLabel.setText("+" + achievement.getReward() + " ORBS (READY TO CLAIM)");
            rewardLabel.setForeground(new Color(255, 195, 0));
        } else {
            rewardLabel.setText("+" + achievement.getReward() + " ORBS");
            rewardLabel.setForeground(new Color(210, 160, 50));
        }

        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, UIScale.scale(3))));
        panel.add(descLabel);
        panel.add(Box.createRigidArea(new Dimension(0, UIScale.scale(4))));
        panel.add(rewardLabel);
        return panel;
    }

    private JPanel createRightPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, UIScale.scale(15));
        panel.add(new AchievementProgressPanel(achievement), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(new AchievementClaimPanel(achievement, this::triggerClaimAnimation), gbc);
        return panel;
    }

    private void triggerClaimAnimation() {
        claimAnimationProgress = 1.0f;
        if (AchievementManager.claimReward(achievement)) {
            final Timer timer = new Timer(15, e -> {
                claimAnimationProgress -= 0.03f;
                if (claimAnimationProgress <= 0f) {
                    claimAnimationProgress = 0f;
                    ((Timer) e.getSource()).stop();
                    if (claimCallback != null) claimCallback.run();
                }
                repaint();
            });
            timer.start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2);
        final int w = getWidth();
        final int h = getHeight();
        final int arc = UIScale.scale(10);

        final Color[] colors = getThemeColors();
        g2.setPaint(new LinearGradientPaint(0, 0, w, h, new float[]{0f, 1f}, new Color[]{colors[0], colors[1]}));
        g2.fillRoundRect(0, 0, w, h, arc, arc);
        g2.setPaint(new LinearGradientPaint(0, 0, w, 0, new float[]{0f, 1f}, new Color[]{colors[2], colors[3]}));
        g2.setStroke(RenderCache.STROKE_1);
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

        if (claimAnimationProgress > 0f) {
            g2.setColor(new Color(255, 190, 0, (int) (100 * claimAnimationProgress)));
            g2.fillRoundRect(0, 0, w, h, arc, arc);
        }
        g2.dispose();
        super.paintComponent(g);
    }

    private Color[] getThemeColors() {
        if (achievement.isCompleted()) {
            if (hovered) return new Color[]{new Color(15, 32, 40, 150), new Color(25, 12, 38, 120), new Color(0, 255, 220, 160), new Color(190, 0, 255, 130)};
            return new Color[]{new Color(12, 22, 28, 100), new Color(18, 10, 26, 80), new Color(0, 255, 220, 60), new Color(0, 200, 255, 25)};
        }
        if (hovered) return new Color[]{new Color(24, 24, 30, 130), new Color(15, 15, 20, 95), new Color(255, 255, 255, 80), new Color(255, 255, 255, 25)};
        return new Color[]{new Color(18, 18, 22, 90), new Color(12, 12, 16, 60), new Color(255, 255, 255, 12), new Color(255, 255, 255, 5)};
    }
}
