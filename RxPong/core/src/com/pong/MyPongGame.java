package com.pong;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class MyPongGame extends ApplicationAdapter {

    //View Objects
    private BitmapFont font;
    private SpriteBatch batch;
    private Integer score = 0;
    private OrthographicCamera camera;
    private OrthographicCamera myWorldcamera;
    private ShapeRenderer shapeRenderer;
    private Box2DDebugRenderer box2DRenderer;
    private float SCREEN_WIDTH, SCREEN_HEIGHT;
    private float WORLD_WIDTH, WORLD_HEIGHT;
    private float SCALE_FACTOR;
    private Observable<InputObservable.InputEvent> inputStream;
    private World world;
    private Circle ball;
    private Box box;
    private Box left;
    private Box right;
    private Box top;
    private Box bottom;

    @Override
    public void create() {
        font = new BitmapFont();
        font.setColor(Color.BLUE);
        font.getData().setScale(5);
        batch = new SpriteBatch();

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        SCREEN_WIDTH = (int) h;
        SCREEN_HEIGHT = (int) w;
        WORLD_WIDTH = 10;
        SCALE_FACTOR = SCREEN_WIDTH / WORLD_WIDTH;
        WORLD_HEIGHT = SCREEN_HEIGHT / SCALE_FACTOR;

        camera = Utility.setupCamera(w, h);
        myWorldcamera = Utility.setupCamera(WORLD_HEIGHT, WORLD_WIDTH);

        shapeRenderer = new ShapeRenderer();
        box2DRenderer = new Box2DDebugRenderer();
        world = new World(new Vector2(0, -5f), false);
        ball = new Circle(world, new Vector2(WORLD_WIDTH / 2, WORLD_HEIGHT / 2), 0.5f, BodyDef.BodyType.DynamicBody, SCALE_FACTOR);
        ball.setLinearVelocity(new Vector2(4, 4));
        box = new Box(world, new Vector2(WORLD_WIDTH / 2, 0.5f), new Vector2(4, 1), BodyDef.BodyType.DynamicBody, SCALE_FACTOR);

        left = new Box(world, new Vector2(0, WORLD_HEIGHT / 2), new Vector2(0.5f, WORLD_HEIGHT), BodyDef.BodyType.StaticBody, SCALE_FACTOR);
        right = new Box(world, new Vector2(WORLD_WIDTH, WORLD_HEIGHT / 2), new Vector2(0.5f, WORLD_HEIGHT), BodyDef.BodyType.StaticBody, SCALE_FACTOR);
        top = new Box(world, new Vector2(WORLD_WIDTH / 2, WORLD_HEIGHT), new Vector2(WORLD_WIDTH, 0.5f), BodyDef.BodyType.StaticBody, SCALE_FACTOR);
        bottom = new Box(world, new Vector2(WORLD_WIDTH / 2, 0), new Vector2(WORLD_WIDTH, 0.5f), BodyDef.BodyType.StaticBody, SCALE_FACTOR);

        //libgdx config changes
        Gdx.graphics.setContinuousRendering(false);

        inputStream = InputObservable.create(myWorldcamera);
        inputStream
                .filter(event -> event.type == InputObservable.EventType.DOWN)
                .buffer(1000, TimeUnit.MILLISECONDS)
                .doOnEach(item -> Gdx.app.log("Count", "" + item.getValue().size()))
                .filter(items -> items.size() > 1)
                .take(1)
                .ignoreElements().subscribe(this::startGame);
    }

    public void startGame() {
        Gdx.app.log("Game", "Started");
        Observable
                .interval(20, TimeUnit.MILLISECONDS)
                .subscribe(count -> {
                    world.step(0.02f, 6, 2);
                    Gdx.graphics.requestRendering();
                });
        Observable.interval(2, TimeUnit.SECONDS).subscribe(time -> ball.scaleVelocity(1.1f));
        Observable.interval(1, TimeUnit.SECONDS).subscribe(time -> score++);
        inputStream
                .groupBy(event -> event.pointer)
                .subscribe(eventstream -> processTouchEvent(eventstream.share(), bottom.getBody(), box.getBody()));
    }

    private void processTouchEvent(Observable<InputObservable.InputEvent> inputStream, Body bodyA, Body bodyB) {

        inputStream
                .filter(event -> event.type == InputObservable.EventType.DOWN)
                .flatMapSingle(event -> {
                    Gdx.app.log("TouchDown", event.toString());
                    MouseJointDef def = new MouseJointDef();
                    def.bodyA = bodyA;
                    def.bodyB = bodyB;
                    //def.collideConnected = true;
                    def.target.set(event.x, bodyB.getPosition().y);
                    def.maxForce = 10000.0f;

                    return inputStream
                            .takeWhile(event1 -> event1.type != InputObservable.EventType.UP)
                            .reduce((MouseJoint) world.createJoint(def), (joint, ev) -> {
                                joint.setTarget(new Vector2(ev.x, bodyB.getPosition().y));
                                return joint;
                            });
                })
                .subscribe(joint -> {
                    Gdx.app.log("TouchEvent", "Action Complete");
                    world.destroyJoint(joint);
                });
    }


    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        box2DRenderer.render(world, myWorldcamera.combined);
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        ball.render(shapeRenderer);
        box.render(shapeRenderer);
        left.render(shapeRenderer);
        right.render(shapeRenderer);
        top.render(shapeRenderer);
        bottom.render(shapeRenderer);

        batch.begin();
        font.draw(batch, score.toString(), SCREEN_WIDTH / 2 - 50, 100);
        batch.end();

    }

    @Override
    public void dispose() {
    }
}
