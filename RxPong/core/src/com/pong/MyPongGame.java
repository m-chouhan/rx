package com.pong;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
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
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

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
    private Observable<Contact> collisionStream;
    private World world;
    private Circle ball;
    private Box boxA;
    private Box left;
    private Box right;
    private Box top;
    private Box bottom;
    private Box boxB;

    @Override
    public void create() {

        createViewPort();
        world = new World(new Vector2(0, -0f), false);
        CollisionDetector collisionDetector = new CollisionDetector();
        world.setContactListener(collisionDetector);
        collisionStream = collisionDetector.getEventStream();

        createObjects(world);
        createRenderers(camera);
        //libgdx config changes
        Gdx.graphics.setContinuousRendering(false);

        inputStream = InputObservable.create(myWorldcamera);
        inputStream
                .filter(event -> event.type == InputObservable.EventType.DOWN)
                .buffer(1000, TimeUnit.MILLISECONDS)
                //.doOnEach(item -> Gdx.app.log("Count", "" + item.getValue().size()))
                .filter(items -> items.size() > 1)
                .take(1)
                .ignoreElements().subscribe(this::startGame);
    }

    private void startGame() {
        Gdx.app.log("Game", "Started");

        Observable<Contact> gameOverEvent = collisionStream.filter(contact ->
                (contact.getFixtureA().getBody() == ball.getBody() && contact.getFixtureB().getBody() == bottom.getBody())
                        || (contact.getFixtureA().getBody() == bottom.getBody() && contact.getFixtureB().getBody() == ball.getBody())
        ).filter(item -> false);

        gameOverEvent.ignoreElements().subscribe(this::endGame);
        Observable
                .interval(20, TimeUnit.MILLISECONDS)
                .takeUntil(gameOverEvent)
                .subscribe(count -> {
                    world.step(0.02f, 6, 2);
                    Gdx.graphics.requestRendering();
                });

        Observable
                .interval(5, TimeUnit.SECONDS)
                .takeUntil(gameOverEvent)
                .subscribe(time -> ball.scaleVelocity(1.1f));
        Observable
                .interval(1, TimeUnit.SECONDS)
                .takeUntil(gameOverEvent)
                .subscribe(time -> score++);
        inputStream
                .takeUntil(gameOverEvent)
                .groupBy(event -> event.pointer)
                .subscribe(eventstream -> {
                    Observable<InputObservable.InputEvent> sharedStream = eventstream.share();
                    processTouchEvent(sharedStream, bottom.getBody(), boxA, boxB);
                });
    }

    /**
     * End game logic goes here
     */
    private void endGame() {

    }

    private void processTouchEvent(Observable<InputObservable.InputEvent> inputStream, Body ground, Box boxA, Box boxB) {

        inputStream
                .filter(event -> event.type == InputObservable.EventType.DOWN)
                .flatMap(event -> {
                    if (boxA.getBoundingBox().contains(event.x, event.y))
                        return Observable.just(Pair.of(event, boxA));
                    else if (boxB.getBoundingBox().contains(event.x, event.y))
                        return Observable.just(Pair.of(event, boxB));
                    return Observable.empty();
                })
                .subscribe(pair -> {
                    InputObservable.InputEvent event = pair.getLeft();
                    Box box = pair.getRight();
                    MouseJoint joint = createMouseJoint(world, event.x, box.getPosition().y, ground, box.getBody());
                    Gdx.app.log("Object Touched", event.toString());
                    inputStream
                            .takeWhile(event1 -> event1.type != InputObservable.EventType.UP)
                            .subscribe(ev ->
                                            joint.setTarget(new Vector2(ev.x, box.getPosition().y)),
                                    error -> {
                                    },
                                    () -> {
                                        world.destroyJoint(joint);
                                    });
                });
    }


    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        box2DRenderer.render(world, myWorldcamera.combined);

        ball.render(shapeRenderer);
        boxA.render(shapeRenderer);
        boxB.render(shapeRenderer);
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

    private void createViewPort() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        SCREEN_WIDTH = (int) h;
        SCREEN_HEIGHT = (int) w;
        WORLD_WIDTH = 10;
        SCALE_FACTOR = SCREEN_WIDTH / WORLD_WIDTH;
        WORLD_HEIGHT = SCREEN_HEIGHT / SCALE_FACTOR;

        camera = Utility.setupCamera(w, h);
        myWorldcamera = Utility.setupCamera(WORLD_HEIGHT, WORLD_WIDTH);
    }

    private void createRenderers(Camera camera) {
        shapeRenderer = new ShapeRenderer();
        box2DRenderer = new Box2DDebugRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);

        //for drawing score on screen
        font = new BitmapFont();
        font.setColor(Color.BLUE);
        font.getData().setScale(5);
        batch = new SpriteBatch();
        batch.setProjectionMatrix(camera.combined);
    }

    private void createObjects(World world) {
        ball = new Circle(world, new Vector2(WORLD_WIDTH / 2, WORLD_HEIGHT / 2), 0.5f, BodyDef.BodyType.DynamicBody, SCALE_FACTOR);
        ball.setLinearVelocity(new Vector2(4, 4));
        boxA = new Box(world, new Vector2(WORLD_WIDTH / 2, 0.5f), new Vector2(4, 1.5f), BodyDef.BodyType.DynamicBody, SCALE_FACTOR);
        boxB = new Box(world, new Vector2(WORLD_WIDTH / 2, WORLD_HEIGHT - 0.5f), new Vector2(4, 1.5f), BodyDef.BodyType.DynamicBody, SCALE_FACTOR);

        left = new Box(world, new Vector2(0, WORLD_HEIGHT / 2), new Vector2(0.5f, WORLD_HEIGHT), BodyDef.BodyType.StaticBody, SCALE_FACTOR);
        right = new Box(world, new Vector2(WORLD_WIDTH, WORLD_HEIGHT / 2), new Vector2(0.5f, WORLD_HEIGHT), BodyDef.BodyType.StaticBody, SCALE_FACTOR);
        top = new Box(world, new Vector2(WORLD_WIDTH / 2, WORLD_HEIGHT), new Vector2(WORLD_WIDTH, 0.5f), BodyDef.BodyType.StaticBody, SCALE_FACTOR);
        bottom = new Box(world, new Vector2(WORLD_WIDTH / 2, 0), new Vector2(WORLD_WIDTH, 0.5f), BodyDef.BodyType.StaticBody, SCALE_FACTOR);
    }

    private MouseJoint createMouseJoint(World world, float x, float y, Body bodyA, Body bodyB) {
        MouseJointDef def = new MouseJointDef();
        def.bodyA = bodyA;
        def.bodyB = bodyB;
        def.target.set(x, y);
        def.maxForce = 90000.0f;
        return (MouseJoint) world.createJoint(def);
    }

    public static class CollisionDetector implements ContactListener {

        private PublishSubject<Contact> eventStream = PublishSubject.create();

        @Override
        public void beginContact(Contact contact) {
            eventStream.onNext(contact);
        }

        public PublishSubject<Contact> getEventStream() {
            return eventStream;
        }

        @Override
        public void endContact(Contact contact) {
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {

        }
    }
}
