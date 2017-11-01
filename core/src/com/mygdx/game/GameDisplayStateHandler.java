package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;

import java.util.concurrent.TimeUnit;
/**
 * Created by Miha on 21.10.2017.
 */

public class GameDisplayStateHandler {
    //short level;
    MyGdxGame game;
    //boolean firstInit;


    public GameDisplayStateHandler(MyGdxGame game){
        //level = game.displayState;
        this.game = game;
        game.displayState = 0;
        //firstInit = false;
    }

    public void welcomeScreen(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    //TimeUnit.SECONDS.sleep(5);
                    Thread.sleep(500);
                }
                catch (Exception e){
                    for (int i=0; i<100000 ; i++){
                        //short for loop delay if sleep gets interrupted
                    }
                }
                mainMenu();//game.displayState = 1; // after welcome screen delay, render will change the state
            }


            //TODO implement here to go back to main menu after certain amount of time
        }).start();



    }

    public void startGame(){
        game.displayState = 2;
        game.startTime = System.nanoTime();
        Gdx.input.setInputProcessor(game);
    }

    public void gameOver(){
        game.displayState = 3;
    }

    public void mainMenu(){
        //game.reset();
        game.reInit();
        Gdx.input.setInputProcessor(game.stage);
        game.displayState = 1;
    }


}
