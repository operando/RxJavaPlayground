package rx.playground;

import rx.Observable;
import rx.Subscription;
import rx.subjects.AsyncSubject;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

public class SubjectSample {


    public static void run() {
        System.out.println("============Subject============");

        AsyncSubject<String> asyncSubject = AsyncSubject.create();
        asyncSubject.subscribe(PrintObserver.create());

        asyncSubject.onNext("test");
        asyncSubject.onNext("test2");// onCompleted直前の値が流れる
        asyncSubject.onCompleted();// onCompletedを読んだら流れる
        asyncSubject.onNext("test3");// 呼ばれない

        AsyncSubject<String> asyncSubject2 = AsyncSubject.create();
        asyncSubject2.subscribe(PrintObserver.create());

        asyncSubject2.onNext("test");
        asyncSubject2.onError(new Exception());// onErrorが呼ばれたらnextに値は流れない


        BehaviorSubject<String> stringBehaviorSubject = BehaviorSubject.create("");

        stringBehaviorSubject.onNext("test1");// 直前の値だけなのでこれは流れない
        stringBehaviorSubject.onNext("test2");// 直前の値だけなのでこれは流れる
        stringBehaviorSubject.subscribe(PrintObserver.create());
        stringBehaviorSubject.onNext("test3");
        stringBehaviorSubject.onCompleted();


        PublishSubject<String> stringPublishSubject = PublishSubject.create();

        stringPublishSubject.onNext("test1");
        stringPublishSubject.subscribe(PrintObserver.create());
        stringPublishSubject.onNext("test2");
        stringPublishSubject.onNext("test3");
        stringPublishSubject.onCompleted();
        stringPublishSubject.onNext("test3");


        ReplaySubject<String> stringReplaySubject = ReplaySubject.create();

        stringReplaySubject.onNext("test1");
        stringReplaySubject.onNext("test2");
        stringReplaySubject.subscribe(PrintObserver.create());// nextした値が全て流れてくる
        stringReplaySubject.onNext("test3");
        stringReplaySubject.onCompleted();
        stringReplaySubject.subscribe(PrintObserver.create());

        PublishSubject<Void> voidPublishSubject = PublishSubject.create();
        Subscription subscription;

        boolean isOverM = true;
        if (isOverM) {
            subscription = voidPublishSubject
                    .flatMap(value -> {
                        return getBitmapFromUrl();
                    })
                    .subscribe(
                            s -> {
                                a();
                            },
                            throwable -> {
                                b();
                            });
        } else {
            subscription = voidPublishSubject
                    .subscribe(
                            s -> {
                                c();
                            });
        }

        System.out.println("============Subject============");
    }

    private static Observable<String> getBitmapFromUrl() {
        return Observable.just("");
    }

    private static void a() {

    }

    private static void b() {

    }

    private static void c() {

    }
}
