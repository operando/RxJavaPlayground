package rx.playground;

import rx.Observable;
import rx.functions.Func1;

import java.util.concurrent.TimeUnit;

/**
 * Repeat
 * http://reactivex.io/documentation/operators/repeat.html
 */
public class Repeat {

    public static void run() {
        System.out.println("============Repeat============");
        Observable.range(0, 3)
                .repeat(2)
                .subscribe(PrintObserver.create());

        // Poll for data periodically using repeatWhen + delay
//        Observable.range(0, 3)
//                .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
//                    @Override
//                    public Observable<?> call(Observable<? extends Void> observable) {
//                        return observable.delay(5, TimeUnit.SECONDS);
//                    }
//                })
//                .subscribe(PrintObserver.create());

        System.out.println("============Repeat============");
    }
}
