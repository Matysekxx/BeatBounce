package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.achievement.Achievement;
import cz.matysekxx.beatbounce.model.achievement.AchievementManager;
import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class AchievementsPanel extends BasePanel {

    private final JPanel listPanel;
    
    private CycleButton filterBtn;
    private CycleButton sortBtn;

    
    public AchievementsPanel() {
        super();
        setOpaque(false);
        setLayout(new BorderLayout());
        final JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setBorder(new EmptyBorder(UIScale.scale(10), UIScale.scale(20), UIScale.scale(20), UIScale.scale(20)));
        final JScrollPane scrollPane = buildScrollPane(listPanel);
        add(scrollPane, BorderLayout.CENTER);
        loadAchievements();
    }

    
    private static JScrollPane buildScrollPane(JPanel content) {
        return SongSelectionPanel.buildScrollPane(content);
    }

    @Override
    protected void drawBackground(Graphics2D g2d, int w, int h) {
        RenderUtils.drawMenuBackground(g2d, w, h);
    }

    
    private JPanel createTopBar() {
        final JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setPreferredSize(new Dimension(0, UIScale.scale(70)));
        topBar.setBorder(new EmptyBorder(0, UIScale.scale(30), 0, UIScale.scale(30)));

        final JLabel titleLabel = new JLabel("ACHIEVEMENTS");
        titleLabel.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_28));
        titleLabel.setForeground(RenderUtils.cyan);
        titleLabel.setVerticalAlignment(SwingConstants.CENTER);
        topBar.add(titleLabel, BorderLayout.WEST);

        final JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, UIScale.scale(15), UIScale.scale(12)));
        controls.setOpaque(false);

        final JLabel filterLabel = new JLabel("FILTER:");
        filterLabel.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_13));
        filterLabel.setForeground(new Color(200, 200, 220));
        controls.add(filterLabel);

        filterBtn = new CycleButton(new String[]{"ALL", "READY TO CLAIM", "CLAIMED", "IN PROGRESS"}, 0);
        filterBtn.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_14));
        filterBtn.setPreferredSize(new Dimension(UIScale.scale(170), UIScale.scale(36)));
        filterBtn.addActionListener(_ -> loadAchievements());
        controls.add(filterBtn);

        controls.add(Box.createRigidArea(new Dimension(UIScale.scale(10), 0)));

        final JLabel sortLabel = new JLabel("SORT:");
        sortLabel.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_13));
        sortLabel.setForeground(new Color(200, 200, 220));
        controls.add(sortLabel);

        sortBtn = new CycleButton(new String[]{"DEFAULT", "PROGRESS", "REWARD"}, 0);
        sortBtn.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_14));
        sortBtn.setPreferredSize(new Dimension(UIScale.scale(150), UIScale.scale(36)));
        sortBtn.addActionListener(_ -> loadAchievements());
        controls.add(sortBtn);

        topBar.add(controls, BorderLayout.EAST);
        return topBar;
    }

    
    public void loadAchievements() {
        if (listPanel == null) return;
        listPanel.removeAll();
        final List<Achievement> list = AchievementManager.getAchievements();

        final String filter = filterBtn != null ? filterBtn.getSelectedOption() : "ALL";
        final List<Achievement> filtered = list.stream()
                .filter(ach -> switch (filter) {
                    case "READY TO CLAIM" -> ach.isCompleted() && !ach.isRewarded();
                    case "CLAIMED" -> ach.isCompleted() && ach.isRewarded();
                    case "IN PROGRESS" -> !ach.isCompleted();
                    default -> true;
                })
                .collect(Collectors.toList());

        final String sort = sortBtn != null ? sortBtn.getSelectedOption() : "DEFAULT";
        switch (sort) {
            case "PROGRESS" -> filtered.sort(Comparator.comparingInt(Achievement::getProgressPercentage).reversed());
            case "REWARD" -> filtered.sort(Comparator.comparingInt(Achievement::getReward).reversed());
        }

        if (filtered.isEmpty()) {
            final JLabel emptyLabel = new JLabel("No achievements found matching criteria.");
            emptyLabel.setFont(UIScale.scaleFont(RenderCache.SANS_ITALIC_22));
            emptyLabel.setForeground(new Color(150, 150, 180));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            listPanel.setLayout(new GridBagLayout());
            listPanel.add(emptyLabel);
        } else {
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            filtered.stream().map(ach -> new AchievementRowPanel(ach, this::loadAchievements)).forEach(row -> {
                listPanel.add(row);
                listPanel.add(Box.createRigidArea(new Dimension(0, UIScale.scale(10))));
            });
        }

        listPanel.revalidate();
        listPanel.repaint();
    }
}
