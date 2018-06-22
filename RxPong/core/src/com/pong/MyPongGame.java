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
    private OrthographicCamera myWorldcamera;

    private int SCREEN_WIDTH, SCREEN_HEIGHT;
    private int WORLD_WIDTH = 10, WORLD_HEIGHT = 10;
    private Observable<InputObservable.InputEvent> inputStream;

    @Override
    public void create() {
        font = new BitmapFont();
        font.setColor(Color.BLUE);
        font.getData().setScale(5);
        batch = new SpriteBatch();

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        myWorldcamera = Utility.setupCamera(WORLD_WIDTH, WORLD_HEIGHT * (h / w));

        camera = Utility.setupCamera(w, h);
        SCREEN_WIDTH = (int) h;
        SCREEN_HEIGHT = (int) w;

        inputStream = InputObservable.create(camera);
        inputStream
                .filter(event -> event.type == InputObservable.EventType.DOWN)
                .buffer(1000, TimeUnit.MILLISECONDS)
                .doOnEach(item -> Gdx.app.log("Count", "" + item.getValue().size()))
                .filter(items -> items.size() > 2)
                .take(1)
                .ignoreElements()
                .subscribe(this::startGame);

        //libgdx config changes
        Gdx.graphics.setContinuousRendering(false);
    }

    public void startGame() {
        Observable
                .interval(500, TimeUnit.MILLISECONDS)
                .subscribe(count -> Gdx.graphics.requestRendering());
        Observable.interval(1, TimeUnit.SECONDS).subscribe((time) -> score++);
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
