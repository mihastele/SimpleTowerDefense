package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import java.util.Date;

/**
 * Created by Miha on 10/17/2017.
 */

public class NetAPITest implements HttpResponseListener {

    /*SpriteBatch batch;
    Skin skin;
    Stage stage;
    TextButton btnDownloadImage;
    TextButton btnDownloadText;
    TextButton btnDownloadLarge;
    TextButton btnDownloadError;
    TextButton btnPost;
    TextButton btnCancel;
    TextButton btnOpenUri;
    Label statusLabel;
    Texture texture;
    String text;
    BitmapFont font;*/
    HttpRequest httpRequest;

    public void create(long time, long score, Date date, String device) {

        String url;
        String httpMethod = Net.HttpMethods.GET;
        String requestContent = null;


        url = "http://api.nejcribic.com/TowerDefenseStatistics/root/app/api.php?send/time="+time+"&score="+score+"&date="+date.getTime()+"&device="+device;//"http://libgdx.badlogicgames.com/releases/libgdx-1.2.0.zip";

        httpRequest = new HttpRequest(httpMethod);
        httpRequest.setUrl(url);
        httpRequest.setContent(requestContent);
        Gdx.net.sendHttpRequest(httpRequest, NetAPITest.this);

        System.out.println(("Downloading data from " + httpRequest.getUrl()));
    }


    @Override
    public void handleHttpResponse(HttpResponse httpResponse) {
        final int statusCode = httpResponse.getStatus().getStatusCode();
        // We are not in main thread right now so we need to post to main thread for ui updates
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                Gdx.app.log("sss", ("HTTP Request status: " + statusCode));
            }
        });

        if (statusCode != 200) {
            Gdx.app.log("NetAPITest", "An error ocurred since statusCode is not OK");
            return;
        } else {
            Gdx.app.log("sss", ("HTTP Request status: " + statusCode));
            String s = httpResponse.getResultAsString();
        }


    }

    @Override
    public void failed(Throwable t) {
        Gdx.app.log("lol","gg");
    }

    @Override
    public void cancelled() {

    }
}

