package rx.playground;

import rx.Observer;

public class PrintObserver {

    public static <T> Observer<T> create() {
        return new Observer<T>() {
            @Override
            public void onCompleted() {
                System.out.println("onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("onError : " + e);
            }

            @Override
            public void onNext(T t) {
                System.out.println("onNext : " + t);
            }
        };
    }

    public static <T> Observer<T> create(String tag) {
        return new Observer<T>() {
            @Override
            public void onCompleted() {
                System.out.println(tag + " : onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println(tag + " : onError : " + e);
            }

            @Override
            public void onNext(T t) {
                System.out.println(tag + " : onNext : " + t);
            }
        };
    }
}
