package com.mygdx.game;

import com.badlogic.gdx.Gdx;

import java.util.Random;

/**
 * Created by Miha on 21.10.2017.
 */

public class Enemy {
    int x, y;
    int size;
    int speed;
    int lives;

    public Enemy(int size, int speed, int lifeBuff) {
        this.size = size; //more bit deljivo z 2
        this.speed = speed;
        Random rnd = new Random();
        lives = size * lifeBuff / 10;

        int tempPosOffset = Gdx.graphics.getHeight() - 2 * 200 - size - 2 * 25;
        if(tempPosOffset<=0){
            tempPosOffset = 10;
        }
        this.x = 200 + 25 + size / 2 + rnd.nextInt(tempPosOffset);  //200 + 25 + 10 + size / 2 + rnd.nextInt(Gdx.graphics.getHeight() - size / 2 - 200 - 10);
        //this.y= Gdx.graphics.getWidth();
        this.y = 0;
    }

    /*public Enemy(int size, int speed, int position) {
        this(size, speed);
        this.x = position;
    }*/

    public void takeLife(int amount) {
        lives -= amount;
    }

    public void premikaj() {
        this.y += speed;
    }

    public int getLives() {
        return lives;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
