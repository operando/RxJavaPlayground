package rx.playground;

import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;

import java.net.SocketException;

/**
 * Error関連の練習
 */
public class Error {

    public static void run() {
        System.out.println("============Error============");

        Observable
                .create((Observable.OnSubscribe<String>) subscriber -> {
                    subscriber.onNext("test");
                    subscriber.onError(new Exception());
                })
                .onErrorReturn(throwable -> "error")
                .subscribe(PrintObserver.create());

        Observable
                .create((Observable.OnSubscribe<String>) subscriber -> {
                    subscriber.onNext("test");
                    subscriber.onError(new Exception());
                })
                .map(String::toUpperCase)
                .onErrorReturn(throwable -> "error")
                .subscribe(PrintObserver.create());

        Observable
                .create((Observable.OnSubscribe<Integer>) subscriber -> {
                    subscriber.onError(new Exception());
                })
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends Integer>>() {
                    @Override
                    public Observable<? extends Integer> call(Throwable throwable) {
                        return Observable.just(-1);
                    }
                })
                .subscribe(PrintObserver.create());

        Observable
                .create((Observable.OnSubscribe<String>) subscriber -> {
                    System.out.println("Retry Test");
                    subscriber.onNext("test");
                    subscriber.onError(new Exception());
                })
                .retry(2)
                .onErrorReturn(throwable -> "error")
//                .retry(2) // こっちに書くと効果はない
                .subscribe(PrintObserver.create());

        Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        subscriber.onError(new Exception());
                    }
                })
                .retry((integer, throwable) -> {
                    System.out.println("retry count : " + integer);
                    return integer < 3;
                })
                .subscribe(PrintObserver.create());

        Single
                .create(new Single.OnSubscribe<String>() {
                    @Override
                    public void call(SingleSubscriber<? super String> singleSubscriber) {
                        singleSubscriber.onError(new Exception());
                    }
                })
                .retry((integer, throwable) -> {
                    System.out.println("single retry count : " + integer);
                    return integer < 3;
                })
                .subscribe(new SingleSubscriber<String>() {
                    @Override
                    public void onSuccess(String value) {
                        System.out.println("single value : " + value);
                    }

                    @Override
                    public void onError(Throwable error) {
                        System.out.println("single error : " + error);
                    }
                });

        Observable
                .create(new Observable.OnSubscribe<String>() {
                    int count;

                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        System.out.println(count);
//                        if (count > 2) {
                        if (count > 1) {
                            subscriber.onError(new SocketException());
                        } else {
                            subscriber.onError(new Exception());
                        }
                        count++;
                    }
                })
                .retry(new Func2<Integer, Throwable, Boolean>() {
                    @Override
                    public Boolean call(Integer count, Throwable throwable) {
                        // SocketExceptionが起きたらError
                        if (throwable instanceof SocketException) {
                            return false;
                        }
                        // ２回まではリトライする
                        return count < 3;
                    }
                })
                .subscribe(PrintObserver.create());

        Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        subscriber.onNext("test");
                        subscriber.onError(new SocketException("exception"));
                    }
                })
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends String>>() {
                    @Override
                    public Observable<? extends String> call(Throwable throwable) {
                        // なんか特定の条件だったら値を流す.そうでないならそのままexceptionを流す
                        if (throwable instanceof SocketException) {
                            return Observable.just("onErrorResumeNext");
                        }
                        return Observable.error(throwable);
                    }
                })
                .subscribe(PrintObserver.create());

        System.out.println("============Error============");
    }
}
