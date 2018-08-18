package com.maximus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

import static android.view.MotionEvent.ACTION_DOWN;
import static com.maximus.TouchInput.EventType.DOUBLE_TAP;
import static com.maximus.TouchInput.EventType.DOWN;
import static com.maximus.TouchInput.EventType.MOVE;
import static com.maximus.TouchInput.EventType.SWIPE_DOWN;
import static com.maximus.TouchInput.EventType.SWIPE_UP;
import static com.maximus.TouchInput.EventType.UP;


/**
 * Created by mahendra.chouhan on 7/27/18.
 * A Sample on how to handle multitouch using RxJava on Android
 * Article link
 * https://medium.com/mindorks/making-sense-of-multitouch-with-rxjava-8d035e56f239
 */

public class DrawableView extends View {

    public static final String TAG = "DrawableView";

    //List of rectangles and their colors to be displayed on screen
    private ArrayList<Pair<Rect, Paint>> objList = new ArrayList<>();
    //stream that emits touch gestures : SWIPE_UP | SWIPE_DOWN | DOUBLE_TAP
    private PublishSubject<TouchInput> gestures$;

    public DrawableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        /* Object initialization */
        for (int i = 100; i <= 500; i += 100) {
            objList.add(
                    Pair.create(new Rect(i, i, i + 100, i + 100),
                            new Paint(Color.RED))
            );
        }
        gestures$ = PublishSubject.create();
        setupPipelines();
    }

    public DrawableView(Context context) {
        super(context);
    }

    /**
     * MotionEvent object contains information about all the
     * currently active pointers, which makes it harder to detect
     * which pointer is moving in currently, so we transform the
     * stream into a more understandable format where each item
     * corresponds to a event
     *
     * @param input$ : <MotionEvent> stream
     * @return <TouchInput> stream
     */
    private PublishSubject<TouchInput> transform(Observable<MotionEvent> input$) {

        PublishSubject<TouchInput> transformed$ = PublishSubject.create();
        input$
                .subscribe((current) -> {
                    int count = current.getPointerCount();
                    switch (current.getActionMasked()) {
                        case ACTION_DOWN:
                        case MotionEvent.ACTION_POINTER_DOWN: {
                            int index = current.getActionIndex();
                            int id = current.getPointerId(index);
                            long time = current.getEventTime();
                            transformed$.onNext(new TouchInput(current.getX(index),
                                    current.getY(index), id, DOWN, time));
                            break;
                        }
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_POINTER_UP: {
                            int index = current.getActionIndex();
                            int id = current.getPointerId(index);
                            long time = current.getEventTime();
                            transformed$
                                    .onNext(new TouchInput(current.getX(index),
                                            current.getY(index),
                                            id, UP, time));
                            break;
                        }
                        case MotionEvent.ACTION_MOVE:
                            for (int actionId = 0; actionId < count; ++actionId) {
                                float x = current.getX(actionId);
                                float y = current.getY(actionId);
                                int id = current.getPointerId(actionId);
                                long time = current.getEventTime();
                                transformed$.onNext(new TouchInput(x, y, id, MOVE, time));
                            }
                            break;
                    }
                });

        return transformed$;
    }

    private void setupPipelines() {

        PublishSubject<TouchInput> transformed$ = transform(RxView.touches(this));

        Observable<Observable<TouchInput>> pointers$ =
                transformed$
                        .groupBy(event -> event.pointer)
                        //.take(3)  //limits max supported fingers
                        .map(Observable::share)
                        .share();

        Observable<List<TouchInput>> doubleTap$ =
                pointers$
                        .flatMap(pointer$ ->
                                pointer$.filter(event -> event.type == DOWN)
                                        .buffer(800, TimeUnit.MILLISECONDS)
                                        .filter(items -> items.size() == 2));
        doubleTap$
                .subscribe(list -> {
                    TouchInput touchInput = list.get(1);
                    touchInput.type = DOUBLE_TAP;
                    gestures$.onNext(touchInput);
                });

        Observable<Observable<TouchInput>> touchEvent$ =
                pointers$
                        .flatMap(pointer$ -> {
                                    Observable<TouchInput> down$ = pointer$.filter(event -> event.type == DOWN);
                                    Observable<TouchInput> up$ = pointer$.filter(event -> event.type == UP);
                                    return pointer$
                                            .distinctUntilChanged()
                                            .window(down$, (item) -> up$)
                                            .map(Observable::share);
                                }
                        ).share();

        touchEvent$
                .flatMapMaybe(touchInput$ ->
                        Maybe.zip(
                                touchInput$.lastElement(), //UP Event
                                touchInput$.firstElement(),//DOWN Event
                                (last, first) -> {
                                    float ydiff = last.y - first.y;
                                    float delta = last.time - first.time;
                                    float velocity = ydiff / delta;
                                    Log.i(TAG, "ydiff = " + ydiff + ", delta = " + delta + ", velocity = " + velocity);
                                    //reject if gesture took too long time to complete
                                    if (delta > 1000) return 0f;
                                    return velocity;
                                })
                )
                .subscribe(velocity -> {
                    if (velocity > 1.5f)
                        gestures$.onNext(new TouchInput(0, 0, 0, SWIPE_DOWN, 0));
                    else if (velocity < -1.5f)
                        gestures$.onNext(new TouchInput(0, 0, 0, SWIPE_UP, 0));
                });

        touchEvent$
                .subscribe(sub$ ->
                        sub$.subscribe(new TouchObserver())
                );

        gestures$
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        event -> {
                            switch (event.type) {
                                case SWIPE_UP:
                                    toast("SWIPE_UP : JUMP UP");
                                    break;
                                case SWIPE_DOWN:
                                    toast("SWIPE_DOWN : JUMP DOWN");
                                    break;
                                case DOUBLE_TAP:
                                    toast("DOUBLE_TAP : SHOOT");
                            }
                        }
                );
    }

    public void toast(String message) {
        Log.i(TAG, message);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles Touch Events and moves selected rectangles on screen
     */
    class TouchObserver implements Observer<TouchInput> {
        Point initial = new Point();
        Rect rectangle;
        Paint paint;

        @Override
        public void onSubscribe(Disposable d) {
            Log.i(TAG, "onStart[Move]");
        }

        @Override
        public void onNext(TouchInput event) {

            //Log.i(TAG, "onNext" + event.toString());
            int x = (int) event.x;
            int y = (int) event.y;

            switch (event.type) {
                case DOWN:
                    for (Pair<Rect, Paint> r : objList) {
                        if (r.first.contains(x, y)) {
                            Log.i(TAG, "Selected " + r.toString());
                            rectangle = r.first;
                            paint = r.second;
                            paint.setColor(Color.BLUE);
                            initial.set(rectangle.left, rectangle.top);
                        }
                    }
                    break;
                case MOVE:
                    if (rectangle != null) rectangle.offsetTo(x, y);
                    break;
            }
            postInvalidate();
        }

        @Override
        public void onError(Throwable e) {
            Log.i(TAG, "onError[Move]");
            e.printStackTrace();
        }

        @Override
        public void onComplete() {
            //reset rectangle back to its original position
            if (rectangle != null) {
                rectangle.offsetTo(initial.x, initial.y);
                paint.setColor(Color.GRAY);
            }
            Log.i(TAG, "onComplete[Move]");
            //reset color back to black after some time
            if (paint != null)
                Observable.just(paint)
                        .delay(2, TimeUnit.SECONDS)
                        .subscribe((paint) -> {
                            paint.setColor(Color.BLACK);
                            postInvalidate();
                        });
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Pair<Rect, Paint> pair : objList)
            canvas.drawRect(pair.first, pair.second);
    }
}
