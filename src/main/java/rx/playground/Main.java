package rx.playground;

import rx.playground.rxjava1.RxJava2InteropSample;
import rx.playground.rxjava2.RxJava2Main;

public class Main {

    public static void main(String[] s) throws InterruptedException {
//        RxJava1Main.main();
        RxJava2Main.main();
        RxJava2InteropSample.run();
    }
}
