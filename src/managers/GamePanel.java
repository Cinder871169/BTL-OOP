package managers;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import main.MainMenu;
import objects.Item;
import objects.Spaceship;
import objects.boss;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    public static final int TILE = 64;
    public static Spaceship selectedSpaceship;

    private boolean running = true;
    private final int FPS = 60; // Target frame rate
    private final long OPTIMAL_TIME = 1000000000 / FPS;
    private long lastTime = System.nanoTime();

    private boolean gameOver = false,gameWin=false;
    private Random random = new Random();

    private BufferedImage backgroundImg;
    private BufferedImage playerImg, bulletImg, enemyImg, bossImg,bossBulletImg,bossBeamImg;

    private Item player, bullet;
    private int playerScore = 0, playerHealth, playerDamage;
    private ArrayList<Item> enemies = new ArrayList<>();
    private int enemyLength = 1;
    private long startTime;

    private int move = 0; // 0: đứng yên, 1: phải, -1: trái
    private int moveY = 0;
    private int shoot = 0; // 0: không bắn, 1: bắn

    private boolean paused = false;
    private Thread thread;

    private MusicPlayer musicPlayer, shootSound, hitSound;
    private String enemyFolder = "enemy1";

    //boss
    private boolean boss_active = false, boss_act = false;
    private int last_boss =  0;
    private boss boss;
    private long boss_spawntime = 0, boss_deathtime = 0, boss_action = 0, bossLastShoot=System.currentTimeMillis();
    private int bossHeath, bossSpeed,bossWidth,bossHeight, bossDamage, bossDirection=1;
    private ArrayList<Item> bossBullets = new ArrayList<>();
    private boolean bossCharge=false, bossBeam=false;
    private long bossChargeTime=0,bossBeamtime=0;
    private int target_x=0;
    private Item beam;
    private String bossFolder = "boss1";
    private String beamFolder = "laser1";
    private String waveFolder = "wave1";

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(150, 255, 245));
        setDoubleBuffered(true);
        setFocusable(true);
        addKeyListener(this);

        initializeObjects();

        musicPlayer = new MusicPlayer("/sound/theme.wav");
        musicPlayer.loop();

        // Lưu thời gian bắt đầu của trò chơi
        startTime = System.currentTimeMillis();

        thread = new Thread(this);
        thread.start();
    }

    private void loadBackgroundImages() {
        long etime = System.currentTimeMillis() - startTime;
        int frameIndex = (int) ((etime / 100) % 10);
        String backgroundImgPath = String.format("/images/background/background%d.png", frameIndex + 1);
        try {
            backgroundImg = loadImage(backgroundImgPath);
        } catch (IOException e) {
            System.err.println("Error loading background image: " + backgroundImgPath);
            e.printStackTrace();
        }
    }

    private void loadEnemyImages() {
        long etime = System.currentTimeMillis() - startTime;
        int frameIndex = (int) ((etime / 100) % 8);
        String enemyImgPath = String.format("/images/enemies1/%s/enemy%d.png", enemyFolder, frameIndex + 1);
        try {
            enemyImg = loadImage(enemyImgPath);
        } catch (IOException e) {
            System.err.println("Error loading enemy image: " + enemyImgPath);
            e.printStackTrace();
        }
    }

    private void loadPlayerBulletImages() {
        long etime = System.currentTimeMillis() - startTime;
        int frameIndex = (int) ((etime / 100) % 4);
        String pbulletImgPath = String.format("/images/PlayerBullet/bullet%d.png", frameIndex + 1);
        try {
            bulletImg = loadImage(pbulletImgPath);
        } catch (IOException e) {
            System.err.println("Error loading player bullet image: " + pbulletImgPath);
            e.printStackTrace();
        }
    }

    private void loadBossImages(){
        long etime = System.currentTimeMillis() - startTime;
        int frameIndex = (int) ((etime / 100) % 8);
        String bossImgPath = String.format("/images/bosses/%s/idle%d.png", bossFolder, frameIndex + 1);
        try {
            bossImg = loadImage(bossImgPath);
        } catch (IOException e) {
            System.err.println("Error loading boss image: " + bossImgPath);
            e.printStackTrace();
        }
    }

    private void loadBossBulletImages(){
        long etime = System.currentTimeMillis() - startTime;
        int frameIndex = (int) ((etime / 100) % 6);
        String bossBulletImgPath = String.format("/images/BossBullet/%s/wave%d.png", waveFolder,frameIndex + 1);
        try {
            bossBulletImg = loadImage(bossBulletImgPath);
        } catch (IOException e) {
            System.err.println("Error loading boss image: " + bossBulletImgPath);
            e.printStackTrace();
        }
    }

    private void loadBossBeamImages(){
        long etime = System.currentTimeMillis() - startTime;
        int frameIndex = (int) ((etime / 100) % 4);
        String bossBeamImgPath = String.format("/images/BossBullet/%s/laser%d.png", beamFolder ,frameIndex + 1);
        try {
            bossBeamImg = loadImage(bossBeamImgPath);
        } catch (IOException e) {
            System.err.println("Error loading boss image: " + bossBeamImgPath);
            e.printStackTrace();
        }
    }

    // Add
    private BufferedImage loadImage(String imagePath) throws IOException {
        BufferedImage img = ImageIO.read(getClass().getResource(imagePath));
        if (img == null) {
            throw new IOException("Image not found: " + imagePath);
        }
        return img;
    }

    private void initializeObjects() {
        player = new Item();
        player.x = ConfigLoader.getInt("player.initialX");
        player.y = ConfigLoader.getInt("player.initialY");

        if (selectedSpaceship == null) {
            // Nếu người chơi chưa chọn tàu, sử dụng tàu mặc định
            try {
                selectedSpaceship = new Spaceship(
                        ImageIO.read(getClass().getResource(ConfigLoader.getString("spaceship1.image"))),
                        ConfigLoader.getInt("spaceship1.hp"),
                        ConfigLoader.getInt("spaceship1.damage"),
                        ConfigLoader.getInt("spaceship1.speed"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        playerImg = selectedSpaceship.getImage();
        playerHealth = selectedSpaceship.getHp();
        player.vx = selectedSpaceship.getSpeed();
        playerDamage = selectedSpaceship.getDamage();

        bullet = new Item();
        bullet.vy = ConfigLoader.getInt("bullet.velocityY");

        for (int i = 0; i < enemyLength; i++) {
            Item enemy = new Item();
            enemy.x = random.nextInt(WIDTH - TILE);
            enemy.y = -random.nextInt(TILE);
            enemy.vy = ConfigLoader.getInt("enemy.initialVelocityY");
            enemies.add(enemy);
        }
    }

    private long lastEnemySpawnTime = System.currentTimeMillis(); // Track the last spawn time
    private int enemySpawnInterval = ConfigLoader.getInt("enemy.spawnInterval") * 1000; // Interval in milliseconds

    private void update() {
        if (paused || gameOver == true || gameWin==true) {
            return; // Skip all updates when the game is paused or gameOver or Win
        }
        loadBackgroundImages();
        loadBossImages();
        if (playerScore %10 == 0 && playerScore >0 && playerScore != last_boss && !boss_active){
            spawnBoss();
        }

        //delay for boss spawn and deadth
        if (boss_active){
            if (System.currentTimeMillis()-boss_spawntime >= 4000 && boss_spawntime!=0){
                int target_y = 0;
                if (boss.y <target_y){
                    boss.y+= bossSpeed;
                }
            }
            if(System.currentTimeMillis()-boss_deathtime>=4000 && boss_deathtime!=0){
                boss_deathtime=0;
                boss_active = false;
            }
            if(System.currentTimeMillis()-boss_deathtime>=3000 && boss_deathtime!=0 && bossFolder=="boss3"){
                gameWin = true;
            }
        }

        //boss action
        if(boss!=null){
            //get random action
            if(!boss_act && System.currentTimeMillis()-boss_action>=10000){
                int[] actions = {10000,27000};
                int randomAction = random.nextInt(actions.length);
                boss_action = System.currentTimeMillis() - actions[randomAction];
                boss_act = true;
            }
            //action
            bossAttack1();
            bossAttack2();
        }

        if (playerScore >= 11 && playerScore < 20 && !boss_active) {
            enemyFolder = "enemy2";
        } else if (playerScore >= 21 && !boss_active) {
            enemyFolder = "enemy3";
        }
        if (playerScore ==20){
            bossFolder="boss2";
            beamFolder="laser2";
        }
        else if (playerScore ==30){
            bossFolder="boss3";
            beamFolder="laser3";
            waveFolder="wave3";
        }

        // Di chuyển ngang
        if (move == 1 && player.x + TILE < WIDTH)
            player.x += player.vx;
        else if (move == -1 && player.x > 0)
            player.x -= player.vx;

        // Di chuyển dọc (lên hoặc xuống)
        if (moveY == 1 && player.y + TILE < HEIGHT)
            player.y += player.vx;
        else if (moveY == -1 && player.y > 0)
            player.y -= player.vx;

        loadPlayerBulletImages();
        if (shoot == 1) {
            bullet.y -= bullet.vy;
            if (bullet.y < -TILE)
                shoot = 0;
        }

        // Xuất hiện địch theo thời gian
        if(!boss_active){
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastEnemySpawnTime >= enemySpawnInterval) {
                spawnEnemy();
                lastEnemySpawnTime = currentTime;
            }
        }

        // Update enemies
        Iterator<Item> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Item enemy = iterator.next();
            loadEnemyImages();
            enemy.y += enemy.vy;

            // Check collision with bullet
            if (checkCollision(bullet, enemy)) {
                resetEnemy(enemy);
                shoot = 0;
                playerScore += ConfigLoader.getInt("enemy.scoreValue");
                hitSound = new MusicPlayer("/sound/hit.wav");
                hitSound.play();
                iterator.remove(); // Remove the enemy
            }

            // Check collision with player
            if (checkCollision(player, enemy)) {
                int impact = random.nextInt(ConfigLoader.getInt("enemy.healthImpactMax") -
                        ConfigLoader.getInt("enemy.healthImpactMin")) +
                        ConfigLoader.getInt("enemy.healthImpactMin");
                hitSound = new MusicPlayer("/sound/hit.wav");
                hitSound.play();
                playerHealth -= impact;
                resetEnemy(enemy);
                iterator.remove(); // Remove the enemy
            }

            if(enemy.y > HEIGHT){
                iterator.remove();
            }
        }

        // Update boss bullet
        Iterator<Item> bossIterator = bossBullets.iterator();
        while (bossIterator.hasNext()) {
                Item bossBullet = bossIterator.next();
                loadBossBulletImages();
                bossBullet.y += bullet.vy/5;
                if(checkBossBulletCollision(player, bossBullet)){
                    playerHealth -= bossDamage*40;
                    bossIterator.remove();
                }
                if(bossBullet.y > HEIGHT){
                    bossIterator.remove();
                }
        }
        //Update boss beam
        beamUpdate();
        
        //check boss collision
        if(boss!=null){
            if(beam!=null){
                if(checkBossBeamCollision(player, beam)){
                    playerHealth -= bossDamage*2;
                }
            }
            if(checkBossCollision(bullet, boss) && shoot==1){
                shoot=0;
                bossHeath -= playerDamage;
            }
            if(checkBossCollision(player, boss)){
                playerHealth -= bossDamage*2;
            }
            if(bossHeath<=0){
                playerScore += ConfigLoader.getInt("enemy.scoreValue");
                boss = null;
                boss_spawntime =0;
                boss_deathtime = System.currentTimeMillis();
            }
        }

        if (playerHealth <= 0) {
            playerHealth = 0;
            gameOver = true;
            musicPlayer.stop();
        }
    }

    private void spawnBoss(){
        try {
            boss = new boss(
            ConfigLoader.getInt("boss.hp"),
            ConfigLoader.getInt("boss.damage"),
            ConfigLoader.getInt("boss.speed"),
            ConfigLoader.getInt("boss.initialX"),
            ConfigLoader.getInt("boss.initialY"),
            ConfigLoader.getInt("boss.width"),
            ConfigLoader.getInt("boss.height"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        boss_active = true;
        last_boss = playerScore;
        boss_spawntime = System.currentTimeMillis();
        boss_action = System.currentTimeMillis();

        if(boss!=null){
            bossHeath = boss.getHp();
            bossSpeed = boss.getSpeed();
            bossWidth = boss.getWidth();
            bossHeight = boss.getHeight();
            bossDamage = boss.getDamage();
        }
    }

    private void bossAttack1(){
        if(System.currentTimeMillis() - boss_action >=10000 && System.currentTimeMillis() - boss_action <=26000){
            if(boss.x<0){
                bossDirection = 1;
            }
            else if(boss.x >= WIDTH - bossWidth){
                bossDirection = -1;
            }
            boss.x += bossSpeed*bossDirection;

            if(System.currentTimeMillis()-bossLastShoot > 2500){
                Item bossBullet = new Item();
                bossBullet.x = boss.x+(bossWidth/2-85);
                bossBullet.y = boss.y + bossHeight-100;
                bossBullets.add(bossBullet);
                bossLastShoot=System.currentTimeMillis();
            }
        }
        
        //delay after action
        if(System.currentTimeMillis() - boss_action >26000 && System.currentTimeMillis() - boss_action <=26500){
            boss_act = false;
            boss_action = System.currentTimeMillis() - 7000;
        }
    }

    private void bossAttack2(){
        if(System.currentTimeMillis() - boss_action >=27000 && System.currentTimeMillis() - boss_action <=37000){
            if(!bossCharge && !bossBeam && System.currentTimeMillis()-bossBeamtime>=3000){
                target_x = player.x-bossWidth/2+32;
                bossChargeTime=System.currentTimeMillis();
                bossCharge=true;
            }
            if(bossCharge){
                if(boss.x>target_x+5){
                    boss.x -= bossSpeed*8;
                }
                if(boss.x < target_x-5){
                    boss.x+= bossSpeed*8;
                }
                if (System.currentTimeMillis() - bossChargeTime >= 2000){
                    bossBeam=true;
                    bossBeamtime=System.currentTimeMillis();
                    bossCharge=false;
                }
            }
            if(bossBeam){
                loadBossBeamImages();
                beam = new Item();
                beam.x = boss.x+(bossWidth/2-80);
                beam.y = boss.y + bossHeight-20;
            }
        }
        
        //delay after action
        if(System.currentTimeMillis() - boss_action >37000 && System.currentTimeMillis() - boss_action <37500){
            boss_act = false;
            boss_action = System.currentTimeMillis() - 8000;
        }
    }

    private void beamUpdate(){
        if(System.currentTimeMillis()-bossBeamtime>=2000 || boss==null){
            bossBeam=false;
            beam=null;
        }
    }

    private void spawnEnemy() {
        enemyLength++;
        Item newEnemy = new Item();
        newEnemy.x = random.nextInt(WIDTH - TILE);
        newEnemy.y = -random.nextInt(TILE);
        newEnemy.vy = ConfigLoader.getInt("enemy.initialVelocityY");
        enemies.add(newEnemy);
    }

    private boolean checkCollision(Item a, Item b) {
        Rectangle aBounds = new Rectangle(a.x, a.y, TILE, TILE);
        Rectangle bBounds = new Rectangle(b.x, b.y, TILE, TILE);
        return aBounds.intersects(bBounds);
    }

    private boolean checkBossCollision(Item a, boss b) {
        Rectangle aBounds = new Rectangle(a.x, a.y, TILE, TILE);
        Rectangle bossBounds = new Rectangle(b.x, b.y, bossWidth , bossHeight);
        return aBounds.intersects(bossBounds);
    }

    private boolean checkBossBulletCollision(Item a, Item b) {
        Rectangle aBounds = new Rectangle(a.x, a.y, TILE, TILE);
        Rectangle bossBounds = new Rectangle(b.x, b.y, 170 , 90);
        return aBounds.intersects(bossBounds);
    }

    private boolean checkBossBeamCollision(Item a, Item b) {
        Rectangle aBounds = new Rectangle(a.x, a.y, TILE, TILE);
        Rectangle bossBounds = new Rectangle(b.x, b.y, 160 , 380);
        return aBounds.intersects(bossBounds);
    }

    private void resetEnemy(Item enemy) {
        do {
            enemy.x = random.nextInt(WIDTH - TILE);
            enemy.y = -random.nextInt(TILE);
        } while (Math.abs(enemy.x - player.x) < TILE && Math.abs(enemy.y - player.y) < TILE);
    }

    @Override
    public void run() {
        while (running) {
            long now = System.nanoTime();
            long elapsedTime = now - lastTime;
            lastTime = now;
            update();
            repaint();
            try {
                long sleepTime = (lastTime - System.nanoTime() + OPTIMAL_TIME) / 1000000;
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImg, 0, 0, null);

        if (shoot == 1) {
            g.drawImage(bulletImg, bullet.x + 19, bullet.y, 25, 60, null);
        }
        if (!gameOver && !gameWin)
            g.drawImage(playerImg, player.x, player.y, TILE, TILE, null);

        ArrayList<Item> bossBulletsCopy = new ArrayList<>(bossBullets);
        for(Item bossBullet : bossBulletsCopy){
            g.drawImage(bossBulletImg, bossBullet.x , bossBullet.y , 200, 100, null );
        }
        ArrayList<Item> enemiesCopy = new ArrayList<>(enemies);
        for (Item enemy : enemiesCopy) {
            g.drawImage(enemyImg, enemy.x, enemy.y, TILE, TILE, null);
        }
        if(boss!=null && System.currentTimeMillis()-boss_spawntime >= 4000 && boss_spawntime!=0){
            g.drawImage(bossImg, boss.x, boss.y, bossWidth, bossHeight, null);
        }
        if(bossBeam && boss!=null){
            g.drawImage(bossBeamImg, beam.x, beam.y, 160,380,null);
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 15));
        g.drawString("SCORE  " + playerScore, 5, 15);
        // Thanh máu
        int healthBarX = 5, healthBarY = 40, healthBarWidth = 150, healthBarHeight = 15;
        int healthFillWidth = (int) ((playerHealth / 100.0) * healthBarWidth);

        // Hiện thanh máu
        g.setColor(Color.RED);
        g.fillRect(healthBarX + 1, healthBarY + 1, healthFillWidth, healthBarHeight - 1);

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

        if (gameWin) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("VICTORY", WIDTH / 2 - 100, HEIGHT / 2 - 50);
            g.drawString("Your score: " + playerScore, WIDTH / 2 - 150, HEIGHT / 2);
            g.drawString("Press R to return to main menu", WIDTH / 2 - 300, HEIGHT / 2 + 50);
            g.drawRect(WIDTH / 2 - 320, HEIGHT / 2 - 100, 640, 200);
        }

        g.dispose();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if ((gameOver || gameWin) && e.getKeyCode() == KeyEvent.VK_R) {
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
                    if (!paused && shoot == 0) { // Không bắn khi pause
                        bullet.x = player.x;
                        bullet.y = player.y + 20;
                        shoot = 1;
                        shootSound = new MusicPlayer("/sound/shoot.wav");
                        shootSound.play();
                    }
                }
                case KeyEvent.VK_P -> {
                    paused = !paused;
                }
            }
        }
    }

    private void returnToMainMenu() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.remove(this); // Loại bỏ GamePanel
        MainMenu mainMenu = new MainMenu(); // Tạo menu mới
        topFrame.add(mainMenu);
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
