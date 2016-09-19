package rx.playground;

import rx.Notification;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;

/**
 * Do
 * http://reactivex.io/documentation/operators/do.html
 */
public class Do {

    public static void run() {
        System.out.println("============Do============");

        Observable.range(0, 3)
                .doOnEach(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        System.out.println("doOnEach : " + integer);
                    }
                })
                .subscribe(PrintObserver.create());

        Observable.range(0, 3)
                .doOnEach(new Action1<Notification<? super Integer>>() {
                    @Override
                    public void call(Notification<? super Integer> notification) {
                        System.out.println("doOnEach : " + notification.getKind().toString());
                    }
                })
                .subscribe(PrintObserver.create());

        Observable.range(0, 3)
                .map(integer -> integer / 0)
                .doOnError(throwable -> System.out.println("doOnError : " + throwable.toString()))
                .subscribe(PrintObserver.create());

        Observable.range(0, 3)
                .doOnRequest(aLong -> System.out.println("doOnRequest : " + aLong))
                .subscribe(PrintObserver.create());

        Observable.range(0, 3)
                .doOnUnsubscribe(() -> System.out.println("doOnUnsubscribeg"))
                .subscribe(PrintObserver.create());

        Observable.range(0, 3)
                .doAfterTerminate(() -> System.out.println("doAfterTerminate"))
                .subscribe(PrintObserver.create());

        Observable.range(0, 3)
                .doOnSubscribe(() -> System.out.println("doOnSubscribe"))
                .doOnNext(integer -> System.out.println("doOnNext : " + integer))
                .doOnTerminate(() -> System.out.println("doOnTerminate"))
                .doOnCompleted(() -> System.out.println("doOnCompleted"))
                .doAfterTerminate(() -> System.out.println("doAfterTerminate"))
                .subscribe(PrintObserver.create());

        System.out.println("============Do============");
    }
}