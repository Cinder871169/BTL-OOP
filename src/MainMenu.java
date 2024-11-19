import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenu extends JPanel {
    private JButton startButton, exitButton;

    public MainMenu() {
        setLayout(null);
        setPreferredSize(new Dimension(GamePanel.WIDTH, GamePanel.HEIGHT));
        setBackground(Color.BLACK);

        startButton = new JButton("Start");
        startButton.setBounds(500, 300, 200, 50);
        startButton.addActionListener(e -> startGame());

        exitButton = new JButton("Exit");
        exitButton.setBounds(500, 400, 200, 50);
        exitButton.addActionListener(e -> System.exit(0));

        add(startButton);
        add(exitButton);
    }

    private void startGame() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.remove(this);

        GamePanel gamePanel = new GamePanel();
        topFrame.add(gamePanel);
        topFrame.revalidate();
        topFrame.repaint();

        gamePanel.requestFocus(); // Ensure the game panel can receive key events
    }
}
