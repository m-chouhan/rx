package rx;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String args[]) throws InterruptedException {

        //disposable.dispose();
        //trampoline();
        //zipExample();
        //flatMapExampe();
        //retryExample();
        //observableOfObservable();
        //parallel();
        //error();
        //hotNCold();
        groupBy();

        Observable.never().blockingFirst();
    }

    public static void createMethod() {
        ConnectableObservable<Object> newThreadObs = Observable
                .create(emitter -> {
                    emitter.onNext(1);
                    emitter.onNext(2);
                    emitter.onNext(3);
                    printThread("emission complete");
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.newThread())
                .publish();

        newThreadObs
                .subscribe(item ->
                {
                    Thread.sleep(10);
                    printThread("received " + item);
                });
        newThreadObs
                .subscribe(item ->
                {
                    Thread.sleep(90);
                    printThread("received " + item);
                });
    }

    public static void trampoline() {
        Scheduler scheduler = Schedulers.newThread();
        Scheduler.Worker worker = scheduler.createWorker();

        Runnable r1 = () -> {
            System.out.println("Start: r1");
            System.out.println("End: r1");
        };

        Runnable r3 = () -> {
            System.out.println("Start: r3");
            worker.schedule(r1);
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("End: r3");
        };


        Runnable r2 = () -> {
            System.out.println("Start: r2");
            //worker.schedule(r1);
            worker.schedule(r3);
            System.out.println("End: r2");
        };

        worker.schedule(r2);
    }

    static class Circle {
        double radius;
        String color;
    }

    public static void zipExample() {

        Observable<Double> radiusStream = Observable
                .range(1, 10)
                .map(item -> Math.random() * 10);
        Observable<String> observable2 = Observable.just("blue", "green", "red").repeat();

        Observable<Circle> observable = Observable.zip(radiusStream, observable2,
                (Double radius, String s) -> {
                    Circle zipObject = new Circle();
                    zipObject.color = s;
                    zipObject.radius = radius;
                    return zipObject;
                });
        observable
                .doOnComplete(() -> System.out.print("Completed"))
                .subscribe(circle ->
                        System.out.println(circle.color));
    }

    public static void flatMapExampe() {

        Observable
                .interval(4, TimeUnit.SECONDS)
                .flatMap(item ->
                        Observable
                                .interval(1, TimeUnit.SECONDS)
                                .map(it -> item))
                .subscribe(item -> printThread("" + item));
    }

    public static void retryExample() {

        Observable
                .range(0, 10)
                .map(item -> {
                    if (item % 2 == 0) return item;
                    throw new RuntimeException("Odd number found");
                })

                .retry(5)
                .subscribe(
                        item -> System.out.println(item),
                        error -> {
                            System.out.println("Error Found");
                        });
    }

    public static void observableOfObservable() {

        Observable
                .range(1, 5)
                //.zipWith(Observable.interval(2, TimeUnit.SECONDS), (first, second) -> first)
                .flatMap(item ->
                        Observable.just(item)
                                //.interval(1, TimeUnit.SECONDS)
                                //.observeOn(Schedulers.newThread())
                                .map(Main::heavyOperation))
                .subscribe(value -> printThread(value.toString()));
    }

    public static void parallel() {

        Flowable
                .range(1, 5)
                .parallel(10)
                .runOn(Schedulers.newThread())
                .map(Main::heavyOperation)
                .sequential()
                .subscribe(value -> printThread(value.toString()));
    }

    public static String heavyOperation(int item) {

        try {
            int time = (int) (Math.random() * 1000 + 100);
            Thread.sleep(time);
            printThread("Performed Heavy Operation");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "[" + item + "]";
    }
    /*
       newThreadObs.subscribe(new Observer<Object>() {
           @Override
           public void onSubscribe(Disposable d) {

           }

           @Override
           public void onNext(Object o) {

           }

           @Override
           public void onError(Throwable e) {

           }

           @Override
           public void onComplete() {

           }
       });
       */

    public static void error() {
        /*
        Observable.just("Hello!")
                .map(input -> {
                    throwException();
                    return 1;
                })
                .subscribe(
                        System.out::println,
                        error -> System.out.println("Error!")
                );
        */

        Observable.interval(1, TimeUnit.SECONDS)
                .map(input -> {
                    if (Math.random() < .5) {
                        throw new RuntimeException();
                    }
                    return "Success " + input;
                })
                .retry()
                .subscribe(System.out::println);
    }

    public static void groupBy() {

        Observable
                .range(1, 100)
                .groupBy(item -> item % 4)
                .flatMapSingle(item -> item.count())
                .subscribe(val -> {
                    printThread(" count" + val);
                });
    }

    public static void throwException() throws RuntimeException {
        throw new RuntimeException();
    }

    public static void hotNCold() throws InterruptedException {
        Observable<Long> cold = Observable.interval(1000, TimeUnit.MILLISECONDS);
        cold.subscribe(i -> System.out.println("First: " + i));
        Thread.sleep(1000);
        cold.subscribe(i -> System.out.println("Second: " + i));

        ConnectableObservable<Long> hot = Observable.interval(200, TimeUnit.MILLISECONDS).publish();
        hot.connect();

        hot.subscribe(i -> System.out.println("Hot First: " + i));
        Thread.sleep(500);
        hot.subscribe(i -> System.out.println("Hot Second: " + i));
    }

    public static void printThread(String message) {

        System.out.println("[" + Thread.currentThread().getName() + "] message: " + message);
    }
}
