package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Created by Miha on 22.10.2017.
 */

public class BasicEnemy extends Enemy {

    public BasicEnemy(int size, int speed, int lifeBuff) {
        super(size, speed, lifeBuff);
        bodyTexture = new Texture("basicEnemy.png");
        bodySprite = new Sprite(bodyTexture);
    }

}
