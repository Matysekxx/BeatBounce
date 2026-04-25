package cz.matysekxx.beatbounce.gui.components;

import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.gui.RenderUtils;
import cz.matysekxx.beatbounce.gui.screen.GameScreen;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TrackCard extends JPanel {
    private int hoverAlpha = 120;
    private float animOffset = 30f;
    private float animAlpha = 0f;
    private Timer enterTimer;

    public TrackCard(String id, String title, String artist, AudiusClient audiusClient, ScreenManager screenManager, int index) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(1000, 85));
        setMaximumSize(new Dimension(1000, 85));
        setBorder(new EmptyBorder(10, 15, 10, 20));
        setToolTipText(title + " by " + artist);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hoverAlpha = 180;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverAlpha = 120;
                repaint();
            }
        });

        enterTimer = new Timer(16, e -> {
            animOffset = Math.max(0, animOffset - 2f);
            animAlpha = Math.min(1f, animAlpha + 0.05f);
            if (animOffset == 0 && animAlpha >= 1f) enterTimer.stop();
            repaint();
        });
        enterTimer.setInitialDelay(index * 40);
        enterTimer.start();

        final JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(80, 70));
        leftPanel.setBorder(new EmptyBorder(0, 5, 0, 20));
        leftPanel.add(createAlbumArt(id));


        final JPanel textPanel = getJPanel(id, title, artist);

        final JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        rightPanel.setOpaque(false);

        final PrimaryButton playBtn = new PrimaryButton("DOWNLOAD");
        playBtn.addActionListener(e -> {
            playBtn.setEnabled(false);
            playBtn.setText("WAIT...");
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    String safeName = title.replaceAll("[^a-zA-Z0-9.-]", "_");
                    audiusClient.downloadMusic(id, safeName).get();
                    return null;
                }

                @Override
                protected void done() {
                    playBtn.setText("PLAY");
                    playBtn.setEnabled(true);
                    for (var al : playBtn.getActionListeners()) playBtn.removeActionListener(al);
                    playBtn.addActionListener(playEvent -> {
                        screenManager.initScreen(GameScreen.class);
                        screenManager.showScreen(GameScreen.class);
                    });
                }
            }.execute();
        });
        rightPanel.add(playBtn);
        add(leftPanel, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private static JPanel getJPanel(String id, String title, String artist) {
        final JPanel textPanel = new JPanel(new GridLayout(3, 1, 0, 0));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(8, 0, 8, 0));

        final JLabel titleLbl = new JLabel(title.length() > 70 ? title.substring(0, 67) + "..." : title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);

        final JLabel artistLbl = new JLabel(artist);
        artistLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        artistLbl.setForeground(new Color(180, 180, 180));

        final int diffLevel = Math.abs(id.hashCode() % 3);
        String diffText = diffLevel == 0 ? "EASY" : (diffLevel == 1 ? "NORMAL" : "HARD");
        String diffColorHex = diffLevel == 0 ? "#4CAF50" : (diffLevel == 1 ? "#FFC107" : "#FF5252");

        final JLabel tagsLbl = new JLabel("<html><font color='" + diffColorHex + "'><b>DIFFICULTY: " + diffText + "</b></font> &nbsp;&nbsp;<font color='#888888'>ELECTRONIC</font></html>");
        tagsLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));

        textPanel.add(titleLbl);
        textPanel.add(artistLbl);
        textPanel.add(tagsLbl);
        return textPanel;
    }

    private JPanel createAlbumArt(String trackId) {
        final int hash = trackId.hashCode();
        final Color c1 = new Color((hash & 0xFF0000) >> 16, (hash & 0x00FF00) >> 8, (hash & 0x0000FF));
        final Color c2 = new Color((hash & 0x0000FF) << 8 | 0xFF);
        final JPanel albumArt = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderUtils.initGraphic2D(g2);
                g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(0, 0, 0, 40));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        albumArt.setOpaque(false);
        albumArt.setPreferredSize(new Dimension(55, 55));
        return albumArt;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, animAlpha));
        g2.translate(0, (int) animOffset);
        super.paint(g2);
        g2.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        RenderUtils.initGraphic2D(g2);
        g2.setColor(new Color(25, 25, 30, hoverAlpha));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.setColor(new Color(255, 255, 255, hoverAlpha > 120 ? 30 : 10));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.dispose();
    }
}