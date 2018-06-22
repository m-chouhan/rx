package com.pong;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by mahendras on 19/11/17.
 */

public class InputObservable extends InputAdapter {

    private final PublishSubject<InputEvent> publisher;

    private InputObservable(PublishSubject<InputEvent> publishSubject) {
        this.publisher = publishSubject;
    }

    enum EventType {UP, DOWN, DRAGGED}

    static class InputEvent {
        public float x, y;
        public final int pointer;
        public final EventType type;

        public InputEvent(float x, float y, int pointer, EventType type) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.pointer = pointer;
        }

        //required for distinct until changed to work
        @Override
        public boolean equals(Object o) {
            if (o instanceof InputEvent) {
                InputEvent event = (InputEvent) o;
                return this.x == event.x && this.y == event.y
                        && this.type == event.type && this.pointer == event.pointer;
            }
            return super.equals(o);
        }

        @Override
        public String toString() {
            return "[" + x + ":" + y + ":" + pointer + "]  " + type.toString();
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        publisher.onNext(new InputEvent(screenX, screenY, pointer, EventType.DOWN));
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        publisher.onNext(new InputEvent(screenX, screenY, pointer, EventType.UP));
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        publisher.onNext(new InputEvent(screenX, screenY, pointer, EventType.DRAGGED));
        return false;
    }

    /**
     * @return Observable emitting absolute screen coordinates
     */
    static PublishSubject<InputEvent> createRaw() {

        PublishSubject<InputEvent> source = PublishSubject.create();
        Gdx.input.setInputProcessor(new InputObservable(source));
        return source;
    }

    /**
     * @param camera : camera object
     * @return an Observable which emits value based on camera world coordinates
     */
    static Observable<InputEvent> create(Camera camera) {
        return createRaw()
                .map(inputEvent -> {
                    Vector3 vector3 = camera.unproject(new Vector3(inputEvent.x, inputEvent.y, 0));
                    inputEvent.x = vector3.x;
                    inputEvent.y = vector3.y;
                    return inputEvent;
                })
                .share();
    }
}
