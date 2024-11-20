import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MainMenu extends JPanel {
    private JButton startButton, exitButton, spaceshipSelectButton;
    private MusicPlayer musicPlayer;
    private BufferedImage menuImg;

    public MainMenu() {
        musicPlayer = new MusicPlayer("/sound/menu.wav");
        musicPlayer.loop();

        try {
            menuImg = ImageIO.read(getClass().getResourceAsStream("/images/menu.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        setLayout(null);

        setPreferredSize(new Dimension(GamePanel.WIDTH, GamePanel.HEIGHT));
        setBackground(Color.BLACK);

        // Create buttons
        startButton = new JButton("Start");
        startButton.setBounds(500, 200, 200, 50);
        startButton.setFont(new Font("Arial", Font.BOLD, 24)); // Set the font size
        startButton.addActionListener(e -> startGame());

        spaceshipSelectButton = new JButton("Select Spaceship");
        spaceshipSelectButton.setBounds(500, 300, 200, 50);
        spaceshipSelectButton.setFont(new Font("Arial", Font.BOLD, 14));
        spaceshipSelectButton.addActionListener(e -> openSpaceshipSelection());

        exitButton = new JButton("Exit");
        exitButton.setBounds(500, 400, 200, 50);
        exitButton.setFont(new Font("Arial", Font.BOLD, 24)); // Set the font size
        exitButton.addActionListener(e -> System.exit(0));

        add(startButton);
        add(spaceshipSelectButton);
        add(exitButton);
    }

    private void openSpaceshipSelection() {
        JFrame spaceshipSelectionFrame = new JFrame("Select Your Spaceship");
        SpaceshipSelectionPanel selectionPanel = new SpaceshipSelectionPanel();
        spaceshipSelectionFrame.add(selectionPanel);
        spaceshipSelectionFrame.pack();
        spaceshipSelectionFrame.setLocationRelativeTo(null);
        spaceshipSelectionFrame.setVisible(true);

        // Khi người chơi chọn tàu, cập nhật tàu vào trò chơi
        spaceshipSelectionFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                Spaceship selectedSpaceship = selectionPanel.getSelectedSpaceship();
                if (selectedSpaceship != null) {
                    GamePanel.selectedSpaceship = selectedSpaceship; // Truyền tàu vào GamePanel
                }
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(menuImg, 0, 0, null);
    }

    private void startGame() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.remove(this);

        GamePanel gamePanel = new GamePanel();
        topFrame.add(gamePanel);
        topFrame.revalidate();
        topFrame.repaint();

        gamePanel.requestFocus();
    }
}
