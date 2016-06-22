package rx.playground;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class SubscribeOnTraining {

    public static void run() {
        System.out.println("============SubscribeOnTraining============");
        Observable.range(0, 2)
                .flatMap(new Func1<Integer, Observable<?>>() {
                    @Override
                    public Observable<?> call(Integer integer) {
                        return Observable
                                .create(new Observable.OnSubscribe<Object>() {
                                    @Override
                                    public void call(Subscriber<? super Object> subscriber) {
                                        System.out.println("Create - Thread : " + Thread.currentThread().getName());
                                        subscriber.onNext(1);
                                        subscriber.onCompleted();
                                    }
                                })
                                .doOnNext(integer1 -> System.out.println("doOnNext - Thread : " + Thread.currentThread().getName()));
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe(integer -> {
                    System.out.println("subscribe - Thread : " + Thread.currentThread().getName());
                });

        Observable observable = Observable.range(0, 1)
                .map(new Func1<Integer, Object>() {
                    @Override
                    public Object call(Integer integer) {
                        System.out.println("****Thread : " + Thread.currentThread().getName());
                        return integer;
                    }
                })
                .subscribeOn(Schedulers.newThread());

        PublishSubject publishSubject = PublishSubject.create();
        publishSubject
                .flatMap(new Func1<Object, Observable<?>>() {
                    @Override
                    public Observable<?> call(Object o) {
                        return Observable.create(new Observable.OnSubscribe<Object>() {
                            @Override
                            public void call(Subscriber<? super Object> subscriber) {
                                System.out.println("Create - Thread : " + Thread.currentThread().getName());
                                subscriber.onNext(1);
                                subscriber.onCompleted();
                            }
                        }).subscribeOn(Schedulers.newThread());
                    }
                })
                .subscribe(integer -> {
                    System.out.println("????Thread : " + Thread.currentThread().getName());
                });


        observable.
                subscribeOn(Schedulers.newThread())
                .subscribe(integer -> {
                    publishSubject.onNext(new Object());
                    System.out.println("**Thread : " + Thread.currentThread().getName());
                });
        System.out.println("============SubscribeOnTraining============");

    }
}
