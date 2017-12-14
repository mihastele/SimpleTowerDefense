package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.ArrayDeque;

/**
 * Created by Miha on 21.10.2017.
 */

public class Tower {
    public Texture bodyOfTank, coreTank, coreShoot;
    public Sprite tankBody, tCore, tShoot;
    private int x, y, radij, which, shootDelay, timer, damage, tier = 0; //which pove ker nasprotnik je ciljan
    public int cost, price; //se ne spreminja in je za vse towerje enak
    public int[] upgradeCost;
    private double kot;
    public boolean desna = false;
    public boolean vDoesgu = false;
    public boolean readyToShoot = false;

    private int offsetCoreX = 0, offsetCoreY = 0;
    private int offsetBodyX = 0, offsetBodyY = 0;
    private int offsetShootX = 0, offsetShootY = 0;

    public Tower(int x, int y) {
        damage = 2;
        //cost = 200;
        desna = false;
        //price = cost; //pove kolk je vreden za prodajo
        upgradeCost = new int[]{50, 100, 190, 250, 380};
        shootDelay = 100;
        timer = shootDelay - 11; //za povecevanje, da vemo kdaj ustrelit, shootdelay nam pa da takt
        //coreTank = new Texture("pipe.png");
        //coreShoot = new Texture("pipe_attack.png");
        //bodyOfTank = new Texture("body_tank.png"); //nalaganje slik
        //tCore = new Sprite(coreTank);
        //tShoot = new Sprite(coreShoot);
        //tShoot.setOrigin(33 - 12.5f, 23.5f);
        //tankBody = new Sprite(bodyOfTank);
        this.x = x;
        this.y = y;
        radij = Gdx.graphics.getBackBufferHeight() / 2;
        kot = 90;

    }

    public Tower(int x, int y, boolean desnaPozicija) {
        this(x, y);
        desna = desnaPozicija;

    }

    public void aliJeVDosegu(ArrayDeque<Enemy> enemies) {
        which = 0;
        kot = 90;
        if (desna) {
            kot = -kot;
        }
        for (Enemy e : enemies) {
            //if(Math.abs(x-e.getX())<radij&&Math.abs(y-e.getY())<radij){ NOPE
            if ((Math.sqrt((x - e.getX()) * (x - e.getX()) + (y - e.getY()) * (y - e.getY()))) <= radij) {
                vDoesgu = true;
                timer = (timer + 1) % (shootDelay + 1); //ker modul deluje deljivo z nečem in ne bo dosegel shootdelaya če ne dam +1
                if (timer == shootDelay - 10) {
                    readyToShoot = true;
                }
                try {

                    this.kot = kot + Math.atan((this.y - e.getY() * 1.0) / (e.getX() - this.x)) * 180 / Math.PI;
                    //this.kot = 90 + (90 - Math.atan((e.getY() - this.y)/(e.getX() - this.x)) * 180 / Math.PI);
                    /*if(e.getY()<this.y) {
                        this.kot = 90 +(90 - Math.atan((e.getX() - this.x)/(e.getY() - this.y)) * 180 / Math.PI);
					}
					else
						this.kot = 90 -(90 + Math.atan((e.getX() - this.x)/(e.getY() - this.y)) * 180 / Math.PI);
						//this.kot = 90 - Math.atan((e.getY() - this.y) / (e.getX() - this.x)) * 180 / Math.PI;
						*/

                } catch (Exception exc) {
                    System.out.println("nope");
                }
                return;

            } else {
                which++;
                vDoesgu = false;
            }
        }
        if (!vDoesgu) {
            timer = shootDelay - 11; //90-1, da takoj sproz ko novga namer
        }

    }

    public int[] strelek(ArrayDeque<Enemy> enemies, ArrayDeque<Tower> towers) {
        int count = 0, score = 0;
        int reward = 0;
        for (Enemy s : enemies) {
            if (count == which) {
                s.takeLife(damage);
                if (s.getLives() < 1) {
                    score = s.size * s.speed;
                    reward = s.size;
                    enemies.remove(s);
                    for (Tower t : towers) {
                        while (t.getTimer() > t.shootDelay - 1) {
                            t.incrementTimer();
                            if (t.getTimer() >= t.getShootDelay()) {
                                t.setTimer(0);
                                break;//handling animation bugs
                            }
                        }
                        /*if(t.getTimer()>t.shootDelay-1){
							t.incrementTimer();
							if(t.getTimer()>=t.getShootDelay()){
								t.setTimer(0);                //handling animation bugs
							}
						}*/
                    }
                    return new int[]{score, reward};
                }
            } else
                count++;

        }
        return new int[]{0, 0};
    }

    public void onUpgrade() {
        price += upgradeCost[tier];
        tier++;
        damage = damage + (int) (tier * 0.5);
        if (tier < 3) {
            shootDelay -= 10;
        } else {
            shootDelay *= (3.0 / 5);
        }
    }

    public int sellCost() {
        return price / 2;
    }

    public void incrementTimer() {
        timer++;
    }

    public void setTimer(int n) {
        timer = n;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRadij() {
        return radij;
    }

    public void setRadij(int radij) {
        this.radij = radij;
    }

    public double getKot() {
        return kot;
    }

    public int getWhich() {
        return which;
    }

    public int getTimer() {
        return timer;
    }

    public int getShootDelay() {
        return shootDelay;
    }

    public void setShootDelay(int shootDelay) {
        this.shootDelay = shootDelay;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getPrice() {
        return price;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getTier() {
        return tier;
    }

    public int getOffsetBodyX() {
        return offsetBodyX;
    }

    public int getOffsetCoreX() {
        return offsetCoreX;
    }

    public int getOffsetBodyY() {
        return offsetBodyY;
    }

    public int getOffsetCoreY() {
        return offsetCoreY;
    }

    public int getOffsetShootX() {
        return offsetShootX;
    }

    public int getOffsetShootY() {
        return offsetShootY;
    }

    public void setOffsetBodyX(int offsetBodyX) {
        this.offsetBodyX = offsetBodyX;
    }

    public void setOffsetBodyY(int offsetBodyY) {
        this.offsetBodyY = offsetBodyY;
    }

    public void setOffsetCoreX(int offsetCoreX) {
        this.offsetCoreX = offsetCoreX;
    }

    public void setOffsetCoreY(int offsetCoreY) {
        this.offsetCoreY = offsetCoreY;
    }

    public void setOffsetShootX(int offsetShootX) {
        this.offsetShootX = offsetShootX;
    }

    public void setOffsetShootY(int offsetShootY) {
        this.offsetShootY = offsetShootY;
    }

}

