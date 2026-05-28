package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.achievement.Achievement;
import cz.matysekxx.beatbounce.model.achievement.AchievementManager;
import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * A composite panel representing a single row in the achievements list.
 * It coordinates several sub-panels:
 * <ul>
 *     <li>{@link AchievementIconPanel} for the visual icon.</li>
 *     <li>{@link AchievementProgressPanel} for the numerical/bar progress.</li>
 *     <li>{@link AchievementClaimPanel} for the reward collection button.</li>
 * </ul>
 * It also handles the 'claimed' flash animation.
 */
public class AchievementRowPanel extends JPanel {
    /**
     * The achievement data for this row.
     */
    private final Achievement achievement;

    /**
     * Callback for when an achievement is claimed, used to refresh the UI.
     */
    private final Runnable claimCallback;

    /**
     * The panel that displays the achievement icon.
     */
    private final AchievementIconPanel iconPanel;

    /**
     * The panel that displays the achievement progress.
     */
    private final AchievementProgressPanel progressPanel;

    /**
     * The panel that provides the interface to claim the achievement reward.
     */
    private final AchievementClaimPanel claimPanel;

    /**
     * Tracks hover state for background highlighting.
     */
    private boolean hovered = false;

    /**
     * Progress of the flash animation when reward is claimed (1.0 -> 0.0).
     */
    private float claimAnimationProgress = 0f;

    /**
     * Constructs a row panel.
     *
     * @param achievement   the achievement to display
     * @param claimCallback runnable to execute after successful claim
     */
    public AchievementRowPanel(Achievement achievement, Runnable claimCallback) {
        this.achievement = achievement;
        this.claimCallback = claimCallback;

        this.setOpaque(false);
        this.setLayout(null);

        this.iconPanel = new AchievementIconPanel(achievement);
        this.progressPanel = new AchievementProgressPanel(achievement);
        this.claimPanel = new AchievementClaimPanel(achievement, this::triggerClaimAnimation);

        this.add(iconPanel);
        this.add(progressPanel);
        this.add(claimPanel);

        this.addMouseListener(new MouseAdapter() {
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
    }

    /**
     * Returns the preferred size of this panel, accounting for UI scaling.
     *
     * @return the preferred dimension
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, UIScale.scale(90));
    }

    /**
     * Returns the maximum size of this panel, allowing it to stretch horizontally.
     *
     * @return the maximum dimension
     */
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, UIScale.scale(90));
    }


    /**
     * Manual layout of sub-components for precise positioning within the row.
     */
    @Override
    public void doLayout() {
        final int w = getWidth();
        final int h = getHeight();

        final int iconSize = UIScale.scale(60);
        iconPanel.setBounds(UIScale.scale(18), UIScale.scale(15), iconSize, iconSize);

        final int barW = UIScale.scale(170);
        final int barH = UIScale.scale(38);
        progressPanel.setBounds(w - UIScale.scale(345), (h - barH) / 2, barW, barH);

        final int btnW = UIScale.scale(140);
        final int btnH = UIScale.scale(50);
        claimPanel.setBounds(w - UIScale.scale(155), (h - btnH) / 2, btnW, btnH);
    }

    /**
     * Initiates the claim logic and starts the visual flash animation.
     */
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

    /**
     * Renders the row background with hover highlights and text info.
     *
     * @param g the Graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphics2D(g2);
        final int w = getWidth();
        final int h = getHeight();
        if (hovered) {
            if (achievement.isCompleted()) {
                if (achievement.isRewarded()) {

                    g2.setPaint(new LinearGradientPaint(0, 0, w, 0,
                            new float[]{0f, 1f},
                            new Color[]{new Color(0, 255, 110, 35), new Color(0, 255, 110, 5)}));
                    g2.fillRoundRect(0, 0, w, h, UIScale.scale(18), UIScale.scale(18));
                    g2.setColor(new Color(0, 255, 110, 120));
                } else {

                    g2.setPaint(new LinearGradientPaint(0, 0, w, 0,
                            new float[]{0f, 1f},
                            new Color[]{new Color(255, 215, 0, 45), new Color(255, 215, 0, 5)}));
                    g2.fillRoundRect(0, 0, w, h, UIScale.scale(18), UIScale.scale(18));
                    g2.setColor(new Color(255, 215, 0, 140));
                }
            } else {

                g2.setPaint(new LinearGradientPaint(0, 0, w, 0,
                        new float[]{0f, 1f},
                        new Color[]{new Color(255, 255, 255, 20), new Color(255, 255, 255, 5)}));
                g2.fillRoundRect(0, 0, w, h, UIScale.scale(18), UIScale.scale(18));
                g2.setColor(new Color(255, 255, 255, 60));
            }
        } else {

            g2.setColor(new Color(255, 255, 255, 12));
            g2.fillRoundRect(0, 0, w, h, UIScale.scale(18), UIScale.scale(18));
            g2.setColor(new Color(255, 255, 255, 25));
        }
        g2.drawRoundRect(0, 0, w - 1, h - 1, UIScale.scale(18), UIScale.scale(18));


        g2.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_22));
        if (achievement.isCompleted()) {
            if (achievement.isRewarded()) {
                g2.setColor(new Color(0, 220, 110));
            } else {
                g2.setColor(new Color(255, 215, 0));
            }
        } else {
            g2.setColor(Color.WHITE);
        }
        g2.drawString(achievement.getTitle(), UIScale.scale(100), UIScale.scale(42));

        g2.setFont(UIScale.scaleFont(RenderCache.SANS_PLAIN_20));
        g2.setColor(new Color(160, 160, 180));
        final String descBase = achievement.getDescription() + "  •  Reward: ";
        g2.drawString(descBase, UIScale.scale(100), UIScale.scale(68));

        final int descWidth = g2.getFontMetrics().stringWidth(descBase);
        final String rewardText = "+" + achievement.getReward() + " ORBS";
        if (achievement.isRewarded()) {
            g2.setColor(new Color(110, 110, 120));
        } else if (achievement.isCompleted()) {
            g2.setColor(new Color(255, 195, 0));
        } else {
            g2.setColor(new Color(210, 160, 50));
        }
        g2.drawString(rewardText, UIScale.scale(100) + descWidth, UIScale.scale(68));


        if (claimAnimationProgress > 0f) {
            g2.setColor(new Color(255, 215, 0, (int) (120 * claimAnimationProgress)));
            g2.fillRoundRect(0, 0, w, h, UIScale.scale(18), UIScale.scale(18));
        }

        g2.dispose();
    }
}
