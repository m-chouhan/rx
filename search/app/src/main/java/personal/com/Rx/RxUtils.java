package personal.com.Rx;

import android.support.v4.util.Pair;

import io.reactivex.ObservableTransformer;

/**
 * Created by mahendra.chouhan on 8/1/18.
 */

public class RxUtils {

    public static ObservableTransformer<Float, Float> avg = floatObservable -> floatObservable
            .scan(Pair.create(0f, 1),
                    (pair, value) -> Pair.create(pair.first + value, pair.second + 1))
            .map(pair -> pair.first / pair.second);

}
