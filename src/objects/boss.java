package objects;

import java.awt.image.BufferedImage;

public class boss {
    private BufferedImage image;
    private int hp;
    private int damage;
    private int speed;

    public boss(BufferedImage image, int hp, int damage, int speed){
        this.image = image;
        this.hp = hp;
        this.damage = damage;
        this.speed = speed;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getHp() {
        return hp;
    }

    public int getDamage() {
        return damage;
    }

    public int getSpeed() {
        return speed;
    }
}

