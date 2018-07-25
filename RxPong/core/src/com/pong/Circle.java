package com.pong;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Created by mahendra.chouhan on 6/22/18.
 */

public class Circle {

    private Body body;
    private Fixture fixture;
    private CircleShape shape;
    private float radius;
    private float scaleFactor;

    Circle(World world, Vector2 position, float radius, BodyDef.BodyType bodyType, float scaleFactor) {

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(position);
        body = world.createBody(bodyDef);
        this.scaleFactor = scaleFactor;
        CircleShape shape = new CircleShape();
        shape.setRadius(radius);
        FixtureDef defination = new FixtureDef();
        defination.shape = shape;
        defination.density = 0.1f;
        defination.friction = 0;
        defination.restitution = 1f;
        body.createFixture(defination);
        shape.dispose();

        fixture = body.getFixtureList().get(0);
        this.shape = (CircleShape) fixture.getShape();
        this.radius = radius;
    }

    Vector2 getPosition() {
        return body.getPosition();
    }

    void setLinearVelocity(Vector2 velocity) {
        body.setLinearVelocity(velocity);
    }

    float getRadius() {
        return radius;
    }

    Body getBody() {
        return body;
    }

    void render(ShapeRenderer renderer) {
        Vector2 position = getPosition();
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.circle(position.x * scaleFactor, position.y * scaleFactor, (radius - 0.2f) * scaleFactor);
        renderer.end();
    }

    public void scaleVelocity(float scl) {
        body.setLinearVelocity(body.getLinearVelocity().scl(scl));
    }
}
