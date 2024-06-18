import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

public class GamePanel extends JPanel {
    private static final int GRID_SIZE = 4;
    private static final int TILE_SIZE = 100;
    private static final int TILE_MARGIN = 16;
    private Tile[][] tiles;
    private int score;
    private Stack<Integer> highScores;
    private Map<String, Integer> savedGame;

    public GamePanel() {
        setPreferredSize(new Dimension(500, 500));
        setFocusable(true);
        highScores = new Stack<>();
        savedGame = new HashMap<>();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                boolean needRepaint = false;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        moveLeft();
                        needRepaint = true;
                        break;
                    case KeyEvent.VK_RIGHT:
                        moveRight();
                        needRepaint = true;
                        break;
                    case KeyEvent.VK_UP:
                        moveUp();
                        needRepaint = true;
                        break;
                    case KeyEvent.VK_DOWN:
                        moveDown();
                        needRepaint = true;
                        break;
                    case KeyEvent.VK_R:
                        resetGame();
                        needRepaint = true;
                        break;
                    case KeyEvent.VK_S: // Save game
                        saveGame();
                        needRepaint = false;
                        break;
                    case KeyEvent.VK_L: // Load game
                        loadGame();
                        needRepaint = true;
                        break;
                }
                if (needRepaint) {
                    repaint();
                }
            }
        });
        resetGame();
    }

    public void resetGame() {
        tiles = new Tile[GRID_SIZE][GRID_SIZE];
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                tiles[row][col] = new Tile();
            }
        }
        score = 0;
        addTile();
        addTile();
    }

    private void addTile() {
        Random random = new Random();
        int row, col;
        do {
            row = random.nextInt(GRID_SIZE);
            col = random.nextInt(GRID_SIZE);
        } while (!tiles[row][col].isEmpty());
        tiles[row][col].setValue(random.nextInt(10) == 0 ? 4 : 2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0xBBADA0));
        g.fillRect(0, 0, getWidth(), getHeight());
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                drawTile(g, tiles[row][col], col, row);
            }
        }
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.drawString("Score: " + score, 20, getHeight() - 20);
        g.drawString("Highest Score: " + getHighestScore(), 20, getHeight() - 50);
    }

    private void drawTile(Graphics g, Tile tile, int x, int y) {
        int value = tile.getValue();
        int xOffset = offsetCoors(x);
        int yOffset = offsetCoors(y);
        g.setColor(tile.getBackground());
        g.fillRect(xOffset, yOffset, TILE_SIZE, TILE_SIZE);
        g.setColor(tile.getForeground());
        g.setFont(tile.getTileFont());

        String s = String.valueOf(value);
        FontMetrics fm = getFontMetrics(tile.getTileFont());
        int w = fm.stringWidth(s);
        int h = -(int) fm.getLineMetrics(s, g).getBaselineOffsets()[2];

        if (value != 0) {
            g.drawString(s, xOffset + (TILE_SIZE - w) / 2, yOffset + TILE_SIZE - (TILE_SIZE - h) / 2 - 2);
        }
    }

    private static int offsetCoors(int arg) {
        return arg * (TILE_MARGIN + TILE_SIZE) + TILE_MARGIN;
    }

    private void moveLeft() {
        boolean moved = false;
        for (int row = 0; row < GRID_SIZE; row++) {
            if (compressTiles(tiles[row])) {
                moved = true;
            }
        }
        if (moved) {
            addTile();
            updateScore();
            if (isGameWon()) {
                showGameWonMessage();
            } else if (isGameOver()) {
                showGameOverMessage();
            }
        }
    }

    private void moveRight() {
        boolean moved = false;
        for (int row = 0; row < GRID_SIZE; row++) {
            reverseArray(tiles[row]);
            if (compressTiles(tiles[row])) {
                moved = true;
            }
            reverseArray(tiles[row]);
        }
        if (moved) {
            addTile();
            updateScore();
            if (isGameWon()) {
                showGameWonMessage();
            } else if (isGameOver()) {
                showGameOverMessage();
            }
        }
    }

    private void moveUp() {
        boolean moved = false;
        for (int col = 0; col < GRID_SIZE; col++) {
            Tile[] column = new Tile[GRID_SIZE];
            for (int row = 0; row < GRID_SIZE; row++) {
                column[row] = tiles[row][col];
            }
            if (compressTiles(column)) {
                moved = true;
            }
            for (int row = 0; row < GRID_SIZE; row++) {
                tiles[row][col] = column[row];
            }
        }
        if (moved) {
            addTile();
            updateScore();
            if (isGameWon()) {
                showGameWonMessage();
            } else if (isGameOver()) {
                showGameOverMessage();
            }
        }
    }

    private void moveDown() {
        boolean moved = false;
        for (int col = 0; col < GRID_SIZE; col++) {
            Tile[] column = new Tile[GRID_SIZE];
            for (int row = 0; row < GRID_SIZE; row++) {
                column[row] = tiles[row][col];
            }
            reverseArray(column);
            if (compressTiles(column)) {
                moved = true;
            }
            reverseArray(column);
            for (int row = 0; row < GRID_SIZE; row++) {
                tiles[row][col] = column[row];
            }
        }
        if (moved) {
            addTile();
            updateScore();
            if (isGameWon()) {
                showGameWonMessage();
            } else if (isGameOver()) {
                showGameOverMessage();
            }
        }
    }

    private void reverseArray(Tile[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            Tile temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }

    private boolean compressTiles(Tile[] tiles) {
        boolean changed = false;
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i].isEmpty()) {
                for (int j = i + 1; j < tiles.length; j++) {
                    if (!tiles[j].isEmpty()) {
                        tiles[i].setValue(tiles[j].getValue());
                        tiles[j].setValue(0);
                        changed = true;
                        break;
                    }
                }
            }
        }
        for (int i = 0; i < tiles.length - 1; i++) {
            if (!tiles[i].isEmpty() && tiles[i].getValue() == tiles[i + 1].getValue()) {
                tiles[i].setValue(tiles[i].getValue() * 2);
                tiles[i + 1].setValue(0);
                score += tiles[i].getValue(); // Cộng điểm khi kết hợp ô
                changed = true;
            }
        }
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i].isEmpty()) {
                for (int j = i + 1; j < tiles.length; j++) {
                    if (!tiles[j].isEmpty()) {
                        tiles[i].setValue(tiles[j].getValue());
                        tiles[j].setValue(0);
                        changed = true;
                        break;
                    }
                }
            }
        }
        return changed;
    }

    private boolean isGameOver() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (tiles[row][col].isEmpty()) {
                    return false;
                }
                if (col < GRID_SIZE - 1 && tiles[row][col].getValue() == tiles[row][col + 1].getValue()) {
                    return false;
                }
                if (row < GRID_SIZE - 1 && tiles[row][col].getValue() == tiles[row + 1][col].getValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isGameWon() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (tiles[row][col].getValue() == 2048) {
                    return true;
                }
            }
        }
        return false;
    }

    private void showGameWonMessage() {
        JOptionPane.showMessageDialog(this, "Congratulations! You've reached 2048!", "You Win!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showGameOverMessage() {
        JOptionPane.showMessageDialog(this, "Game Over! Your score: " + score, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateScore() {
        // Tính điểm dựa trên giá trị các ô
        int currentscore = 0;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                currentscore += tiles[row][col].getValue();
            }
        }
        score = currentscore;
        if(highScores.isEmpty() || currentscore > highScores.peek()){
            highScores.push(currentscore);
        }
    }

    public int getHighestScore() {
        if (highScores.isEmpty()) {
            return 0; // Hoặc một giá trị mặc định khác nếu cần
        } else {
            return highScores.peek();
        }
    }

    private void saveGame() {
        savedGame.clear();
        savedGame.put("score", score);
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                savedGame.put(row + "," + col, tiles[row][col].getValue());
            }
        }
        JOptionPane.showMessageDialog(this, "Game Saved!", "Save Game", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadGame() {
        if (savedGame.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No saved game found!", "Load Game", JOptionPane.WARNING_MESSAGE);
            return;
        }
        score = savedGame.get("score");
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                tiles[row][col].setValue(savedGame.get(row + "," + col));
            }
        }
        JOptionPane.showMessageDialog(this, "Game Loaded!", "Load Game", JOptionPane.INFORMATION_MESSAGE);
    }
}
