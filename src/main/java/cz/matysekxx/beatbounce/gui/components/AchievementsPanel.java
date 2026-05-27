package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.achievement.Achievement;
import cz.matysekxx.beatbounce.model.achievement.AchievementManager;
import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;


/**
 * The main container for the achievements dashboard.
 * Features a filterable and sortable list of all game achievements.
 * Includes a top navigation bar with {@link CycleButton}s for UI control.
 */
public class AchievementsPanel extends BasePanel {

    /**
     * The internal container holding the achievement rows.
     */
    private final JPanel listPanel;

    /**
     * Button to filter by status (All, Claimable, In Progress).
     */
    private CycleButton filterBtn;

    /**
     * Button to sort by different criteria.
     */
    private CycleButton sortBtn;

    /**
     * Constructs a new AchievementsPanel and initializes its layout.
     */
    public AchievementsPanel() {
        super();
        setOpaque(false);
        setLayout(new BorderLayout());
        final JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        final JScrollPane scrollPane = buildScrollPane(listPanel);
        add(scrollPane, BorderLayout.CENTER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                revalidate();
            }
        });

        loadAchievements();
    }


    /**
     * Configures a standard scroll pane for achievement rows.
     */
    private static JScrollPane buildScrollPane(JPanel content) {
        return SongSelectionPanel.buildScrollPane(content);
    }

    @Override
    protected void drawBackground(Graphics2D g2d, int w, int h) {
        RenderUtils.drawMenuBackground(g2d, w, h);
    }


    /**
     * Creates the top bar with title and control buttons.
     */
    private JPanel createTopBar() {
        final JPanel topBar = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, UIScale.scale(70));
            }

            @Override
            public Insets getInsets() {
                return new Insets(0, UIScale.scale(30), 0, UIScale.scale(30));
            }
        };
        topBar.setOpaque(false);

        final JLabel titleLabel = new JLabel("ACHIEVEMENTS");
        titleLabel.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_28));
        titleLabel.setForeground(RenderUtils.cyan);
        titleLabel.setVerticalAlignment(SwingConstants.CENTER);
        topBar.add(titleLabel, BorderLayout.WEST);

        final JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, UIScale.scale(15), UIScale.scale(12))) {
            @Override
            public void doLayout() {
                setFlowLayoutGap(this);
                super.doLayout();
            }

            private void setFlowLayoutGap(JPanel p) {
                LayoutManager lm = p.getLayout();
                if (lm instanceof FlowLayout fl) {
                    fl.setHgap(UIScale.scale(15));
                    fl.setVgap(UIScale.scale(12));
                }
            }
        };
        controls.setOpaque(false);

        final JLabel filterLabel = new JLabel("FILTER:");
        filterLabel.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_13));
        filterLabel.setForeground(new Color(200, 200, 220));
        controls.add(filterLabel);

        filterBtn = new CycleButton(new String[]{"ALL", "READY TO CLAIM", "IN PROGRESS"}, 0) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(UIScale.scale(170), UIScale.scale(36));
            }
        };
        filterBtn.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_14));
        filterBtn.addActionListener(_ -> loadAchievements());
        controls.add(filterBtn);

        controls.add(Box.createRigidArea(new Dimension(UIScale.scale(10), 0)));

        final JLabel sortLabel = new JLabel("SORT:");
        sortLabel.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_13));
        sortLabel.setForeground(new Color(200, 200, 220));
        controls.add(sortLabel);

        sortBtn = new CycleButton(new String[]{"DEFAULT", "PROGRESS", "REWARD"}, 0) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(UIScale.scale(150), UIScale.scale(36));
            }
        };
        sortBtn.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_14));
        sortBtn.addActionListener(_ -> loadAchievements());
        controls.add(sortBtn);

        topBar.add(controls, BorderLayout.EAST);
        return topBar;
    }


    /**
     * Fetches achievements from the manager, applies current filters and sorting,
     * and repopulates the list panel.
     */
    public void loadAchievements() {
        if (listPanel == null) return;
        listPanel.removeAll();
        listPanel.setBorder(new EmptyBorder(UIScale.scale(10), UIScale.scale(20), UIScale.scale(20), UIScale.scale(20)) {
            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(UIScale.scale(10), UIScale.scale(20), UIScale.scale(20), UIScale.scale(20));
            }
        });

        final List<Achievement> list = AchievementManager.getAchievements();

        final String filter = filterBtn != null ? filterBtn.getSelectedOption() : "ALL";
        final String sort = sortBtn != null ? sortBtn.getSelectedOption() : "DEFAULT";

        final List<Achievement> filtered = AchievementManager.filterAchievements(list, filter);
        final List<Achievement> sorted = AchievementManager.sortAchievements(filtered, sort);

        if (sorted.isEmpty()) {
            final JLabel emptyLabel = new JLabel("No achievements found matching criteria.");
            emptyLabel.setFont(UIScale.scaleFont(RenderCache.SANS_ITALIC_22));
            emptyLabel.setForeground(new Color(150, 150, 180));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            listPanel.setLayout(new GridBagLayout());
            listPanel.add(emptyLabel);
        } else {
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            sorted.stream()
                    .map(ach -> new AchievementRowPanel(ach, this::loadAchievements))
                    .forEach(row -> {
                        listPanel.add(row);
                        listPanel.add(Box.createRigidArea(new Dimension(0, UIScale.scale(10))));
                    });
        }

        listPanel.revalidate();
        listPanel.repaint();
    }
}
}
