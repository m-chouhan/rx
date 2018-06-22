package com.pong;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class MyPongGame extends ApplicationAdapter {

    //View Objects
    BitmapFont font;
    SpriteBatch batch;
    Integer score = 0;
    OrthographicCamera camera;

    private int SCREEN_WIDTH, SCREEN_HEIGHT;

    @Override
    public void create() {
        font = new BitmapFont();
        font.setColor(Color.BLUE);
        font.getData().setScale(5);
        batch = new SpriteBatch();
        camera = Utility.setupCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        SCREEN_WIDTH = Gdx.graphics.getHeight();
        SCREEN_HEIGHT = Gdx.graphics.getWidth();

        Observable
                .interval(500, TimeUnit.MILLISECONDS)
                .subscribe(count -> Gdx.graphics.requestRendering());
        Observable.interval(1, TimeUnit.SECONDS).subscribe((time) -> score++);

        //libgdx config changes
        Gdx.graphics.setContinuousRendering(false);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.draw(batch, score.toString(), SCREEN_WIDTH / 2 - 50, 100);
        batch.end();
    }

    @Override
    public void dispose() {
    }
}
