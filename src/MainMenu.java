import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class MainMenu extends JPanel {
    private JButton startButton, exitButton;
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
        startButton.setBounds(500, 300, 200, 50);
        startButton.setFont(new Font("Arial", Font.BOLD, 24)); // Set the font size
        startButton.addActionListener(e -> startGame());

        exitButton = new JButton("Exit");
        exitButton.setBounds(500, 400, 200, 50);
        exitButton.setFont(new Font("Arial", Font.BOLD, 24)); // Set the font size
        exitButton.addActionListener(e -> System.exit(0));

        add(startButton);
        add(exitButton);
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
