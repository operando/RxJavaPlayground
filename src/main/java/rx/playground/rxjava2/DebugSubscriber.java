package rx.playground.rxjava2;

import io.reactivex.subscribers.ResourceSubscriber;

public class DebugSubscriber<T> extends ResourceSubscriber<T> {

    private String label;

    public DebugSubscriber() {
        super();
    }

    public DebugSubscriber(String label) {
        super();
        this.label = label;
    }

    @Override
    public void onNext(T data) {
        // onNextメソッドの呼び出し時に出力
        String threadName = Thread.currentThread().getName();
        if (label == null) {
            System.out.println(threadName + ": " + data);
        } else {
            System.out.println(threadName + ": " + label + ": " + data);
        }
    }

    ;

    @Override
    public void onError(Throwable throwable) {
        // onErrorメソッドの呼び出し時に出力
        String threadName = Thread.currentThread().getName();
        if (label == null) {
            System.out.println(threadName + ": エラー = " + throwable);
        } else {
            System.out.println(threadName + ": " + label + ": エラー = " + throwable);
        }
    }

    @Override
    public void onComplete() {
        // onCompleteメソッドの呼び出し時に出力
        String threadName = Thread.currentThread().getName();
        if (label == null) {
            System.out.println(threadName + ": 完了");
        } else {
            System.out.println(threadName + ": " + label + ": 完了");
        }
    }
}