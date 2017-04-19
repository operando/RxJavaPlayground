package rx.playground.rxjava1;

import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.BackpressureStrategy;
import rx.Observable;

public class RxJava2InteropSample {

    public static void run() {
        io.reactivex.Observable o2 = RxJavaInterop.toV2Observable(Observable.just("RxJava 1"));
        o2.subscribe(System.out::println);
        rx.Observable o1 = RxJavaInterop.toV1Observable(io.reactivex.Observable.just("RxJava 2"), BackpressureStrategy.BUFFER);
        o1.subscribe(System.out::println);
    }
}