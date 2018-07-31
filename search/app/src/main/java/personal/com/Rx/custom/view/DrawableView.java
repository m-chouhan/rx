package personal.com.Rx.custom.view;

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
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import personal.com.Rx.TouchInput;

import static personal.com.Rx.TouchInput.EventType.DOWN;
import static personal.com.Rx.TouchInput.EventType.MOVE;
import static personal.com.Rx.TouchInput.EventType.UP;


/**
 * Created by mahendra.chouhan on 7/27/18.
 */

public class DrawableView extends View {

    public static final String TAG = "DrawableView";

    private ArrayList<Pair<Rect, Paint>> objList = new ArrayList<>();

    public DrawableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawableView(Context context) {
        super(context);
    }

    private PublishSubject<TouchInput> transform(Observable<MotionEvent> input$) {

        PublishSubject<TouchInput> transformed$ = PublishSubject.create();
        input$
                .subscribe((current) -> {
                    int count = current.getPointerCount();
                    switch (current.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_POINTER_DOWN: {
                            int index = current.getActionIndex();
                            int id = current.getPointerId(index);
                            transformed$.onNext(new TouchInput(current.getX(index),
                                    current.getY(index), id, DOWN));
                            break;
                        }
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_POINTER_UP: {
                            int index = current.getActionIndex();
                            int id = current.getPointerId(index);
                            transformed$
                                    .onNext(new TouchInput(current.getX(index), current.getY(index), id, UP));
                            break;
                        }
                        case MotionEvent.ACTION_MOVE:
                            for (int actionId = 0; actionId < count; ++actionId) {
                                float x = current.getX(actionId);
                                float y = current.getY(actionId);
                                int id = current.getPointerId(actionId);
                                transformed$.onNext(new TouchInput(x, y, id, MOVE));
                            }
                            break;
                    }
                });

        return transformed$;
    }

    private void init() {
        /* Object initialization */
        for (int i = 0; i < 700; i += 150) {
            objList.add(
                    Pair.create(new Rect(i, i, i + 150, i + 150),
                            new Paint(Color.RED))
            );
        }

        PublishSubject<TouchInput> transformed$ = transform(RxView.touches(this));

        Observable<Observable<TouchInput>> pointers$ =
                transformed$
                        .groupBy(event -> event.pointer)
                        //.take(3)  //limits max supported fingers
                        .map(Observable::share)
                        .share();
        pointers$
                .subscribe(pointer$ ->
                        pointer$.filter(event -> event.type == DOWN)
                                .buffer(800, TimeUnit.MILLISECONDS)
                                .filter(items -> items.size() > 1)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe((item) -> toast("DOUBLE TAP PERFORMED")));

        Observable<Observable<TouchInput>> touchEvent$ =
                pointers$
                        .flatMap(split$ -> {
                                    Observable<TouchInput> down$ = split$.filter(event -> event.type == DOWN);
                                    Observable<TouchInput> up$ = split$.filter(event -> event.type == UP);
                                    return split$
                                            .distinctUntilChanged()
                                            //.doOnNext(touchInput -> Log.i(TAG, touchInput.toString()))
                                            .window(down$, (item) -> up$)
                                            .map(Observable::share);
                                }
                        ).share();

        touchEvent$.subscribe(this::processGesture);
    }

    private void processGesture(Observable<TouchInput> touchInput$) {

        touchInput$
                .buffer(2, 1)
                .filter(buffer -> buffer.size() > 1 && buffer.get(1).x - buffer.get(0).x > 0)
                .subscribe(
                        item -> Log.i(TAG, "UP")
                );

        touchInput$
                .subscribe(new Observer<TouchInput>() {
                    Point initial = new Point();
                    Rect rectangle;
                    Paint paint;

                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.i(TAG, "onSubscribe");
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
                        Log.i(TAG, "onError");
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        if (rectangle != null) {
                            rectangle.offsetTo(initial.x, initial.y);
                            paint.setColor(Color.GRAY);
                        }
                        Log.i(TAG, "onComplete");
                        Observable.just(1)
                                .delay(3, TimeUnit.SECONDS)
                                .ignoreElements()
                                .subscribe(() -> {
                                    paint.setColor(Color.BLACK);
                                    postInvalidate();
                                });
                    }
                });
    }

    public void toast(String message) {
        Log.i(TAG, message);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Pair<Rect, Paint> pair : objList)
            canvas.drawRect(pair.first, pair.second);
    }
}
