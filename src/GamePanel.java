import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Random;

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
    private int enemySpawnFrequency;

    private int move = 0; // 0: no movement, 1: right, -1: left
    private int shoot = 0; // 0: no shooting, 1: shoot

    private Thread thread;

    private MusicPlayer musicPlayer;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(150, 255, 245));
        setFocusable(true);
        addKeyListener(this);

        loadImages();
        initializeObjects();

        musicPlayer = new MusicPlayer("/sound/theme.wav");
        musicPlayer.loop();

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

        if (move == 1 && player.x + TILE < WIDTH)
            player.x += player.vx;
        else if (move == -1 && player.x > 0)
            player.x -= player.vx;

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
            }

            if (checkCollision(player, enemy)) {
                int impact = random.nextInt(ConfigLoader.getInt("enemy.healthImpactMax") -
                        ConfigLoader.getInt("enemy.healthImpactMin")) +
                        ConfigLoader.getInt("enemy.healthImpactMin");
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
                if (!gameOver)
                    update();
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
        g.drawString("HEALTH " + playerHealth, 5, 35);
        g.drawString("TIME   " + enemyCount, 5, 55);

        if (gameOver) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("GAME OVER", WIDTH / 2 - 150, HEIGHT / 2 - 50);
            g.drawString("Press SPACE to return to main menu", WIDTH / 2 - 300, HEIGHT / 2 + 50);
        }

        g.dispose();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver && e.getKeyCode() == KeyEvent.VK_SPACE) {
            returnToMainMenu();
        } else {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_A -> move = -1;
                case KeyEvent.VK_D -> move = 1;
                case KeyEvent.VK_SPACE -> {
                    if (shoot == 0) {
                        bullet.x = player.x;
                        bullet.y = player.y + 20;
                        shoot = 1;
                    }
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
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
