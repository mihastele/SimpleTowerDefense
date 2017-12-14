package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Created by Miha on 22.10.2017.
 */

public class BlueEnemy extends Enemy {

    public BlueEnemy(int size, int speed, int lifeBuff) {
        super(size, speed, lifeBuff);
        speed = (speed *37)/20; //1.85 multiplier floored
        size = (size * 8)/10;
        bodyTexture = new Texture("enemy2.png");
        bodySprite = new Sprite(bodyTexture);
    }

}
