import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameMenu extends JFrame {

    public GameMenu() {
        setTitle("Game Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        // Initialize and display the start menu
        initMenu();
    }

    private void initMenu() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(3, 1, 10, 10));

        JLabel titleLabel = new JLabel("Welcome to the Game", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        JButton startButton = new JButton("Start Game");
        JButton exitButton = new JButton("Exit");

        // Start Game Button Action
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });

        // Exit Button Action
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        menuPanel.add(titleLabel);
        menuPanel.add(startButton);
        menuPanel.add(exitButton);

        add(menuPanel);
        setVisible(true);
    }

    private void startGame() {
        getContentPane().removeAll(); // Clear the start menu
        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        revalidate();
        pack();
        gamePanel.requestFocusInWindow();
    }
}
