package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Created by Miha on 21.10.2017.
 */

public class BasicTower extends Tower {

    public BasicTower(int x, int y) {
        super(x, y);
        coreTank = new Texture("pipe.png");
        coreShoot = new Texture("pipe_attack.png");
        bodyOfTank = new Texture("body_tank.png"); //nalaganje slik
        setOffsetBodyX(-bodyOfTank.getWidth() / 2);
        setOffsetShootX(-coreShoot.getWidth() / 2);
        setOffsetCoreX(-coreTank.getWidth() / 2);
        setOffsetBodyY(-bodyOfTank.getHeight() / 2);
        setOffsetCoreY(-coreTank.getHeight() / 2);
        setOffsetShootY(-coreShoot.getHeight() / 2);
        tCore = new Sprite(coreTank);
        tShoot = new Sprite(coreShoot);
        cost=200;
        price=cost;


        tCore.setOrigin(tCore.getOriginX() + getOffsetCoreX(), tCore.getOriginY()); //TODO FIX DIS
        tShoot.setOrigin(tShoot.getOriginX() + getOffsetShootX(), tShoot.getOriginY());
        //tShoot.setOrigin(33 - 12.5f, 23.5f); ta je dobr za ta tower


        tankBody = new Sprite(bodyOfTank);
    }

    public BasicTower(int x, int y, boolean desnaPozicija) {
        this(x, y);
        desna = desnaPozicija;
    }


}
