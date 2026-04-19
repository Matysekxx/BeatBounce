package cz.matysekxx.beatbounce.gui.screen;

import cz.matysekxx.beatbounce.gui.ButtonFactory;

import javax.swing.*;
import java.awt.*;

import static cz.matysekxx.beatbounce.util.Time.sleep;

public class IntroScreen extends Screen {

    public IntroScreen(ScreenManager screenManager) {
        super();
        this.setLayout(new BorderLayout());
        final IntroBackgroundPanel backgroundPanel = new IntroBackgroundPanel(new Color(92, 79, 244), new Color(13, 31, 140, 240));
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
        //TODO: pridat animaci pro intro screen pozadi
    }

    private static class IntroBackgroundPanel extends JPanel {
        private final Color firstColor;
        private final Color secondColor;

        public IntroBackgroundPanel(Color firstColor, Color secondColor) {
            super();
            this.firstColor = firstColor;
            this.secondColor = secondColor;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            final Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, firstColor, 0, getHeight(), secondColor);

            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            final String text = "BEAT BOUNCE";
            g2d.setFont(new Font("Monospaced", Font.BOLD, 90));

            final int x = (getWidth() - g2d.getFontMetrics().stringWidth(text)) >> 1;
            final int y = (getHeight() >> 1);

            final Color neonPink = new Color(234, 255, 249, 255);

            g2d.setColor(neonPink);
            g2d.drawString(text, x, y);

            g2d.dispose();
        }
    }
}
