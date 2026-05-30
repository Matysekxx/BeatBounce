package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.gui.RenderCache;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.model.achievement.Achievement;
import cz.matysekxx.beatbounce.model.achievement.AchievementManager;
import cz.matysekxx.beatbounce.util.UIScale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;


/**
 * The main container for the achievements' dashboard.
 * Features a filterable and sortable list of all game achievements.
 * Includes a top navigation bar with {@link CycleButton}s for UI control.
 *
 * @author Matysekxx
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

        addComponentListener(new ComponentAdapter() {
            /**
             * Handles the component resized event to revalidate the panel.
             * @param e the component event
             */
            @Override
            public void componentResized(ComponentEvent e) {
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

    /**
     * Draws the background of the panel using the standard menu background.
     *
     * @param g2d the graphics context
     * @param w   the width of the panel
     * @param h   the height of the panel
     */
    @Override
    protected void drawBackground(Graphics2D g2d, int w, int h) {
        RenderUtils.drawMenuBackground(g2d, w, h);
    }


    /**
     * Creates the top bar with title and control buttons.
     */
    private JPanel createTopBar() {
        final JPanel topBar = getTopBar();

        final JLabel titleLabel = new JLabel("ACHIEVEMENTS");
        titleLabel.setFont(UIScale.scaleFont(RenderCache.SANS_BOLD_28));
        titleLabel.setForeground(RenderUtils.cyan);
        titleLabel.setVerticalAlignment(SwingConstants.CENTER);
        topBar.add(titleLabel, BorderLayout.WEST);

        final JPanel controls = getJPanel();

        final JLabel filterLabel = new JLabel("FILTER:");
        filterLabel.setFont(UIScale.scaleFont(RenderCache.AUDIOWIDE_24));
        filterLabel.setForeground(new Color(200, 200, 220));
        controls.add(filterLabel);

        filterBtn = new CycleButton(new String[]{"ALL", "READY TO CLAIM", "IN PROGRESS"}, 0) {
            /**
             * Returns the preferred size of the filter button.
             * @return the preferred dimension
             */
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(UIScale.scale(190), UIScale.scale(36));
            }
        };
        filterBtn.setFont(UIScale.scaleFont(RenderCache.MONTSERRAT_BLACK_14));
        filterBtn.addActionListener(_ -> loadAchievements());
        controls.add(filterBtn);

        controls.add(Box.createRigidArea(new Dimension(UIScale.scale(10), 0)));

        final JLabel sortLabel = new JLabel("SORT:");
        sortLabel.setFont(UIScale.scaleFont(RenderCache.MONTSERRAT_BLACK_14));
        sortLabel.setForeground(new Color(200, 200, 220));
        controls.add(sortLabel);


        sortBtn = new CycleButton(new String[]{"DEFAULT", "PROGRESS", "REWARD"}, 0) {
            /**
             * Returns the preferred size of the sort button.
             * @return the preferred dimension
             */
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(UIScale.scale(160), UIScale.scale(36));
            }
        };
        sortBtn.setFont(UIScale.scaleFont(RenderCache.MONTSERRAT_BLACK_14));
        sortBtn.addActionListener(_ -> loadAchievements());
        controls.add(sortBtn);

        topBar.add(controls, BorderLayout.EAST);
        return topBar;
    }

    private JPanel getTopBar() {
        final JPanel topBar = new JPanel(new BorderLayout()) {
            /**
             * Returns the preferred size of the top bar.
             * @return the preferred dimension
             */
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, UIScale.scale(70));
            }

            /**
             * Returns the insets for the top bar.
             * @return the insets
             */
            @Override
            public Insets getInsets() {
                return new Insets(0, UIScale.scale(30), 0, UIScale.scale(30));
            }
        };
        topBar.setOpaque(false);
        return topBar;
    }

    /**
     * Creates and configures the control panel for filtering and sorting.
     *
     * @return a configured JPanel for UI controls
     */
    private JPanel getJPanel() {
        final JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, UIScale.scale(15), UIScale.scale(12))) {
            /**
             * Lays out the components in the panel.
             */
            @Override
            public void doLayout() {
                setFlowLayoutGap(this);
                super.doLayout();
            }

            /**
             * Sets the gap for the flow layout based on UI scale.
             * @param p the panel to configure
             */
            private void setFlowLayoutGap(JPanel p) {
                final LayoutManager lm = p.getLayout();
                if (lm instanceof FlowLayout fl) {
                    fl.setHgap(UIScale.scale(15));
                    fl.setVgap(UIScale.scale(12));
                }
            }
        };
        controls.setOpaque(false);
        return controls;
    }


    /**
     * Fetches achievements from the manager, applies current filters and sorting,
     * and repopulates the list panel.
     */
    public void loadAchievements() {
        if (listPanel == null) return;
        listPanel.removeAll();
        listPanel.setBorder(new EmptyBorder(UIScale.scale(10), UIScale.scale(20), UIScale.scale(20), UIScale.scale(20)) {
            /**
             * Returns the insets of the border.
             * @param c the component
             * @return the insets
             */
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
