package com.maximus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import personal.com.Rx.R;

public class SearchActivity extends AppCompatActivity {

    public static final String TAG = "SearchActivity";
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = findViewById(R.id.message);
        Button sendRequest = findViewById(R.id.sendRequest);
        Button cancelRequest = findViewById(R.id.cancelRequest);
        Button searchButton = findViewById(R.id.search);
        TextView resultView = findViewById(R.id.results);

        Observable<String> editText$ = RxTextView
                .textChanges(mEditText)
                .skipInitialValue()
                .map(CharSequence::toString);

        Observable<String> searchButton$ = RxView
                .clicks(searchButton)
                .map(item -> mEditText.getText().toString());
        Observable<Integer> cancelRequest$ = RxView
                .clicks(cancelRequest)
                .scan(0, (counter, string) -> ++counter)
                .skip(1).replay(1);

        Observable<String> search$ = searchButton$
                .mergeWith(editText$)
                .distinctUntilChanged()
                .debounce(2, TimeUnit.SECONDS)
                .doOnNext(text -> {
                    Log.i(TAG, "Searching [" + text + "]");
                });

        search$
                .switchMap(
                        text -> fetchList(text)
                                .takeUntil(cancelRequest$)
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnComplete(() -> toast("Completed [" + text + "]"))
                                .doOnDispose(() -> toast("Dispose [" + text + "]"))
                                .subscribeOn(AndroidSchedulers.mainThread())
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    Log.i(TAG, "Fetched:" + list.toString());
                    resultView.setText("");
                    list.forEach(item -> resultView.append(item + "\n"));
                });

        cancelRequest$
                .subscribe(item -> Log.i(TAG, "Request Cancelled [" + item + "]"));

        Observable<Integer> sendRequest$ = RxView
                .clicks(sendRequest)
                .debounce(1, TimeUnit.SECONDS)
                .scan(0, (counter, string) -> ++counter);
    }

    public Observable<List<String>> fetchList(String text) {
        return Observable
                .just("One", "Two", "Three", "Four")
                .map(item -> text + item)
                .toList()
                .toObservable()
                .delay(2, TimeUnit.SECONDS);
        //.blockingGet();
    }

    public void toast(String message) {
        Log.i(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
