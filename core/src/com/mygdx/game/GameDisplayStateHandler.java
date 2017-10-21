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
    }

    public void welcomeScreen(){
        try {

            TimeUnit.SECONDS.sleep(3l);
        }
        catch (Exception e){
            for (int i=0; i<100000 ; i++){
                //short forloop delay if sleep gets interrupted
            }
        }
        game.displayState = 1; // after welcome screen delay, render will change the state

    }


}
