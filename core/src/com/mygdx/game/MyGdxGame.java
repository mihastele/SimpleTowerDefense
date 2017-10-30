package com.mygdx.game;


import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.net.HttpParametersUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayDeque;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.badlogic.gdx.Gdx.graphics;


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
    private short X_CORE_OFFSET = 100;
    public short displayState = 0;

    //Screen dimensions will be used to center text
    private BitmapFont font, zivljenja, denarBitmapFont, scoreBitmapFont, tankPrice, upgradePrice, removeReturn, endScoreText;
    private int screenWidth, screenHeight;
    private ShapeRenderer sr;
    private Texture addIcon, upgradeIcon, refundIcon;
    //private Sprite tankBody,tCore;
    //private String message = "Touch me";

    private short endX, endY;//,endTimer; // end scene variables to draw the gameOver screen

    Texture bg, bgWall, bgCastleFloor, loadingScreen; //handling backgrounds

    Sprite ldgScreen;

    Texture gameOverTexture;
    Sprite gameOverSprite;

    ///////////MENU BUTTONS HANDLINGS ////////

    TextureAtlas menuBtns;

    Skin menuBtnsSkin;

    Button playBtn, top10Btn, quitBtn;

    Table table;

    Stage stage;

    /////////////////////////////////////////

    GameDisplayStateHandler gameHandler;

    private boolean selected = false, selectedMenu = false, platno = true; // selected-pove ali je kater ot towerjev selektiran, selectedMenu pove ali je kater na meniju selektiran(dela samo za tower meni),
    //platno pove ali je na dodajanju towerjev(true) ali pa na upgrejdanju teh(false)
    private int indexOfTheSelected = 0, countForIndex = 0, selX = 0, selY = 0;
    private boolean selRight = false;

    ArrayDeque<Tower> samples = new ArrayDeque<Tower>();

    private int Xs = 0, Ys = 0, Hs = 0, Ws = 0; //draw box to select, TODO poprav to da bo delal na pravo mesto da se za towerjam narise

    Timer timer;

    public static int time = 0;
    public static boolean readytoInititate = false;
    private int timeDelay = 3;
    private int speedDeviation = 0;
    private int sizeDeviation = 0;
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
                t.tCore.setPosition(t.getY(), t.getX() + t.getOffsetCoreY());
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
                t.tShoot.setPosition(t.getY(), t.getX() + t.getOffsetShootY());
                t.tShoot.draw(batch);
            }

        }

    }

    public void handleTowers() {
        for (Tower s : towers) {
            batch.draw(s.tankBody, s.getY() + s.getOffsetBodyX(), s.getX() + s.getOffsetBodyY());
            //s.aliJeVDosegu(enemies);

        }
    }

    public void handleSamples() {
        for (Tower s : samples) {
            this.batch.begin();
            batch.draw(s.tankBody, s.getY() + s.getOffsetBodyX(), s.getX() + s.getOffsetBodyY());
            s.tCore.setRotation(90);
            s.tCore.setPosition(s.getY(), s.getX() + s.getOffsetCoreY());
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
        Tower t = null;
        int ini = 1; //samples.size(); ce bi bil push a arajdekju
        for (Tower s : samples) {
            if (indexOfTheSelected == ini) {
                t = s;
            }
            ini++;
        }

        try {

            if (denar >= t.getCost() && !checkIfTowerThere(y, desno)) {
                denar -= t.getCost(); //TODO IMPORTANT: KO BOS DODAJU VEC TOWERJEV BOS TUKI SU CEZ SWITCH INDEKSOFSELECTED DA BOS VEDU KER TOWER MORS POSTAVT NAPREJ!!!!!
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
                try {
                    switch (indexOfTheSelected) {
                        case 1:
                            towers.add(new BasicTower(x, y, desno));
                            break;
                        case 2:
                            towers.add(new StrongTower(x, y, desno));
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleRemoveTower(int x, int y) { //TODO logika odstranjevanja towerja
        if (right) {
            prostaPolja.insert(prostaPoljaMeja, y);
        } else {
            prostaPolja.insert(0, y);
            prostaPoljaMeja++;
        }
        denar += selectedTower(x, y, selRight).getPrice() / 2;
        towers.remove(selectedTower(x, y, selRight));
    }

    public void selektirajTowerLogika(int x, int y, boolean desna) {
        selected = true;
        Xs = x - ((X_CORE_OFFSET / 2));// /2 in the end erased
        Ys = y - X_CORE_OFFSET / 2;
        Hs = X_CORE_OFFSET;//Ws=130;

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
                tankPrice.draw(batchrotated, "-" + s.getCost(), screenHeight / 5 * i - (("cost:" + s.getCost()).length() / 2) * 4 * 3, -(screenWidth - SIZE_BOTTOM / 2 + 25));//(("cost:"+s.getCost()).length()/2)*4*5 odmik teksta polvic krat konstanta vleikosti
                i++;
            }
        } else {
            try {

                Tower t = null;
                for (Tower s : towers) {
                    if (s.getX() == selX && s.getY() == selY) {
                        t = s;
                    }
                }
                //upgrade text logic here TODO
                if (selected) {
                    if (selectedTower(selX, selY, selRight).getTier() < 5) {
                        upgradePrice.draw(batchrotated, "-" + t.upgradeCost[selectedTower(selX, selY, selRight).getTier()], screenHeight / 3 - 10, -(screenWidth - SIZE_BOTTOM / 2));
                    } else {
                        upgradePrice.draw(batchrotated, "Max", screenHeight / 3 - 10, -(screenWidth - SIZE_BOTTOM / 2));
                    }
                    removeReturn.draw(batchrotated, "+" + selectedTower(selX, selY, selRight).getPrice() / 2, 2 * screenHeight / 3 - 10, -(screenWidth - SIZE_BOTTOM / 2));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

    public void drawPluses() {
        batch.begin();
        for (int i = 0; i < prostaPolja.size; i++) {
            if (i < prostaPoljaMeja) {
                batch.draw(addIcon, prostaPolja.get(i) - 48, (X_CORE_OFFSET / 2) - 48); //odmik je ravno četrtina velikosti slike
            } else
                batch.draw(addIcon, prostaPolja.get(i) - 48, screenHeight - (X_CORE_OFFSET / 2) - 48);
        }
        batch.end();
    }

    public void handleEnemies() {
//        private int time=0,timeSeconds = 0; TODO zanimajo nas te deli kode
//        private int timeDelay=3;
//        private int speedDeviation=0;
//        private int sizeDeviation=0;
//        private int enemyType = 0;

        int deltaLifeBuffMultiplier = 1;
        // int i= 500000000;
        //boolean milisekunde=false;
        /*long startTime= System.nanoTime();

        time+=System.nanoTime()-startTime;
        if(time>=1000000000){ //sestevanje sekund
            time=time%1000000000;
            timeSeconds++;
        }*/


        switch (enemyType) {
            case 0:
                sr.setColor(Color.GREEN);
                deltaLifeBuffMultiplier = 1;
                break;
            case 1:
                sr.setColor(Color.SKY);
                deltaLifeBuffMultiplier = 2;
                break;
            case 2:
                sr.setColor(Color.BLUE);
                deltaLifeBuffMultiplier = 5;
                break;
            case 3:
                sr.setColor(Color.PURPLE);
                deltaLifeBuffMultiplier = 10;
                break;
            case 4:
                sr.setColor(Color.RED);
                deltaLifeBuffMultiplier = 20;
                break;
            case 5:
                sr.setColor(Color.GOLD);
                deltaLifeBuffMultiplier = 30;
                break;

            default:
                sr.setColor(Color.GOLD);
                deltaLifeBuffMultiplier += 1;
        }

        if (time == 119 && enemyType < 6 && readytoInititate) {
            readytoInititate = false; //enkrat izved
            enemyType++;
            sizeDeviation += 20;
            speedDeviation += 1;
        } else if (time == 119 && readytoInititate) {
            readytoInititate = false;
            sizeDeviation += 10;
            speedDeviation += 1;
        }

        Random rnd = new Random();

        if (time < 20 && readytoInititate) {
            if (time % 10 == 0) {
                int speed = rnd.nextInt(4) + 1 + speedDeviation;
                int size = rnd.nextInt(20) + 25 + sizeDeviation;
                enemies.add(new BasicEnemy(size, speed, deltaLifeBuffMultiplier));
            }

        } else if (time < 40 && readytoInititate) {
            if (time % 6 == 0) {
                int speed = rnd.nextInt(4) + 1 + speedDeviation;
                int size = rnd.nextInt(30) + 25 + sizeDeviation;
                enemies.add(new BasicEnemy(size, speed, deltaLifeBuffMultiplier));
            }

        } else if (time < 80 && readytoInititate) {
            if (time % 3 == 0) {
                int speed = rnd.nextInt(4) + 1 + speedDeviation;
                int size = rnd.nextInt(100) + 25 + sizeDeviation;
                enemies.add(new BasicEnemy(size, speed, deltaLifeBuffMultiplier));
            }

        } else {
            if (time % 2 == 0 && readytoInititate) {
                int speed = rnd.nextInt(4) + 1 + speedDeviation;
                int size = rnd.nextInt(100) + 25 + sizeDeviation;
                enemies.add(new BasicEnemy(size, speed, deltaLifeBuffMultiplier));
            }
        }
        readytoInititate = false;

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
        startCoreGame();
        mainMenuInit();
        menuHelperOnStart();
        reset();
    }

    public void startCoreGame() {
        //denar = 400000;


        screenWidth = graphics.getWidth();
        screenHeight = graphics.getHeight();

        loadingScreen = new Texture("loadingScreen.png");
        loadingScreen.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

        bg = new Texture("pattern.jpg");
        bg.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        bgWall = new Texture("castlewall.png");
        bgWall.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        bgCastleFloor = new Texture("castleFloor.png");
        bgCastleFloor.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);


        batch = new SpriteBatch();
        batchrotated = new SpriteBatch();


        gameOverTexture = new Texture("gameOver.png");
        gameOverSprite = new Sprite(gameOverTexture);



        gameOverSprite.setSize(screenHeight/2, screenHeight/2/4);

        //gameOverSprite.setOrigin(gameOverSprite.getRegionWidth()/2,gameOverSprite.getRegionHeight()/2);
        gameOverSprite.setOriginCenter();


        gameOverSprite.setPosition(screenWidth/8, screenHeight/2);

        gameOverSprite.setRotation(90);


        //gameOverSprite.setScale(screenHeight / 2, screenWidth / 2);


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
            timer.schedule(new TajmerHendl(), 0, 500);

        } catch (Exception e) {

        }





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
        for (int i = screenWidth / 5; i < screenWidth * 2 - screenWidth / 5 + 1; i = i + screenWidth / 5) { //gre od prvega do zadnjega na levi ki je eden manjši od indeksa, ter naprej na desne isto
            if (i < screenWidth - 10) { //10 je odmik za int TODO prej je blo 10 ce pride bug poprav tuki... nevermind zgornja for zanka je mela bug
                prostaPolja.add(i % screenWidth);
                temp = prostaPolja.size;
            } else if (i > screenWidth)
                //prostaPolja.add( (i+screenWidth/5) % screenWidth);
                prostaPolja.add(prostaPolja.get((i / (screenWidth / 5) - 2) % temp)); //-2 ker je zamik za eno in ker enga preskočmo je zamik 2 al neki tazga nvm, java logic...
        }

        prostaPoljaMeja = prostaPolja.size / 2;

        samples.add(new BasicTower((screenHeight / 5), screenWidth * 9 / 10 + screenWidth / 20 - 25));
        samples.add(new StrongTower((screenHeight * 2 / 5), screenWidth * 9 / 10 + screenWidth / 20 - 25));


        //enemies.add(new BasicEnemy(10, 2, screenHeight / 2)); //old, new buff would make this one unstoppable

        if (screenHeight / 5 > 100) {
            X_CORE_OFFSET = (short) (screenHeight / 5); //TODO test if typecasting works ok
        }


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

    public void menuHelperOnStart() {
        gameHandler = new GameDisplayStateHandler(this);
        gameHandler.welcomeScreen();
        ldgScreen = new Sprite(loadingScreen);
        ldgScreen.setSize(screenWidth, screenHeight);
        ldgScreen.setPosition(0, 0);

    }

    public void reset(){
        enemyType = 0;
        life = 6;
        score = 0l;
        denar = 400;
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
        endScoreText.dispose();
        tankPrice.dispose();
        addIcon.dispose();
        upgradeIcon.dispose();
        refundIcon.dispose();
        upgradePrice.dispose();
        removeReturn.dispose();
        timer.cancel();
        timer.purge();
        bg.dispose();
        bgWall.dispose();
        bgCastleFloor.dispose();
        loadingScreen.dispose();
        gameOverTexture.dispose();


        menuBtns.dispose();
        menuBtnsSkin.dispose();
        stage.dispose();

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

        for (Enemy s : enemies) {
            s.bodyTexture.dispose();

        }
    }

    //Get middle of screen and adjust for message size
    @Override
    public void render() { //core render
        //time = (++time) % 300;

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        switch (displayState) {
            case 0:
                batch.begin();

                ldgScreen.draw(batch);
                //batch.draw(loadingScreen,0,0,0,0, screenWidth, screenHeight);
                batch.end();
                break;
            case 1:
                stage.draw();
                break;
            case 2:
                drawMainGame();
                break;
            default:
                endGameLogic();


        }

        //drawMainGame(); under state 2
    }

    public void drawMainGame() {

        /*if(displayState !=3) { //todo remove
            endGameSync(); //todo remove
            gameHandler.gameOver(); //todo remove
        }*/

        sr.begin(ShapeRenderer.ShapeType.Filled);

        batch.begin();
        batch.draw(bg, 0, X_CORE_OFFSET, 0, 0, screenWidth, screenHeight - 2 * X_CORE_OFFSET);
        batch.draw(bgCastleFloor, 0, 0, 0, 0, screenWidth, X_CORE_OFFSET);
        batch.draw(bgCastleFloor, 0, screenHeight - X_CORE_OFFSET, 0, 0, screenWidth, X_CORE_OFFSET);

        batch.end();

        handleEnemies();


        if (life < 1 && displayState != 3) { //displaystate prevention of spamming
            endGameSync();
            gameHandler.gameOver();
        }

        //sr.setColor((float)1.0*66/255, (float)1.0*215/255, (float)1.0*244/255,1);
        sr.setColor((float) 0.9, (float) 0.9, (float) 0.9, 1);

        ///////////wall drawing//////////////////
        batch.begin();
        batch.draw(bgWall, 0, X_CORE_OFFSET, 0, 0, screenWidth, 25); //25 is the thickness of wall
        batch.draw(bgWall, 0, screenHeight - X_CORE_OFFSET - 25, 0, 0, screenWidth, 25);
        batch.end();

        //sr.rect(0, X_CORE_OFFSET, screenWidth, 25); // y  , x , height , width
        //sr.rect(0, screenHeight - X_CORE_OFFSET - 25, screenWidth, 25); // y , x ,height , width // X_CORE:OFFSET -25 default
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
            drawSelectionBox(Ys, Xs, Hs, Hs); //Hs == Ws, optimize later
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


        try {

            for (Enemy e : enemies) {
                e.bodySprite.setPosition(e.y, e.x);
                e.bodySprite.draw(batch);
                e.premikaj();
                if (e.y >= screenWidth) { // if enemy comes to finish
                    this.life--;
                    enemies.remove(e);
                }
            }
        }catch (Exception e){

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

    public void endGameLogic() {

        if (endX < screenHeight / 2) {
            drawMainGame();
        }

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.GRAY);
        sr.rect(screenWidth / 2 - endY, screenHeight / 2 - endX, endY * 2, endX * 2);
        sr.end();


        if (endX >= screenHeight / 2) {
            batch.begin();

            gameOverSprite.draw(batch);

            //batch.draw(gameOverSprite, screenHeight / 4, screenWidth / 8, screenHeight / 2, screenWidth / 2);
            batch.end();
            batchrotated.begin();
            endScoreText.draw(batchrotated, "score:" + score, screenHeight/2 - endScoreText.getLineHeight(), -screenWidth/2);
            batchrotated.end();
        }
    }

    public void endGameSync() { //data parallel synchronization for rendering
        //endTimer = 0;
        endX = 0; //TODO prever sinhronizacijo!
        endY = 0;

        endScoreText = new BitmapFont();
        endScoreText.getData().setScale(3);

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (endX = 0; endX < (screenHeight) / 2; endX++) {
                    endY = (short) ((screenWidth * endX * 1.0) / screenHeight); //here only the variables change, rendering calls endGameLogic where the rendering will take place
                    try {
                        Thread.sleep(7);
                    } catch (InterruptedException e) {

                    }

                }

                try {
                    Thread.sleep(5000);
                }
                catch (Exception e){

                }

                gameHandler.mainMenu();
            }
        }).start();


        new NetAPITest().create();
        //handleRequest();




    }

    /*public void handleRequest(){
        Net.HttpRequest httpGet = new Net.HttpRequest(Net.HttpMethods.GET);
        httpGet.setUrl("http://www.api.nejcribic.com/TowerDefenseStatistics/root/app/api.php?send/time=100&score=100&date=1020&device=android");
        //httpGet.setContent(HttpParametersUtils.convertHttpParameters(parameters));
        Gdx.net.sendHttpRequest (httpGet, new Net.HttpResponseListener() {
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String status = httpResponse.getResultAsString();
                //do stuff here based on response
            }

            public void failed(Throwable t) {
                String status = "failed";
                //do stuff here based on the failed attempt
            }

            @Override
            public void cancelled() {
                return;
            }
        });
    }*/

    public void mainMenuInit() {
        menuBtns = new TextureAtlas("button.pack");
        menuBtnsSkin = new Skin(menuBtns);

        stage = new Stage();

        table = new Table();
        table.setBounds(0, 0, screenWidth, screenHeight);

        playBtn = new Button(menuBtnsSkin.getDrawable("playBtn"));
        top10Btn = new Button(menuBtnsSkin.getDrawable("top10"));
        quitBtn = new Button(menuBtnsSkin.getDrawable("quit"));

        playBtn.setTransform(true);
        top10Btn.setTransform(true);
        quitBtn.setTransform(true);

        playBtn.setOrigin(menuBtnsSkin.getDrawable("playBtn").getMinWidth() / 2, menuBtnsSkin.getDrawable("playBtn").getMinHeight() / 2);
        top10Btn.setOrigin(menuBtnsSkin.getDrawable("top10").getMinWidth() / 2, menuBtnsSkin.getDrawable("top10").getMinHeight() / 2);
        quitBtn.setOrigin(menuBtnsSkin.getDrawable("quit").getMinWidth() / 2, menuBtnsSkin.getDrawable("quit").getMinHeight() / 2);


        playBtn.setRotation(90);
        top10Btn.setRotation(90);
        quitBtn.setRotation(90);

        /*ChangeListener cl = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).getName().equals("playBtn")){
                    gameHandler.startGame();
                }
            }
        };

        playBtn.addListener(cl);*/

        playBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                gameHandler.startGame();
            }
        });

        top10Btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //TODO Implement
            }
        });

        quitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dispose();
                System.exit(0);
            }
        });


        table.add(playBtn);
        table.add(top10Btn);
        table.add(quitBtn);

        /*table.setTransform(true);

        table.setRotation(90);
        table.setOrigin(screenWidth/2,screenHeight/2);
        table.setPosition(0,0);
        //OBRNE VSE UKUP NA 90 STOPINJ IN SO NAROBE POSTACKAN*/
        stage.addActor(table);

    }

    //Return true to indicate that the event was handled
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        screenY = screenHeight - screenY; //obratna logika je annoying

        if (screenY < X_CORE_OFFSET) {
            //TODO: leva logika
            right = false;
            leftOrRightsideLogic(screenX);
            if (!platno) {
                return true;
            }
        }
        if (screenY > screenHeight - X_CORE_OFFSET) { //TODO Y je res X in obratno
            //TODO desna logika
            right = true;
            leftOrRightsideLogic(screenX);

            if (!platno) {
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
                        Tower t = null;
                        for (Tower s : towers) {
                            if (s.getX() == selX && s.getY() == selY) {
                                t = s;
                            }
                        }

                        if (selectedTower(selX, selY, selRight).getTier() <= 4) {
                            if (t.upgradeCost[selectedTower(selX, selY, selRight).getTier()] <= denar) {
                                denar -= t.upgradeCost[selectedTower(selX, selY, selRight).getTier()];
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
                if (localY <= screenWidth * 4 / 5 && localY > X_CORE_OFFSET / 2) {
                    if (right) {
                        handleAddTower(screenHeight - X_CORE_OFFSET / 2, localY, right);
                    } else {
                        handleAddTower(X_CORE_OFFSET / 2, localY, right);
                    }
                }
            }
            return;
        }
        if (screenX > screenWidth / 5) { //to prepreči selekstiranje preveč navzgor kjer ni towerjev kvecjemu so
            if (checkIfTowerThere(localY, right)) { // ce je tower tam ga selektiri TODO spremeba iz ScreenX v LocalY
                platno = false; //
                if (right) {
                    int x = screenHeight - X_CORE_OFFSET / 2;
                    selX = x;
                    selY = localY;
                    selRight = right;
                    selektirajTowerLogika(x, localY, right);
                } else {
                    int x = X_CORE_OFFSET / 2;
                    selX = x;
                    selY = localY;
                    selRight = right;
                    selektirajTowerLogika(x, localY, right);
                }
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

class TajmerHendl extends TimerTask {
    @Override
    public void run() {
        //System.out.println("mmmmm");
        MyGdxGame.time++;

        MyGdxGame.time = MyGdxGame.time % 120;
        MyGdxGame.readytoInititate = true;
    }
}