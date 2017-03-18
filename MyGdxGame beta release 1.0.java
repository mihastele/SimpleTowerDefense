package com.mygdx.game;


import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Console;
import java.util.ArrayDeque;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


import sun.rmi.runtime.Log;

import static com.badlogic.gdx.Gdx.graphics;
import static com.badlogic.gdx.utils.JsonValue.ValueType.array;


class Enemy {
    int x, y;
    int size;
    int speed;
    int lives;

    public Enemy(int size, int speed,int lifeBuff) {
        this.size = size; //more bit deljivo z 2
        this.speed = speed;
        Random rnd = new Random();
        lives = size*lifeBuff / 10;

        this.x = 200+25+size/2+rnd.nextInt(Gdx.graphics.getHeight()-2*200-size-2*25);  //200 + 25 + 10 + size / 2 + rnd.nextInt(Gdx.graphics.getHeight() - size / 2 - 200 - 10);
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

class Tower {
    public Texture bodyOfTank, coreTank, coreShoot;
    public Sprite tankBody, tCore, tShoot;
    private int x, y, radij, which, shootDelay, timer, damage, price, tier = 0; //which pove ker nasprotnik je ciljan
    private static int cost; //se ne spreminja in je za vse towerje enak
    public static int[] upgradeCost;
    private double kot;
    public boolean desna = false;
    public boolean vDoesgu = false;
    public boolean readyToShoot = false;

    public Tower(int x, int y) {
        damage = 2;
        cost = 200;
        desna = false;
        price = cost; //pove kolk je vreden za prodajo
        upgradeCost = new int[]{50, 100, 190, 250, 380};
        shootDelay = 100;
        timer = shootDelay - 11; //za povecevanje, da vemo kdaj ustrelit, shootdelay nam pa da takt
        coreTank = new Texture("pipe.png");
        coreShoot = new Texture("pipe_attack.png");
        bodyOfTank = new Texture("body_tank.png"); //nalaganje slik
        tCore = new Sprite(coreTank);
        tShoot = new Sprite(coreShoot);
        tShoot.setOrigin(33 - 12.5f, 23.5f);
        tankBody = new Sprite(bodyOfTank);
        this.x = x;
        this.y = y;
        radij = Gdx.graphics.getBackBufferHeight() / 2 - 50;
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

    public int getDamage() {
        return damage;
    }

    public int getPrice() {
        return price;
    }

    public static int getCost() {
        return cost;
    }

    public int getTier() {
        return tier;
    }
}

public class MyGdxGame extends ApplicationAdapter implements InputProcessor {

    ArrayDeque<Enemy> enemies = new ArrayDeque<Enemy>();
    ArrayDeque<Tower> towers = new ArrayDeque<Tower>();
    Array<Integer> prostaPolja = new Array<Integer>(); //TODO s for zanko pr create ustvar seznam vseh prostih mest, nato pa jih spotoma ko dodajaš towerje odstranjuješ in po odstranitvi towerja
    //TODO nazaj notr vržeš , polja so Y vrednosti, za ali je levo ali desno pa dodaj logiko, svetujem da pregleduješ ko dodajaš tower
    int prostaPoljaMeja = 0; //da veš kje se začnejo desni
    private SpriteBatch batch, batchrotated;
    Random rnd = new Random();
    private Texture coin, heart;
    private Sprite coinS, heartS;
    private int SIZE_BOTTOM;

    //Screen dimensions will be used to center text
    private BitmapFont font, zivljenja, denarBitmapFont, scoreBitmapFont, tankPrice, upgradePrice, removeReturn;
    private int screenWidth, screenHeight;
    private ShapeRenderer sr;
    private Texture addIcon, upgradeIcon, refundIcon;
    //private Sprite tankBody,tCore;
    //private String message = "Touch me";

    private boolean selected = false, selectedMenu = false, platno = true; // selected-pove ali je kater ot towerjev selektiran, selectedMenu pove ali je kater na meniju selektiran(dela samo za tower meni),
    //platno pove ali je na dodajanju towerjev(true) ali pa na upgrejdanju teh(false)
    private int indexOfTheSelected = 0, countForIndex = 0, selX = 0, selY = 0;
    private boolean selRight = false;

    ArrayDeque<Tower> samples = new ArrayDeque<Tower>();

    private int Xs = 0, Ys = 0, Hs = 0, Ws = 0; //draw box to select, TODO poprav to da bo delal na pravo mesto da se za towerjam narise

    Timer timer;

    public static int time=0;
    public static boolean readytoInititate=false;
    private int timeDelay=3;
    private int speedDeviation=0;
    private int sizeDeviation=0;
    private int enemyType = 0;
    private int life = 6;
    private long score = 0l;
    private int denar = 400;
    private boolean right = false; //pove če si kliknu na desn rob da vemo kam tower postavt;


    public void handleCoreRotation() {


        for (Tower t : towers) {
            t.aliJeVDosegu(enemies);
            if (!(t.getTimer() + 10 >= t.getShootDelay())) {
                t.tCore.setRotation((float) t.getKot());
                t.tCore.setPosition(t.getY() + 5, t.getX() + 5);
                t.tCore.draw(batch);

                if (t.readyToShoot) {
                    int[] temp = t.strelek(enemies, towers);
                    score += temp[0];
                    denar += temp[1];
                    t.readyToShoot = false;


                }
            } else {

                //System.out.println(t.tShoot.getOriginX() + " " + t.tShoot.getOriginY());
                t.tShoot.setRotation((float) t.getKot());
                t.tShoot.setPosition(t.getY() + 5, t.getX() + 5);
                t.tShoot.draw(batch);
            }

        }

    }

    public void handleTowers() {
        for (Tower s : towers) {
            batch.draw(s.tankBody, s.getY(), s.getX());
            //s.aliJeVDosegu(enemies);

        }
    }

    public void handleSamples() {
        for (Tower s : samples) {
            this.batch.begin();
            batch.draw(s.tankBody, s.getY()-25, s.getX()-25);
            s.tCore.setRotation(90);
            s.tCore.setPosition(s.getY() + 5-25, s.getX() + 5-25);
            s.tCore.draw(batch);
            this.batch.end();
        }
    }

    public boolean inTargetPositionY(int y, int zgMeja) {
        if (zgMeja - screenWidth / 5 < y && zgMeja > y) {
            return true;
        }
        return false;
    }

    public int setTargetPositionY(int y) {
        for (int i = 0; i < 5; i++) {
            if (i * screenWidth / 5 < y && (i + 1) * screenWidth / 5 > y) {
                return i * screenWidth / 5;
            }
        }
        return screenWidth;
    }

    public void handleAddTower(int x, int y, boolean desno) { // TODO logika po dodajanu towerja
        if (denar >= Tower.getCost() && !checkIfTowerThere(y, desno)) {
            denar -= Tower.getCost(); //TODO IMPORTANT: KO BOS DODAJU VEC TOWERJEV BOS TUKI SU CEZ SWITCH INDEKSOFSELECTED DA BOS VEDU KER TOWER MORS POSTAVT NAPREJ!!!!!
            if (desno) {
                for (int i = prostaPoljaMeja; i < prostaPolja.size; i++) {
                    if (y == prostaPolja.get(i)) {
                        prostaPolja.removeIndex(i);
                        break;
                    }
                }
                //towers.add(new Tower(x,y,desno));
            } else {
                for (int i = 0; i < prostaPoljaMeja; i++) {
                    if (y == prostaPolja.get(i)) {
                        prostaPolja.removeIndex(i);
                        prostaPoljaMeja--;
                        break;
                    }
                }
                //towers.add(new Tower(x,y,desno));
            }
            towers.add(new Tower(x, y, desno));
        }
    }

    public void handleRemoveTower(int x, int y) { //TODO logika odstranjevanja towerja
        if (right) {
            prostaPolja.insert(prostaPoljaMeja, y);
        } else {
            prostaPolja.insert(0, y);
            prostaPoljaMeja++;
        }
        denar+=selectedTower(x, y, selRight).getPrice()/2;
        towers.remove(selectedTower(x, y, selRight));
    }

    public void selektirajTowerLogika(int x, int y, boolean desna) {
        selected = true;
        Xs = x - 40;
        Ys = y - 40;
        Hs = 130;//Ws=130;

    }

    public Tower selectedTower(int x, int y, boolean desni) {
        for (Tower s : towers) {
            if (s.getX() == x && s.getY() == y && s.desna == desni) {
                return s;
            }
        }
        return null;
    }

    public Tower selectedTower(int y, boolean desni) {
        for (Tower s : towers) {
            if (s.getY() == y && s.desna == desni) {
                return s;
            }
        }
        return null;
    }

    public void drawSelectionBox(int y, int x, int visina, int sirina) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0 / 255f, (float) 221.0 / 255, (float) 255.0 / 255, 1);
        sr.rect(y, x, visina, sirina);
        sr.end();
    }


    public void handleSpodnjoplatno() {
        if (platno) {
            if (selectedMenu) { //selektiranega pobarva
                drawPluses();
                drawSelectionBox(screenWidth - SIZE_BOTTOM, indexOfTheSelected * screenHeight / 5 - screenHeight / 10, SIZE_BOTTOM, screenHeight / 5);
				/*sr.begin(ShapeRenderer.ShapeType.Filled);
				sr.setColor(0/255f, (float)221.0/255, (float)255.0/255,1);
				sr.rect(screenWidth-SIZE_BOTTOM+50,indexOfTheSelected*screenHeight/5-screenHeight/10,200,screenHeight/5);
				sr.end();*/
            }
            handleSamples();

        } else {
            //TODO V PRIHODNJOSTI DEJ SEM SVOJE FUNKCIONALNOSTI SEM KER JE TKO LEPSE IN BOLJ UCINKOVITO
        }


    }

    public void handleSpodnjoplatnoTeksti() {
        if (platno) {
            int i = 1;
            for (Tower s : samples) {
                tankPrice.draw(batchrotated, "-" + s.getCost(), screenHeight / 5 * i - (("cost:" + s.getCost()).length() / 2) * 4 *3 , -(screenWidth - SIZE_BOTTOM/2 + 25 ));//(("cost:"+s.getCost()).length()/2)*4*5 odmik teksta polvic krat konstanta vleikosti
                i++;
            }
        } else {
            //upgrade text logic here TODO
            if(selected) {
                if (selectedTower(selX, selY, selRight).getTier() < 5) {
                    upgradePrice.draw(batchrotated, "-" + Tower.upgradeCost[selectedTower(selX, selY, selRight).getTier()], screenHeight / 3 - 10, -(screenWidth - SIZE_BOTTOM / 2));
                } else {
                    upgradePrice.draw(batchrotated, "Max", screenHeight / 3 - 10, -(screenWidth - SIZE_BOTTOM / 2));
                }
                removeReturn.draw(batchrotated, "+" + selectedTower(selX, selY, selRight).getPrice() / 2, 2 * screenHeight / 3 - 10, -(screenWidth - SIZE_BOTTOM / 2));
            }

        }


    }

    public void drawPluses() {
        batch.begin();
        for (int i = 0; i < prostaPolja.size; i++) {
            if (i < prostaPoljaMeja) {
                batch.draw(addIcon, prostaPolja.get(i) - 29, 100 - 29); //odmik je ravno četrtina velikosti slike
            } else
                batch.draw(addIcon, prostaPolja.get(i) - 29, screenHeight - 100 - 29);
        }
        batch.end();
    }

    public void handleEnemies(){
//        private int time=0,timeSeconds = 0; TODO zanimajo nas te deli kode
//        private int timeDelay=3;
//        private int speedDeviation=0;
//        private int sizeDeviation=0;
//        private int enemyType = 0;

        int deltaLifeBuffMultiplier=1;
       // int i= 500000000;
        //boolean milisekunde=false;
        /*long startTime= System.nanoTime();

        time+=System.nanoTime()-startTime;
        if(time>=1000000000){ //sestevanje sekund
            time=time%1000000000;
            timeSeconds++;
        }*/



        switch (enemyType){
            case 0:
                sr.setColor(Color.GREEN);
                deltaLifeBuffMultiplier=1;
                break;
            case 1:
                sr.setColor(Color.BLUE);
                deltaLifeBuffMultiplier=2;
                break;
            case 2:
                sr.setColor(Color.RED);
                deltaLifeBuffMultiplier=5;
                break;
            case 3:
                sr.setColor(Color.BLACK);
                deltaLifeBuffMultiplier=10;
                break;
        }

        if(time==119&&enemyType<4&&readytoInititate){
            readytoInititate=false; //enkrat izved
            enemyType++;
            sizeDeviation+=20;
            speedDeviation+=1;
        }else if(time==119&&readytoInititate){
            readytoInititate=false;
            sizeDeviation+=10;
            speedDeviation+=1;
        }

        Random rnd = new Random();

        if (time < 20&&readytoInititate) {
            if(time%10==0){
                int speed=rnd.nextInt(4)+1+speedDeviation;
                int size=rnd.nextInt(20)+25+sizeDeviation;
                enemies.add(new Enemy(size,speed,deltaLifeBuffMultiplier));
            }

        } else if (time < 40&&readytoInititate) {
            if(time%6==0){
                int speed=rnd.nextInt(4)+1+speedDeviation;
                int size=rnd.nextInt(30)+25+sizeDeviation;
                enemies.add(new Enemy(size,speed,deltaLifeBuffMultiplier));
            }

        } else if (time < 80&&readytoInititate) {
            if(time%3==0){
                int speed=rnd.nextInt(4)+1+speedDeviation;
                int size=rnd.nextInt(100)+25+sizeDeviation;
                enemies.add(new Enemy(size,speed,deltaLifeBuffMultiplier));
            }

        } else {
            if (time%2==0&&readytoInititate){
                int speed=rnd.nextInt(4)+1+speedDeviation;
                int size=rnd.nextInt(100)+25+sizeDeviation;
                enemies.add(new Enemy(size,speed,deltaLifeBuffMultiplier));
            }
        }
        readytoInititate=false;

      /*  if(time%5>3&&readytoInititate){
            Random rnd = new Random();
            readytoInititate=false;
            int speed=rnd.nextInt(4)+1+speedDeviation;
            int size=rnd.nextInt(100)+25+sizeDeviation;
            enemies.add(new Enemy(size,speed,deltaLifeBuffMultiplier));
        }*/



    }



    //Set screen dimensions, font, and use this class for input processing
    @Override
    public void create() {
        //denar = 400000;

        batch = new SpriteBatch();
        batchrotated = new SpriteBatch();


        heart = new Texture("life.png");
        coin = new Texture("coin.png");
        coinS = new Sprite(coin);
        heartS = new Sprite(heart);

        tankPrice = new BitmapFont();
        tankPrice.getData().setScale(3);

        addIcon = new Texture("add.png");
        upgradeIcon = new Texture("upgrade.png");
        refundIcon = new Texture("refund.png");
        upgradePrice = new BitmapFont();
        upgradePrice.getData().setScale(3);
        removeReturn = new BitmapFont();
        removeReturn.getData().setScale(3);

        try {
            timer = new Timer();
            timer.schedule(new TajmerHendl(),0, 500);

        }catch (Exception e){

        }







                screenWidth = graphics.getWidth();
        screenHeight = graphics.getHeight();
        int temp = 0; //da gre do polovice in potem spet znova začne
		/*for (int i=screenWidth/5;i<screenWidth*2;i=i+screenWidth/5){ //gre od prvega do zadnjega na levi ki je eden manjši od indeksa, ter naprej na desne isto
			if(i<screenWidth) {
				prostaPolja.add( i % screenWidth);
				temp=prostaPolja.size;
			}
			else
				//prostaPolja.add( (i+screenWidth/5) % screenWidth);
				prostaPolja.add(prostaPolja.get((i/(screenWidth/5)-1)%temp));
		}*/
        for (int i = screenWidth / 5; i < screenWidth * 2 - screenWidth / 5; i = i + screenWidth / 5) { //gre od prvega do zadnjega na levi ki je eden manjši od indeksa, ter naprej na desne isto
            if (i < screenWidth - 10) { //10 je odmik za int
                prostaPolja.add(i % screenWidth);
                temp = prostaPolja.size;
            } else if (i > screenWidth)
                //prostaPolja.add( (i+screenWidth/5) % screenWidth);
                prostaPolja.add(prostaPolja.get((i / (screenWidth / 5) - 2) % temp)); //-2 ker je zamik za eno in ker enga preskočmo je zamik 2 al neki tazga nvm, java logic...
        }

        prostaPoljaMeja = prostaPolja.size / 2;

        samples.push(new Tower((screenHeight / 5), screenWidth * 9 / 10 + screenWidth / 20 - 25));

        //towers.add(new Tower(100,screenWidth/5));
        //towers.add(new Tower(screenHeight-100,screenWidth/5,true));
        //towers.getLast().onUpgrade();
        //towers.getLast().onUpgrade();
        //towers.getLast().onUpgrade();
        //towers.getLast().onUpgrade();

        //enemies.add(new Enemy(10, 2, screenHeight / 2)); //old, new buff would make this one unstoppable

        if ((screenWidth / 9) <= 150) {
            SIZE_BOTTOM = 150;
        } else
            SIZE_BOTTOM = screenWidth / 9;
        sr = new ShapeRenderer();

        font = new BitmapFont();
        font.setColor(Color.BLUE);
        font.getData().setScale(5, 5);

        denarBitmapFont = new BitmapFont();
        scoreBitmapFont = new BitmapFont();
        zivljenja = new BitmapFont(); //flipped
        zivljenja.setColor(Color.BLUE);
        zivljenja.getData().setScale(5, 5);
        //coreTank= new Texture("pipe.png");
        //bodyOfTank = new Texture("body_tank.png");
        //tCore=new Sprite(coreTank);
        //tankBody= new Sprite(bodyOfTank);

        Gdx.input.setInputProcessor(this);
    }

    //Don't forget to free font
    @Override
    public void dispose() {
        batch.dispose();
        batchrotated.dispose();
        font.dispose();
        zivljenja.dispose();
        sr.dispose();
        heart.dispose();
        coin.dispose();
        denarBitmapFont.dispose();
        scoreBitmapFont.dispose();
        tankPrice.dispose();
        addIcon.dispose();
        upgradeIcon.dispose();
        refundIcon.dispose();
        upgradePrice.dispose();
        removeReturn.dispose();
        timer.cancel();
        timer.purge();


        //coreTank.dispose();
        //bodyOfTank.dispose();
        for (Tower s : towers) {
            s.bodyOfTank.dispose();
            s.coreTank.dispose();
            s.coreShoot.dispose();
        }

        for (Tower s : samples) {
            s.bodyOfTank.dispose();
            s.coreTank.dispose();

        }
    }

    //Get middle of screen and adjust for message size
    @Override
    public void render() {
        //time = (++time) % 300;

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        sr.begin(ShapeRenderer.ShapeType.Filled);




        /*if (time == 299) {
            enemies.add(new Enemy(rnd.nextInt(100) + 25, rnd.nextInt(10) + 1));
        }
        */

        //enemies.add(new Enemy(10,1));

        handleEnemies();

        for (Enemy e : enemies) {
            sr.circle(e.y, e.x, e.size);
            e.premikaj();
            if (e.y >= screenWidth) { // if enemy comes to finish
                this.life--;
                enemies.remove(e);
            }
        }

        if (life < 1) {
            System.exit(0);
        }

        //sr.setColor((float)1.0*66/255, (float)1.0*215/255, (float)1.0*244/255,1);
        sr.setColor((float) 0.9, (float) 0.9, (float) 0.9, 1);
        sr.rect(0, 200, screenWidth, 25); // y  , x , height , width
        sr.rect(0, screenHeight - 225, screenWidth, 25); // y , x ,height , width

        sr.rect(screenWidth - SIZE_BOTTOM, 0, SIZE_BOTTOM, screenHeight); // y , x ,height , width

        sr.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
        sr.rect(0, 0, 50, screenHeight);

        sr.end();

        if (!platno) {
            selectedMenu = false; //ce ni pravo platno izbrano, bo selectedMenu avtomatsko false
        }

        handleSpodnjoplatno();

        batchrotated.begin();
        Matrix4 mx4Font = new Matrix4();
        mx4Font.setToRotation(new Vector3(0, 0, 1), 90);
        batchrotated.setTransformMatrix(mx4Font);//batchrotated.getTransformMatrix().setToRotation(new Vector3(0,0,1),90));
        denarBitmapFont.setColor(0f, 0f, 0f, 1);
        denarBitmapFont.getData().setScale(3);
        denarBitmapFont.draw(batchrotated, "x" + denar, 175 + 20, -5); // x je prva zdej, y je pa negativen zdej
        zivljenja.setColor(0f, 0f, 0f, 1);
        zivljenja.getData().setScale(3);
        zivljenja.draw(batchrotated, "x" + life, 75, -5); // x je prva zdej, y je pa negativen zdej
        zivljenja.setColor(0f, 0f, 0f, 1);
        zivljenja.getData().setScale(3);
        zivljenja.draw(batchrotated, String.format("Score:%2d", score), 400, -5); // x je prva zdej, y je pa negativen zdej

        handleSpodnjoplatnoTeksti();
        //batchrotated.end(); //this has no bug here, it has when synchronized with the other batch
        batchrotated.end();

        if (selected) {
            drawSelectionBox(Ys, Xs, Hs, Hs);
        } else {
            Hs = 0;
            Ws = 0;
            Hs = 0;
            Ws = 0;
        }

        batch.begin();


        if (selected) {
            batch.draw(upgradeIcon, screenWidth - SIZE_BOTTOM / 2 - 82, screenHeight / 3);
            batch.draw(refundIcon, screenWidth - SIZE_BOTTOM / 2 - 82, 2 * screenHeight / 3);
        }


        //font.draw(batch, message, 100, 100);
        heartS.setRotation(90);
        heartS.setPosition(0, 25);
        heartS.draw(batch);
        coinS.setPosition(0, 75 + 50 + 20);
        coinS.draw(batch);
        //batch.draw(tankBody,screenWidth/10,100);
        //tCore.setOrigin(10,10);
        handleTowers();
        handleCoreRotation();
        //batch.draw(tCore,screenWidth/10+5,105);


        //batch.setProjectionMatrix(batch.getTransformMatrix().rotate(new Vector3(0,0,1),90));
        //zivljenja.draw(batch,Integer.toString(life),0,screenWidth-10);

        batch.end();
    }

    //Return true to indicate that the event was handled
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        screenY = screenHeight - screenY; //obratna lgoika je annoying

        if (screenY < 200) {
            //TODO: leva logika
            right = false;
            leftOrRightsideLogic(screenX);
            if(!platno) {
                return true;
            }
        }
        if (screenY > screenHeight - 200) { //TODO Y je res X in obratno
            //TODO desna logika
            right = true;
            leftOrRightsideLogic(screenX);

            if(!platno) {
                return true;
            }
        }

        if (screenX > screenWidth - SIZE_BOTTOM) {
            if (platno) {
                for (int i = 1; i <= 5; i++) {
                    if (screenY < i * screenHeight / 5 + screenWidth / 10 && screenY > i * screenHeight / 5 - screenWidth / 10) {
                        selectedMenu = true;
                        indexOfTheSelected = i;
                        return true;

                    }
                }
            } else {

                if (screenY < screenHeight / 3 + SIZE_BOTTOM / 2 && screenY > screenHeight / 3 - SIZE_BOTTOM / 2) {
                    //TODO dodej logiko za upgrejde
                    try {
                        if (selectedTower(selX, selY, selRight).getTier() <= 4) {
                            if (Tower.upgradeCost[selectedTower(selX, selY, selRight).getTier()] <= denar) {
                                denar -= Tower.upgradeCost[selectedTower(selX, selY, selRight).getTier()];
                                selectedTower(selX, selY, selRight).onUpgrade();
                            }
                        }
                    } catch (Exception a) {
                        a.printStackTrace();
                    }
                    return true;

                }
                if (screenY < 2 * screenHeight / 3 + SIZE_BOTTOM / 2 && screenY > 2 * screenHeight / 3 - SIZE_BOTTOM / 2) {
                    //TODO dodej logiko za brisanje
                    try {
                        handleRemoveTower(selX, selY);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }


        }

        platno = true; //TODO ce v prazno kliknes mori it platno na true, ko pa kliknes na tower gre pa na false
        selected = false;
        selectedMenu = false;
        indexOfTheSelected = 0; //noben ni selektiran, torej 0 kot zmenjeno
        right = false;
        selX = 0;
        selY = 0;
        selRight = false;
        //	message = "Touched down at " + screenX + ", " + screenY;
        return true;
    }

    public void leftOrRightsideLogic(int screenX) {
        float s = (screenX - screenWidth / 10) * 1.0f / (screenWidth / 5);
        int localY = 0;
        //int localY=(int)(Math.ceil(s)*screenWidth/5);
        for (int i = 0; i < Math.ceil(s); i++) {
            localY += screenWidth / 5; //da je ista sorta mnozenja kot pri generiranju
        }
        if (selectedMenu) {
            selected = false; // TODO na novo dodan preglej ce si s tem pofejlu
            if (selectedMenu) {
                if (localY <= screenWidth * 4 / 5 && localY > 100) {
                    if (right) {
                        handleAddTower(screenHeight - 100, localY, right);
                    } else {
                        handleAddTower(100, localY, right);
                    }
                }
            }
            return;
        }
        if (checkIfTowerThere(localY, right)) { // ce je tower tam ga selektiri TODO spremeba iz ScreenX v LocalY
            platno = false; //
            if (right) {
                int x = screenHeight - 100;
                selX = x;
                selY = localY;
                selRight = right;
                selektirajTowerLogika(x, localY, right);
            } else {
                int x = 100;
                selX = x;
                selY = localY;
                selRight = right;
                selektirajTowerLogika(x, localY, right);
            }
        }

    }

    public boolean checkIfTowerThere(int y, boolean right) {
        int[] towersLeft = new int[5];
        int[] towersRight = new int[5];
        int countleft = 0, countright = 0;


        for (Tower s : towers) {
            if (s.desna) {
                towersRight[countright] = s.getY();
                countright++;
            } else {
                towersLeft[countleft] = s.getY();
                countleft++;
            }
        }
        if (right) {
            for (int thisy : towersRight) {
                if (thisy == y) {
                    return true;
                }
            }
        } else {
            for (int thisy : towersLeft) {
                if (thisy == y) { //TODO pogrutni zakaj tuki pri enakih ne gre v if
                    return true;
                }
            }
        }
        return false;
    }

    //When finger is lifted up
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        //	message = "Touch up at " + screenX + ", " + screenY;
        return true;
    }

    //While dragging finger across screen
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        //	message = "Dragging at " + screenX + ", " + screenY;
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

}

class TajmerHendl extends TimerTask{
    @Override
    public void run() {
        //System.out.println("mmmmm");
        MyGdxGame.time++;

        MyGdxGame.time = MyGdxGame.time % 120;
        MyGdxGame.readytoInititate=true;
    }
}