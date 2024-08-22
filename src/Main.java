import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.imageio.ImageIO;

class Objects {
    public int x, y, vx, vy, width, height;
}

class BackPanel extends JPanel implements Runnable, KeyListener {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    public static final int SIZE = 16 * 3;

    private BufferedImage backgroundImage;
    private BufferedImage playerDisplay;
    private BufferedImage defaultImage;
    private Objects player;
    private Thread thread;

    public BackPanel() {
        try {
            backgroundImage = ImageIO.read(getClass().getResourceAsStream("/image/background.png"));
            defaultImage = ImageIO.read(getClass().getResourceAsStream("/image/sprite.png"));
            playerDisplay = defaultImage.getSubimage(16 * 2, 0, 16, 16);
        } catch (Exception e) {
            e.printStackTrace();
        }

        player = new Objects();
        player.width = player.height = SIZE;
        player.x = WIDTH / 2 - SIZE / 2;
        player.y = HEIGHT / 2 - SIZE / 2;

        thread = new Thread(this);
        thread.start();

        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.setBackground(new Color(150, 255, 245));
        this.addKeyListener(this);
    }

    public void update() {
        player.x += player.vx;
        player.y += player.vy;

        if (player.x < 0)
            player.x = 0;
        if (player.y < 0)
            player.y = 0;
        if (player.x > WIDTH - player.width)
            player.x = WIDTH - player.width;
        if (player.y > HEIGHT - player.height)
            player.y = HEIGHT - player.height;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, null);
        g.drawImage(playerDisplay, player.x, player.y, SIZE, SIZE, null);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_LEFT -> player.vx = -5;
            case KeyEvent.VK_RIGHT -> player.vx = 5;
            case KeyEvent.VK_UP -> player.vy = -5;
            case KeyEvent.VK_DOWN -> player.vy = 5;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT)
            player.vx = 0;
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN)
            player.vy = 0;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void run() {
        long t1 = System.nanoTime();
        double delta = 0.0;
        double ticks = 60.0;

        while (thread != null) {
            long t2 = System.nanoTime();
            delta += (t2 - t1) / (1e9 / ticks);
            t1 = t2;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }
}

public class Main extends JFrame {

    private final BackPanel backPanel = new BackPanel();

    public Main() {
        this.setTitle("Space War");
        this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.add(backPanel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        new Main();
    }
}
