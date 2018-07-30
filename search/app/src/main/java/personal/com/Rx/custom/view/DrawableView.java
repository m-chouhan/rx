package personal.com.Rx.custom.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;


/**
 * Created by mahendra.chouhan on 7/27/18.
 */

public class DrawableView extends View {

    public static final String TAG = "DrawableView";

    private Paint paint;
    private ArrayList<Rect> objList = new ArrayList<>();

    public DrawableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawableView(Context context) {
        super(context);
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);

        objList.add(new Rect(0, 0, 100, 100));
        objList.add(new Rect(100, 100, 200, 200));

        ConnectableObservable<MotionEvent> input$ = RxView.touches(this).publish();
        input$.subscribe(
                m -> {
                    int pointerCount = m.getPointerCount();

                    for (int i = 0; i < pointerCount; i++) {
                        int x = (int) m.getX(i);
                        int y = (int) m.getY(i);
                        int id = m.getPointerId(i);
                        int action = m.getActionMasked();
                        int actionIndex = m.getActionIndex();
                        String actionString;

                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                actionString = "DOWN";
                                break;
                            case MotionEvent.ACTION_UP:
                                actionString = "UP";
                                break;
                            case MotionEvent.ACTION_POINTER_DOWN:
                                actionString = "PNTR DOWN";
                                break;
                            case MotionEvent.ACTION_POINTER_UP:
                                actionString = "PNTR UP";
                                break;
                            case MotionEvent.ACTION_MOVE:
                                actionString = "MOVE";
                                break;
                            default:
                                actionString = "";
                        }

                        String touchStatus = "Action: " + actionString + " Action Index: " + actionIndex + " ID: " + id + " X: " + x + " Y: " + y;
                        Log.i(TAG, touchStatus);
                    }
                }
        );

        Observable<MotionEvent> downEvent$ =
                input$
                        .filter(motionEvent ->
                                motionEvent.getAction() == MotionEvent.ACTION_DOWN ||
                                        motionEvent.getAction() == MotionEvent.ACTION_POINTER_DOWN);
        Observable<MotionEvent> upEvent$ =
                input$
                        .filter(motionEvent -> motionEvent.getAction() == MotionEvent.ACTION_UP ||
                                motionEvent.getAction() == MotionEvent.ACTION_POINTER_UP);
        /*
        input$
                .window(downEvent$, (item) -> upEvent$)
                .subscribe(
                        motionEvent$ -> processGesture(motionEvent$)
                );
        /**/
        /*
        input$
                .groupBy(motionEvent -> motionEvent.getPointerId(motionEvent.getActionIndex()))
                .map(stream$ -> stream$.publish())
                .subscribe(splitInput$ -> {
                    Observable<MotionEvent> down$ =
                            splitInput$
                                    .filter(motionEvent ->
                                            motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN ||
                                                    motionEvent.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN);
                    Observable<MotionEvent> up$ =
                            splitInput$
                                    .filter(motionEvent -> motionEvent.getActionMasked() == MotionEvent.ACTION_UP ||
                                            motionEvent.getActionMasked() == MotionEvent.ACTION_POINTER_UP);
                    splitInput$
                            .window(down$, (item) -> up$)
                            .subscribe(motionEvent$ -> processGesture(motionEvent$));
                    splitInput$.connect();
                });
        /**/
        input$.connect();
    }

    private void processGesture(Observable<MotionEvent> motionEvent$) {

        AtomicReference<Rect> rect = new AtomicReference<>();

        motionEvent$
//                .skipWhile(motionEvent -> {
//                    if (motionEvent.getAction() != MotionEvent.ACTION_DOWN) return true;
//                    for (Rect r : objList) {
//                        if (r.contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
//                            rect.set(r);
//                            return false;
//                        }
//                    }
//                    return true;
//                })
                .subscribe(new Observer<MotionEvent>() {

                    Point initial = new Point();
                    Rect rectangle;

                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.i(TAG, "onSubscribe");
                    }

                    @Override
                    public void onNext(MotionEvent motionEvent) {

                        Log.i(TAG, "onNext" + motionEvent.toString());
                        int x = (int) motionEvent.getX();
                        int y = (int) motionEvent.getY();
                        switch (motionEvent.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN:
                            case MotionEvent.ACTION_POINTER_DOWN:
                                for (Rect r : objList) {
                                    if (r.contains(x, y)) {
                                        Log.i(TAG, "Selected " + r.toString());
                                        rectangle = r;
                                        initial.set(x, y);
                                    }
                                }
                                break;
                            case MotionEvent.ACTION_MOVE:
                                if (rectangle != null) rectangle.offsetTo(x, y);
                                break;

                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_POINTER_UP:
                                if (rectangle != null) rectangle.offsetTo(initial.x, initial.y);
                                break;
                        }
                        postInvalidate();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "onError");
                    }

                    @Override
                    public void onComplete() {
                        Log.i(TAG, "onComplete");
                    }
                });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Rect rect : objList)
            canvas.drawRect(rect, paint);
    }
}
