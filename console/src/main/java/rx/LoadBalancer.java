package rx;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import org.apache.commons.lang3.tuple.Pair;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LoadBalancer {

    public static void main(String args[]) {

        PublishSubject<Pair<Request, ObservableEmitter>> requestStream = PublishSubject.create();

        requestStream
                .doOnNext(pair -> printThread("Request Received [" + pair.getLeft().ip + "]"))
                .toFlowable(BackpressureStrategy.BUFFER)
                .parallel()
                .runOn(Schedulers.single())
                .map(pair ->
                        Pair.of(heavyOperation(pair.getLeft()),
                                pair.getRight()))
                .map(LoadBalancer::DbOperation)
                .sequential()
                .subscribe(pair -> {
                    Response response = pair.getLeft();
                    ObservableEmitter<Response> channel = pair.getRight();
                    channel.onNext(response);
                    channel.onComplete();
                });
        //*/
        createLoad(requestStream);
        requestStream.onComplete();

        //Blocking call
        Observable.never().blockingFirst();
    }

    public static void createLoad(PublishSubject<Pair<Request, ObservableEmitter>> inputStream) {

        Stream<String> randomIpStream =
                IntStream
                        .range(1, 5)
                        .mapToObj(item -> "10.10.10.11:" + (int) (Math.random() * 1000));

        randomIpStream.forEach(ip -> {
            Observable<Response> observable = Observable
                    .create(channel -> {
                        inputStream.onNext(Pair.of(new Request(ip), channel));
                    });

            observable.subscribe(response -> {
                printThread("Response Sent for [" + response.ip + "]");
            });
        });
    }

    public static Response heavyOperation(Request request) {

        try {
            printThread("Performing Heavy Operation [" + request.ip + "]");
            int time = (int) (Math.random() * 4000 + 2000);
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new Response("[" + "data" + "]", request.ip);
    }

    public static class Request {
        String ip;

        Request(String ip) {
            this.ip = ip;
        }
    }

    public static class Response {
        String data;
        String ip;

        Response(String data, String ip) {
            this.data = data;
            this.ip = ip;
        }
    }

    private static Pair<Response, ObservableEmitter> DbOperation(Pair<Response, ObservableEmitter> object) {
        try {
            printThread("Performing DB Operation [" + object.getLeft().ip + "]");
            int time = (int) (Math.random() * 4000 + 2000);
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return object;
    }

    public static void printThread(String message) {

        System.out.println("[" + Thread.currentThread().getName() + "," + java.time.LocalTime.now() + "]" +
                " message: " + message);
    }
}
