import java.awt.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SpaceshipSelectionPanel extends JPanel {
    private static final int SPACESHIP_IMAGE_WIDTH = 100;
    private static final int SPACESHIP_IMAGE_HEIGHT = 80;
    private static final Color SELECTED_COLOR = new Color(150, 200, 150); // Màu xanh nhạt khi chọn
    private static final Color HOVER_COLOR = new Color(200, 220, 200); // Màu khi hover
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 16);
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 14);

    private JPanel[] spaceshipPanels;
    private Spaceship[] spaceships;
    private Spaceship selectedSpaceship;
    private int selectedIndex = -1; // Lưu index của tàu vũ trụ được chọn

    public SpaceshipSelectionPanel() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Load spaceship details
        spaceships = new Spaceship[3];
        spaceshipPanels = new JPanel[3];
        try {
            spaceships[0] = loadSpaceship("spaceship1");
            spaceships[1] = loadSpaceship("spaceship2");
            spaceships[2] = loadSpaceship("spaceship3");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading spaceship data!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create panels for spaceship selection
        for (int i = 0; i < spaceships.length; i++) {
            spaceshipPanels[i] = createSpaceshipPanel(i);
            add(spaceshipPanels[i]);
            add(Box.createHorizontalStrut(20)); // Khoảng cách giữa các panel
        }
    }

    private Spaceship loadSpaceship(String spaceshipKey) throws IOException {
        return new Spaceship(
                ImageIO.read(getClass().getResource(ConfigLoader.getString(spaceshipKey + ".image"))),
                ConfigLoader.getInt(spaceshipKey + ".hp"),
                ConfigLoader.getInt(spaceshipKey + ".damage"),
                ConfigLoader.getInt(spaceshipKey + ".speed")
        );
    }

    private JPanel createSpaceshipPanel(int index) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Resize spaceship image
        Image scaledImage = spaceships[index].getImage().getScaledInstance(SPACESHIP_IMAGE_WIDTH, SPACESHIP_IMAGE_HEIGHT, Image.SCALE_SMOOTH);

        JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(imageLabel);

        // Add Spaceship info labels
        panel.add(createSpaceshipInfoLabel("HP: " + spaceships[index].getHp()));
        panel.add(createSpaceshipInfoLabel("Damage: " + spaceships[index].getDamage()));
        panel.add(createSpaceshipInfoLabel("Speed: " + spaceships[index].getSpeed()));

        JButton selectButton = new JButton("Select");
        selectButton.setFont(BUTTON_FONT);
        selectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectSpaceship(index);
            }
        });
        panel.add(selectButton);

        // Mouse hover effect
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (selectedIndex != index) {
                    panel.setBackground(HOVER_COLOR); // Màu khi di chuột vào
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (selectedIndex != index) {
                    panel.setBackground(null); // Reset màu khi di chuột ra ngoài
                }
            }
        });

        return panel;
    }

    private JLabel createSpaceshipInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private void selectSpaceship(int index) {
        // Cập nhật thông tin tàu vũ trụ được chọn
        if (selectedIndex == index) {
            return; // Nếu tàu vũ trụ đã được chọn, không thực hiện gì nữa
        }

        selectedSpaceship = spaceships[index];
        selectedIndex = index;
    
        // Cập nhật màu nền và hiệu ứng hình ảnh cho panel tàu vũ trụ
        for (int i = 0; i < spaceshipPanels.length; i++) {
            if (i == index) {
                spaceshipPanels[i].setBackground(SELECTED_COLOR); // Đổi màu nền khi được chọn
                spaceshipPanels[i].setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2)); // Thêm viền vàng cho panel được chọn
            } else {
                spaceshipPanels[i].setBackground(null); // Reset màu nền
                spaceshipPanels[i].setBorder(null); // Loại bỏ viền khi không chọn
            }
            spaceshipPanels[i].repaint(); // Vẽ lại panel để hiển thị thay đổi
        }
    
        // Hiển thị tàu vũ trụ đã được chọn trong console
        System.out.println("Spaceship " + (index + 1) + " selected!");

        JOptionPane.showMessageDialog(this, "You have selected Spaceship " + (index + 1), "Selection Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    public Spaceship getSelectedSpaceship() {
        return selectedSpaceship;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }
}