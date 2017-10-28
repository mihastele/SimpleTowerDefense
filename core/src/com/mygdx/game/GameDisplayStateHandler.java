package com.mygdx.game;


import java.util.concurrent.TimeUnit;
/**
 * Created by Miha on 21.10.2017.
 */

public class GameDisplayStateHandler {
    //short level;
    MyGdxGame game;


    public GameDisplayStateHandler(MyGdxGame game){
        //level = game.displayState;
        this.game = game;
        game.displayState = 0;
    }

    public void welcomeScreen(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    //TimeUnit.SECONDS.sleep(5);
                    Thread.sleep(5000);
                }
                catch (Exception e){
                    for (int i=0; i<100000 ; i++){
                        //short forloop delay if sleep gets interrupted
                    }
                }
                mainMenu();//game.displayState = 1; // after welcome screen delay, render will change the state
            }
        }).start();



    }

    public void startGame(){
        game.displayState = 2;
    }

    public void gameOver(){
        game.displayState = 3;
    }

    public void mainMenu(){
        game.displayState = 1;
    }


}
