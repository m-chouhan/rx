package com.pong;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Created by mahendra.chouhan on 6/22/18.
 */

public class Box {

    Body body;
    Rectangle rectangle;
    float scaleFactor;

    Box(World world, Vector2 position, Vector2 dimensions,
        BodyDef.BodyType bodyType, float scaleFactor) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(position);
        body = world.createBody(bodyDef);
        body.setGravityScale(0);
        body.setLinearDamping(10);
        body.setAngularDamping(100);
        rectangle = new Rectangle(position.x, position.y, dimensions.x, dimensions.y);
        this.scaleFactor = scaleFactor;
        //Assign various physical properties to our body
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(dimensions.x / 2, dimensions.y / 2);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 10f;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0f;
        body.createFixture(fixtureDef);
        shape.dispose();
    }

    Vector2 getPosition() {
        return body.getPosition();
    }

    Rectangle getBoundingBox() {
        return rectangle.setCenter(body.getPosition());
    }

    Body getBody() {
        return body;
    }

    void setLinearVelocity(Vector2 velocity) {
        body.setLinearVelocity(velocity);
    }

    void render(ShapeRenderer renderer) {
        Rectangle boundingBox = getBoundingBox();
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.rect(boundingBox.x * scaleFactor, boundingBox.y * scaleFactor,
                boundingBox.getWidth() * scaleFactor, boundingBox.getHeight() * scaleFactor);
        renderer.end();
    }

}
