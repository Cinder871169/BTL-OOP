import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    public static final int TILE = 64;

    private boolean gameOver = false;
    private Random random = new Random();

    private BufferedImage backgroundImg;
    private BufferedImage playerImg, bulletImg, enemyImg;

    private Item player, bullet;
    private int playerScore = 0, playerHealth;
    private ArrayList<Item> enemies = new ArrayList<>();
    private int enemyLength = 1;
    private int enemyCount = 0;
    private long startTime;
    private int enemySpawnFrequency;

    private int move = 0; // 0: no movement, 1: right, -1: left
    private int moveY = 0;
    private int shoot = 0; // 0: no shooting, 1: shoot

    private boolean paused = false;
    private Thread thread;

    private MusicPlayer musicPlayer, shootSound, hitSound;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(150, 255, 245));
        setFocusable(true);
        addKeyListener(this);

        loadImages();
        initializeObjects();

        musicPlayer = new MusicPlayer("/sound/theme.wav");
        musicPlayer.loop();

        // Lưu thời gian bắt đầu của trò chơi
        startTime = System.currentTimeMillis();

        thread = new Thread(this);
        thread.start();
    }

    private void loadImages() {
        try {
            backgroundImg = ImageIO.read(getClass().getResource("/images/background.png"));
            playerImg = ImageIO.read(getClass().getResource("/images/player.png"));
            bulletImg = ImageIO.read(getClass().getResource("/images/bullet.png"));
            enemyImg = ImageIO.read(getClass().getResource("/images/enemy.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeObjects() {
        player = new Item();
        player.x = ConfigLoader.getInt("player.initialX");
        player.y = ConfigLoader.getInt("player.initialY");
        player.vx = ConfigLoader.getInt("player.velocityX");
        playerHealth = ConfigLoader.getInt("player.health");

        bullet = new Item();
        bullet.vy = ConfigLoader.getInt("bullet.velocityY");

        enemySpawnFrequency = ConfigLoader.getInt("enemy.spawnFrequency");

        for (int i = 0; i < enemyLength; i++) {
            Item enemy = new Item();
            enemy.x = random.nextInt(WIDTH - TILE);
            enemy.y = -random.nextInt(TILE);
            enemy.vy = ConfigLoader.getInt("enemy.initialVelocityY");
            enemies.add(enemy);
        }
    }

    private void update() {
        enemyCount++;

        // Di chuyển ngang
        if (move == 1 && player.x + TILE < WIDTH)
            player.x += player.vx;
        else if (move == -1 && player.x > 0)
            player.x -= player.vx;

        // Di chuyển dọc (lên hoặc xuống)
        if (moveY == 1 && player.y + TILE < HEIGHT)
            player.y += player.vx; // Dùng velocity dọc để điều chỉnh tốc độ lên xuống
        else if (moveY == -1 && player.y > 0)
            player.y -= player.vx;

        if (shoot == 1) {
            bullet.y -= bullet.vy;
            if (bullet.y < -TILE)
                shoot = 0;
        }
        for (Item enemy : enemies) {
            enemy.y += enemy.vy;

            if (checkCollision(bullet, enemy)) {
                resetEnemy(enemy);
                shoot = 0;
                playerScore += ConfigLoader.getInt("enemy.scoreValue");
                hitSound = new MusicPlayer("/sound/hit.wav");
                hitSound.play();
            }

            if (checkCollision(player, enemy)) {
                int impact = random.nextInt(ConfigLoader.getInt("enemy.healthImpactMax") -
                        ConfigLoader.getInt("enemy.healthImpactMin")) +
                        ConfigLoader.getInt("enemy.healthImpactMin");
                hitSound = new MusicPlayer("/sound/hit.wav");
                hitSound.play();
                playerHealth -= impact;
                resetEnemy(enemy);
            }

            if (enemy.y > HEIGHT + TILE) {
                int impact = random.nextInt(ConfigLoader.getInt("enemy.healthImpactMax") -
                        ConfigLoader.getInt("enemy.healthImpactMin")) +
                        ConfigLoader.getInt("enemy.healthImpactMin");
                playerHealth -= impact;
                resetEnemy(enemy);
            }
        }

        if (playerHealth <= 0) {
            playerHealth = 0;
            gameOver = true;
            musicPlayer.stop();
        }

        if (enemyCount % enemySpawnFrequency == 0) {
            enemyLength++;
            Item newEnemy = new Item();
            newEnemy.x = random.nextInt(WIDTH - TILE);
            newEnemy.y = -random.nextInt(TILE);
            newEnemy.vy = ConfigLoader.getInt("enemy.initialVelocityY");
            enemies.add(newEnemy);
        }
    }

    private boolean checkCollision(Item a, Item b) {
        int dx = (a.x - b.x) * (a.x - b.x);
        int dy = (a.y - b.y) * (a.y - b.y);
        double distance = Math.sqrt(dx + dy);
        return distance < TILE;
    }

    private void resetEnemy(Item enemy) {
        enemy.x = random.nextInt(WIDTH - TILE);
        enemy.y = -random.nextInt(TILE);
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double delta = 0.0;
        double tickRate = 60.0;

        while (thread != null) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / (1e9 / tickRate);
            lastTime = currentTime;

            if (delta >= 1) {
                if (!gameOver && !paused) {
                    update();
                }
                repaint();
                delta--;
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImg, 0, 0, null);

        if (shoot == 1)
            g.drawImage(bulletImg, bullet.x, bullet.y, TILE, TILE, null);
        if (!gameOver)
            g.drawImage(playerImg, player.x, player.y, TILE, TILE, null);

        for (Item enemy : enemies)
            g.drawImage(enemyImg, enemy.x, enemy.y, TILE, TILE, null);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 15));
        g.drawString("SCORE  " + playerScore, 5, 15);
        // Health bar dimensions
        int healthBarX = 5, healthBarY = 40, healthBarWidth = 150, healthBarHeight = 15;
        int healthFillWidth = (int) ((playerHealth / 100.0) * healthBarWidth);

        // Fill health bar
        g.setColor(Color.RED);
        g.fillRect(healthBarX + 1, healthBarY + 1, healthFillWidth, healthBarHeight - 1);

        // Add health label
        g.setFont(new Font("Arial", Font.PLAIN, 15));
        g.setColor(Color.WHITE);
        g.drawString("HEALTH", healthBarX, healthBarY - 5);

        // Tính toán thời gian trôi qua
        long elapsedTime = System.currentTimeMillis() - startTime;
        long seconds = (elapsedTime / 1000) % 60;
        long minutes = (elapsedTime / (1000 * 60)) % 60;
        long hours = (elapsedTime / (1000 * 60 * 60)) % 24;

        // Hiển thị thời gian theo định dạng giờ:phút:giây
        g.drawString(String.format("TIME   %02d:%02d:%02d", hours, minutes, seconds), 5, 70);

        if (paused) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("PAUSED", WIDTH / 2 - 100, HEIGHT / 2 - 50);
            g.drawString("Press R to return to main menu", WIDTH / 2 - 300, HEIGHT / 2 + 50);
            g.drawRect(WIDTH / 2 - 320, HEIGHT / 2 - 100, 640, 200);
        }

        if (gameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("GAME OVER", WIDTH / 2 - 150, HEIGHT / 2 - 50);
            g.drawString("Your score: " + playerScore, WIDTH / 2 - 150, HEIGHT / 2);
            g.drawString("Press R to return to main menu", WIDTH / 2 - 300, HEIGHT / 2 + 50);
            g.drawRect(WIDTH / 2 - 320, HEIGHT / 2 - 100, 640, 200);
        }

        g.dispose();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver && e.getKeyCode() == KeyEvent.VK_R) {
            returnToMainMenu();
        } else if (paused && e.getKeyCode() == KeyEvent.VK_R) {
            returnToMainMenu(); // Quay lại menu chính khi đang tạm dừng
        } else {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_A -> move = -1;
                case KeyEvent.VK_D -> move = 1;
                case KeyEvent.VK_W -> moveY = -1; // Di chuyển lên
                case KeyEvent.VK_S -> moveY = 1; // Di chuyển xuống
                case KeyEvent.VK_SPACE -> {
                    if (!paused && shoot == 0) { // Chỉ bắn khi không đang pause
                        bullet.x = player.x;
                        bullet.y = player.y + 20;
                        shoot = 1;
                        shootSound = new MusicPlayer("/sound/shoot.wav");
                        shootSound.play();
                    }
                }
                case KeyEvent.VK_P -> {
                    paused = !paused; // Toggle paused state
                }
            }
        }
    }

    private void returnToMainMenu() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.remove(this); // Remove the game panel
        MainMenu mainMenu = new MainMenu(); // Create a new main menu
        topFrame.add(mainMenu); // Add it to the frame
        topFrame.revalidate();
        topFrame.repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_D)
            move = 0;
        if (e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_S)
            moveY = 0; // Dừng di chuyển lên/xuống
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}