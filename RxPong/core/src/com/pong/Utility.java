package com.pong;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class Utility {

    /**
     * Creates new Orthographic camera and changes the coordinates to portrait mode
     *
     * @param Width
     * @param Height
     * @return new Instance of Orthographic Camera
     */
    public static OrthographicCamera setupCamera(float Width, float Height) {
        OrthographicCamera DisplayCamera = new OrthographicCamera(Width, Height); //viewport dimensions
        DisplayCamera.position.set(Height / 2, Width / 2, 0);
        DisplayCamera.rotate(90);
        DisplayCamera.update();
        return DisplayCamera;
    }
}