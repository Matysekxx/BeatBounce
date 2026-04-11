package cz.matysekxx.beatbounce.gui.screen;


import java.awt.*;

public class GameScreen extends Screen {
    private final GamePanel gamePanel;
    public GameScreen() {
        super();
        this.setLayout(new BorderLayout());

        gamePanel = new GamePanel(null);
        this.add(gamePanel, BorderLayout.CENTER);
        this.setVisible(true);
        gamePanel.startGame();
    }
}
