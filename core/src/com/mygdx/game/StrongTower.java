package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Created by Miha on 21.10.2017.
 */


public class StrongTower extends Tower {
    public StrongTower(int x, int y) {
        super(x, y);
        coreTank = new Texture("strong_pipe.png");
        coreShoot = new Texture("strong_pipe_attack.png");
        bodyOfTank = new Texture("strong_body.png"); //nalaganje slik
        setOffsetBodyX(-bodyOfTank.getWidth() / 2);
        setOffsetShootX(-coreShoot.getWidth() / 2);
        setOffsetCoreX(-coreTank.getWidth() / 2);
        setOffsetBodyY(-bodyOfTank.getHeight() / 2);
        setOffsetCoreY(-coreTank.getHeight() / 2);
        setOffsetShootY(-coreShoot.getHeight() / 2);
        tCore = new Sprite(coreTank);
        tShoot = new Sprite(coreShoot);
        setDamage(3 * getDamage());
        setShootDelay(2*getShootDelay());
        upgradeCost = new int[]{400, 800, 1200, 1800, 2400};
        cost=750;
        price=cost;
        setRadij(2*getRadij());



        tCore.setOrigin(tCore.getOriginX() + getOffsetCoreX(), tCore.getOriginY());
        tShoot.setOrigin(tShoot.getOriginX() + getOffsetShootX(), tShoot.getOriginY());



        //tShoot.setOrigin(33 - 12.5f, 23.5f);
        tankBody = new Sprite(bodyOfTank);

    }

    public StrongTower(int x, int y, boolean desno) {
        this(x, y);
        desna = desno;

    }
}