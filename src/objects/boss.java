package objects;

import java.awt.image.BufferedImage;

public class boss {
    private int hp;
    private int damage;
    private int speed;
    public int x;
    public int y;
    private int width;
    private int height;

    public boss(int hp, int damage, int speed, int x, int y, int width, int height){
        this.hp = hp;
        this.damage = damage;
        this.speed = speed;
        this.x = x;
        this.y=y;
        this.height=height;
        this.width = width;
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

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }
}

