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
import java.nio.file.Path;

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


        final JPanel textPanel = getJPanel(title, artist);

        final JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        rightPanel.setOpaque(false);

        final PrimaryButton playBtn = new PrimaryButton("DOWNLOAD");
        playBtn.addActionListener(e -> {
            playBtn.setEnabled(false);
            playBtn.setText("WAIT...");
            new SwingWorker<Path, Void>() {
                @Override
                protected Path doInBackground() throws Exception {
                    return audiusClient.downloadMusic(id, title).get();
                }

                @Override
                protected void done() {
                    playBtn.setText("PLAY");
                    playBtn.setEnabled(true);
                    for (var al : playBtn.getActionListeners()) playBtn.removeActionListener(al);
                    playBtn.addActionListener(playEvent -> {
                        screenManager.initScreen(GameScreen.class);
                        final GameScreen gameScreen = screenManager.getScreen(GameScreen.class);
                        try {
                            gameScreen.setupGamePanel(get());
                            screenManager.showScreen(GameScreen.class);
                            gameScreen.start();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                }
            }.execute();
        });
        rightPanel.add(playBtn);
        add(textPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private static JPanel getJPanel(String title, String artist) {
        final JPanel textPanel = new JPanel(new GridLayout(3, 1, 0, 0));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(4, 0, 4, 0));

        final JLabel titleLbl = new JLabel(title.length() > 70 ? title.substring(0, 67) + "..." : title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);

        final JLabel artistLbl = new JLabel(artist);
        artistLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        artistLbl.setForeground(new Color(180, 180, 180));

        textPanel.add(titleLbl);
        textPanel.add(artistLbl);
        return textPanel;
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