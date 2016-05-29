package rx.playground;

import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Func1;

public class SingleSample {

    public static void run() {
        System.out.println("============Single============");

        Single
                .create((Single.OnSubscribe<String>) singleSubscriber -> {
                    singleSubscriber.onSuccess("test");
                    singleSubscriber.onSuccess("test");
                })
                .subscribe(new SingleSubscriber<String>() {
                    @Override
                    public void onSuccess(String value) {
                        System.out.println(value);
                    }

                    @Override
                    public void onError(Throwable error) {
                        System.out.println(error);
                    }
                });

        Single
                .create((Single.OnSubscribe<String>) singleSubscriber -> {
                    singleSubscriber.onSuccess("test");
                    singleSubscriber.onSuccess("test");
                })
                .toObservable()
                .subscribe(PrintObserver.create());

        Observable
                .range(0, 3)
                .single()
                .subscribe(PrintObserver.create());

        Single
                .concat(Single.just(3), Single.just(4))
                .subscribe(PrintObserver.create());

        Single
                .just(5)
                .flatMapObservable(new Func1<Integer, Observable<String>>() {
                    @Override
                    public Observable<String> call(Integer integer) {
                        return Observable.create(new Observable.OnSubscribe<String>() {
                            @Override
                            public void call(Subscriber<? super String> subscriber) {
                                subscriber.onNext(Integer.toString(integer));
                                subscriber.onNext(Integer.toString(integer));
                            }
                        });
                    }
                })
                .subscribe(PrintObserver.create());

        System.out.println("============Single============");
    }
}
