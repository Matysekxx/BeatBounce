package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.gui.ButtonFactory;
import cz.matysekxx.beatbounce.gui.Star;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

import static cz.matysekxx.beatbounce.util.Time.sleep;

public class IntroScreen extends Screen {
    private final IntroBackgroundPanel backgroundPanel;

    public IntroScreen(ScreenManager screenManager) {
        super();
        this.setLayout(new BorderLayout());
        backgroundPanel = new IntroBackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout());
        this.setContentPane(backgroundPanel);

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 100));
        buttonPanel.setOpaque(false);

        final JButton startButton = ButtonFactory.createStartButton(e -> {
            sleep(200);
            screenManager.showScreen(GameScreen.class);
        });
        buttonPanel.add(startButton);
        final JButton exitButton = ButtonFactory.createExitButton(e -> {
            sleep(200);
            System.exit(0);
        });
        buttonPanel.add(exitButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override
    public void start() {
        backgroundPanel.startAnimation();
    }

    @Override
    public void stop() {
        backgroundPanel.stopAnimation();
    }

    private static class IntroBackgroundPanel extends JPanel {
        private float time = 0;
        private Timer timer;
        private final Collection<Star> stars = new ArrayList<>(STARS_COUNT);
        private static final int STARS_COUNT = 800;

        public IntroBackgroundPanel() { super(); initStars(); }

        public void startAnimation() {
            if (timer == null) {
                timer = new Timer(16, e -> {
                    time += 0.04f;
                    if (!stars.isEmpty()) {
                        for (Star s : stars) s.update();
                    }
                    repaint();
                });
                timer.start();
            }
        }

        public void stopAnimation() {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
        }

        private void initStars() {
            for (int i = 0; i < STARS_COUNT; i++) stars.add(new Star());
        }


        //TODO: rozsekat metodu do vice metod
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            final Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            final int w = getWidth();
            final int h = getHeight();
            final int horizonY = h / 2 + 50;

            final GradientPaint sky = new GradientPaint(
                    0, 0, new Color(2, 2, 10), 0,
                    horizonY,
                    new Color(40, 5, 60)

            );
            g2d.setPaint(sky);
            g2d.fillRect(0, 0, w, horizonY);

            g2d.translate(w / 2, horizonY - 100);
            for (Star s : stars) {
                final double fov = 300.0;
                final double projX = (s.x / s.z) * fov;
                final double projY = (s.y / s.z) * fov;
                final double size = Math.max(0.5, 4.0 - (s.z / 100.0));

                final int alpha = (int) Math.min(255, Math.max(0, 255 - (s.z * 0.5)));
                g2d.setColor(new Color(0, 255, 255, alpha));
                g2d.fillOval((int) projX, (int) projY, (int) size, (int) size);
            }
            g2d.translate(-w / 2, -(horizonY - 100));

            g2d.setColor(new Color(3, 0, 10));
            g2d.fillRect(0, horizonY, w, h - horizonY);

            g2d.setColor(new Color(0, 255, 255, 140));
            final int vanishingPointX = w / 2;
            for (int i = -30; i <= 30; i++) {
                final int bottomX = vanishingPointX + i * 150;
                g2d.drawLine(vanishingPointX, horizonY, bottomX, h);
            }

            final float gridOffset = time % 1.0f;
            for (int z = 1; z <= 20; z++) {
                final double depth = Math.pow((z + gridOffset) / 20.0, 2.5);
                final int lineY = horizonY + (int) ((h - horizonY) * depth);
                if (lineY > horizonY && lineY <= h) {
                    g2d.drawLine(0, lineY, w, lineY);
                }
            }

            g2d.setColor(new Color(255, 0, 255, 200));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(0, horizonY, w, horizonY);
            g2d.setStroke(new BasicStroke(1));
            g2d.setColor(Color.WHITE);
            g2d.drawLine(0, horizonY, w, horizonY);


            //TODO: predelat jeste nejak Title
            final String text = "BEAT BOUNCE";
            g2d.setFont(new Font("Monospaced", Font.BOLD | Font.ITALIC, 115));
            final FontMetrics fm = g2d.getFontMetrics();
            final int x = (w - fm.stringWidth(text)) >> 1;
            final int y = (h / 3);

            final double pulse = (Math.sin(time * 3) + 1.0) / 2.0;
            for (int i = 12; i >= 1; i -= 2) {
                final int alpha = (int) (10 + (20 * pulse) / i);
                g2d.setColor(new Color(0, 255, 255, alpha));
                g2d.drawString(text, x - i, y - i);
                g2d.drawString(text, x + i, y + i);
                g2d.drawString(text, x - i, y + i);
                g2d.drawString(text, x + i, y - i);
            }

            g2d.setColor(new Color(240, 255, 255));
            g2d.drawString(text, x, y);

            g2d.setColor(new Color(0, 0, 0, 40));
            for (int i = 0; i < h; i += 3) {
                g2d.drawLine(0, i, w, i);
            }

            final RadialGradientPaint vignette = new RadialGradientPaint(
                    w / 2f, h / 2f, w * 0.8f,
                    new float[]{0.4f, 1.0f},
                    new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 220)}
            );
            g2d.setPaint(vignette);
            g2d.fillRect(0, 0, w, h);
            g2d.dispose();
        }
    }
}
