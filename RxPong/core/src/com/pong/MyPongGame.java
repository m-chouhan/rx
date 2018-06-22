package com.pong;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class MyPongGame extends ApplicationAdapter {

    BitmapFont font;
    SpriteBatch batch;
    Integer score = 0;

    @Override
    public void create() {
        Gdx.graphics.setContinuousRendering(false);
        font = new BitmapFont();
        font.setColor(Color.BLUE);
        font.getData().setScale(5);
        batch = new SpriteBatch();

        Observable
                .interval(500, TimeUnit.MILLISECONDS)
                .subscribe(count -> Gdx.graphics.requestRendering());
        Observable.interval(1, TimeUnit.SECONDS).subscribe((time) -> score++);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        font.draw(batch, score.toString(), 10, 100);
        batch.end();
    }

    @Override
    public void dispose() {
    }
}
