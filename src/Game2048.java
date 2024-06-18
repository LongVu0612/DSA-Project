
import javax.swing.*;

public class Game2048 extends JFrame {
    public Game2048() {
        setTitle("2048 Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 500);
        setResizable(false);

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Game2048 game = new Game2048();
            game.setVisible(true);
        });
    }
}
