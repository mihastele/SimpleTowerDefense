package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Created by Miha on 22.10.2017.
 */

public class BigEnemy extends Enemy {

    public BigEnemy(int size, int speed, int lifeBuff) {
        super(size, speed, lifeBuff);
        size = (int)(size*5.25);
        speed = (size * 3) / 4;
        bodyTexture = new Texture("enemy1.png");
        bodySprite = new Sprite(bodyTexture);
    }

}
