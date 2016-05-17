package rx.playground;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] s) {

        Observer<String> stringObserver = PrintObserver.create();
        Observable.just("hogehoge", "mogemoge")
                .subscribe(stringObserver);

        Observable.just("test")
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.println(s);
                    }
                })
                .subscribe();

        Observable.range(0, 4)
                .take(2) // 0,1 が2回
                .repeat(2)
//                .take(2) // 0,1 だけ
                .subscribe(PrintObserver.create());

        Observable.interval(1, TimeUnit.SECONDS)
                .take(5)
                .subscribe(PrintObserver.create());

        Observable.range(0, 3)
                .publish()
                .defer(() -> Observable.range(2, 3))
                .subscribe(PrintObserver.create());

        Observable
                .create(new Observable.OnSubscribe<Integer>() {
                    @Override
                    public void call(Subscriber<? super Integer> subscriber) {
                        System.out.println("integer call");
                        subscriber.onNext(1);
                        subscriber.onNext(2);
                        subscriber.onCompleted();
                    }
                })
                .doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        System.out.println("doOnNext : " + integer);
                    }
                })
                .flatMap(new Func1<Integer, Observable<?>>() { // return merge(map(func));
                    @Override
                    public Observable<?> call(Integer integer) {
                        return Observable.create(new Observable.OnSubscribe<String>() {
                            @Override
                            public void call(Subscriber<? super String> subscriber) {
                                System.out.println("string flatMap call");
                                subscriber.onNext("toString : " + integer.toString());
                                subscriber.onCompleted();
                            }
                        });
                    }
                })
                .subscribe(PrintObserver.create());

        Observable.merge(Observable.just(100), Observable.range(0, 3), Observable.just(200))
                .subscribe(PrintObserver.create());

        Observable.range(0, 10)
                .skip(5)
                .take(2)
//                .limit(2) take(count)を内部で読んでるだけなので同じ
                .subscribe(PrintObserver.create());

        Observable.range(0, 5)
                .doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        // 0 - 4の値は来る
                        System.out.println(integer);
                    }
                })
                .skip(2)
                .subscribe(PrintObserver.create());  // 2 - 4の値が来る
    }
}
