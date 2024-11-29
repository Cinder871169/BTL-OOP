package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import managers.GamePanel;
import managers.MusicPlayer;
import managers.SpaceshipSelectionPanel;
import objects.Spaceship;

public class MainMenu extends JPanel {
    private JButton startButton, exitButton, spaceshipSelectButton;
    private MusicPlayer musicPlayer;
    private List<BufferedImage> menuImages; // Danh sách các ảnh
    private int currentImageIndex = 0; // Chỉ số ảnh hiện tại
    private Timer animationTimer; // Timer để thực hiện animation
    private BufferedImage logo;

    public MainMenu() {
        musicPlayer = new MusicPlayer("/sound/menu.wav");
        musicPlayer.loop();

        try {
            logo = ImageIO.read(getClass().getResource("/images/logo.png")); // Đảm bảo logo.png có trong folder /images
        } catch (Exception e) {
            e.printStackTrace();
        }

        menuImages = new ArrayList<>();
        loadImagesFromFolder("/images/menu"); // Load tất cả ảnh từ folder

        setLayout(null);
        setPreferredSize(new Dimension(GamePanel.WIDTH, GamePanel.HEIGHT));
        setBackground(Color.BLACK);

        // Tạo các nút bấm
        startButton = new JButton("Start");
        startButton.setBounds(500, 200, 200, 50);
        startButton.setFont(new Font("Arial", Font.BOLD, 24));
        startButton.addActionListener(e -> startGame());

        spaceshipSelectButton = new JButton("Select Spaceship");
        spaceshipSelectButton.setBounds(500, 300, 200, 50);
        spaceshipSelectButton.setFont(new Font("Arial", Font.BOLD, 14));
        spaceshipSelectButton.addActionListener(e -> openSpaceshipSelection());

        exitButton = new JButton("Exit");
        exitButton.setBounds(500, 400, 200, 50);
        exitButton.setFont(new Font("Arial", Font.BOLD, 24));
        exitButton.addActionListener(e -> System.exit(0));

        add(startButton);
        add(spaceshipSelectButton);
        add(exitButton);

        // Khởi động Timer để thay đổi ảnh
        animationTimer = new Timer(30, e -> {
            currentImageIndex = (currentImageIndex + 1) % menuImages.size();
            repaint(); // Yêu cầu vẽ lại panel
        });
        animationTimer.start();
    }

    private void loadImagesFromFolder(String folderPath) {
        try {
            File folder = new File(getClass().getResource(folderPath).toURI());
            File[] imageFiles = folder.listFiles((dir, name) -> name.endsWith(".png") || name.endsWith(".jpg"));

            if (imageFiles != null) {
                for (File file : imageFiles) {
                    BufferedImage img = ImageIO.read(file);
                    menuImages.add(img);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openSpaceshipSelection() {
        JFrame spaceshipSelectionFrame = new JFrame("Select Your Spaceship");
        SpaceshipSelectionPanel selectionPanel = new SpaceshipSelectionPanel();
        spaceshipSelectionFrame.add(selectionPanel);
        spaceshipSelectionFrame.pack();
        spaceshipSelectionFrame.setLocationRelativeTo(null);
        spaceshipSelectionFrame.setVisible(true);

        spaceshipSelectionFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                Spaceship selectedSpaceship = selectionPanel.getSelectedSpaceship();
                if (selectedSpaceship != null) {
                    GamePanel.selectedSpaceship = selectedSpaceship;
                }
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Vẽ ảnh nền động (menu background)
        if (!menuImages.isEmpty()) {
            BufferedImage currentImage = menuImages.get(currentImageIndex);
            g.drawImage(currentImage, 0, 0, null);
        }
        
        // Vẽ ảnh logo ở chính giữa trên cùng
        if (logo != null) {
            int logoX = getWidth()/2 - logo.getWidth();  // Xác định vị trí X để căn giữa
            int logoY = 50;  // Vị trí Y cho logo ở trên cùng
            g.drawImage(logo, logoX, logoY,logo.getWidth()*2,logo.getHeight()*2, null);
        }
    }

    private void startGame() {
        animationTimer.stop(); // Dừng animation khi bắt đầu game
        musicPlayer.stop();
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.remove(this);

        GamePanel gamePanel = new GamePanel();
        topFrame.add(gamePanel);
        topFrame.revalidate();
        topFrame.repaint();

        gamePanel.requestFocus();
    }
}
