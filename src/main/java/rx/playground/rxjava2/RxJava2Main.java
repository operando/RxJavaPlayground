package rx.playground.rxjava2;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import io.reactivex.subscribers.ResourceSubscriber;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;

public class RxJava2Main {

    public static void main() throws InterruptedException {
        // あいさつの言葉を通知するFlowableの生成

        Flowable<String> flowable =
                Flowable.create(emitter -> {
                            // このFlowableEmitterは通知メソッド（onNextメソッド、onErrorメソッド、onCompleteメソッド）の内部で購読が解除されたか確認するようになっています。
                            // そのため、RxJava 1.xの通知メソッドと異なり、購読が解除されている状態でFlowableEmitterのonNextメソッドなどの通知メソッドを呼んでも、Subscriberには通知が行われないようになっています。

                            // subscribeメソッドがRxJava 1.xと異なりExceptionをthrowするようになっています。
                            // このことにより処理中に例外をcatchする必要がなくなり、発生した例外はFlowable内部の呼び出し元でcatchされ、致命的なエラーでない限りはSubscriberにエラーの通知を行うようになっています。

                            String[] datas = {"Hello, World!", "こんにちは、世界！"};
                            for (String data : datas) {
                                // 購読解除されている場合は処理をやめる
                                if (emitter.isCancelled()) {
                                    // RxJava 2.xでは購読が解除された場合、onNextメソッドなどの通知メソッドを呼び出しても、通知を行わないようになっていますが、createメソッドを使って実装している場合、購読が解除された後もFlowableの処理を続けるかどうかは実装者の責任になります。
                                    // つまり、RxJavaがしてくれるのは通知メソッドを呼び出しても通知を行わないことだけで、処理自体を止めてくれるわけではありません。
                                    // そのため、createメソッドを使って大量のデータをループしながら通知するFlowableを生成した場合や、完了することなく永遠にデータを通知するようなFlowableを生成した場合、購読解除された際の処理を自分で実装していないと処理が実行され続け無駄にリソースを費やすことになります。
                                    return;
                                }

                                // データを通知する
                                // RxJava 1.xと異なりRxJava 2.xからはnullが通知できなくなったことがあります。
                                // もしonNextメソッドの引数にnullを渡した場合、NullPointerExceptionが発生します。
                                emitter.onNext(data);
                            }

                            // 完了したことを通知する
                            emitter.onComplete();
                        },
                        // どのようなバックプレッシャーを行うのかのオプションを指定する
                        // BUFFER = Flowableによるデータの生成スピードがSubscriberによるデータの処理スピードより速い場合、通知できずに通知待ちとなったデータをすべてバッファし、通知できるようになるまで保持するモード
                        BackpressureStrategy.BUFFER);  // 超過したデータはバッファする


        // RxJava 2.xのSubscriberを引数に受け取るsubscribeメソッドは、戻り値を返さないようにRxJava 1.xから変更されています。
        // これはReactive Streamsの仕様に沿ったもので、このsubscribeメソッドを呼んだ場合は、Subscriberの内部で購読の解除を行うような設計になっています。

        // データ数をリクエストし、リクエストした分のデータを処理した後に再度データ数をリクエストするようなことを繰り返し行うと、何らかの問題が発生してデータ数のリクエストがされなくなり、消費者がデータを受け取れる状況にもかかわらず、生産者がデータの通知待ち状態になってしまうリスクがあります。
        // また、生産者と消費者での処理スピードのギャップがあまりない場合や通知するデータ数が多くない場合など、特に通知するデータ数を制限する必要がない場合も多くあります。
        // このような場合、onSubscribeメソッド内で実行するrequestメソッドにLong.MAX_VALUEを指定することで、生産者はデータ数の制限なくデータを通知できるようになり、このようなリスクを減らすことができることになります。
        flowable
                // Subscriberの処理を別スレッドで行うようにする
                .observeOn(Schedulers.computation())
                // 購読する
                .subscribe(new Subscriber<String>() {

                    /** データ数のリクエストおよび購読の解除を行うオブジェクト */
                    private Subscription subscription;

                    // 購読が開始された際の処理
                    @Override
                    public void onSubscribe(Subscription subscription) {
                        // SubscriptionをSubscriber内で保持する
                        this.subscription = subscription;
                        // 受け取るデータ数をリクエストする
                        this.subscription.request(1L);
                        // Subscriptionのrequestメソッドを使って最初に通知されるデータ数をリクエストすることで、初めてデータの通知が開始されます。
                        // 今回は最初にデータを1件だけ通知するようにリクエストしています。
                        // もし、このonSubscribeメソッド内でデータ数のリクエストを行っていないと、Flowableが通知するデータ数のリクエストが来るのを待っている状態になり、通知を始めることができなくなるので注意が必要です。

                        // requestメソッドの呼び出しをonSubscribeメソッドの最後で行っていることです。
                        // Flowableによってはrequestメソッドの呼び出しと同時にデータの通知を始めるものもあります。
                        // そのようなFlowableの場合、reuqestメソッド以降に記述してあるコードが、完了の通知を行うまで実行されなくなる可能性があります。
                        // そのためonSubscribeメソッド内でrequestメソッドを呼ぶ際は、最後に行うようにしてください。

                        // データ数を制限することなくデータを通知するようにリクエストする
//                        subscription.request(Long.MAX_VALUE);
                    }

                    // データを受け取った際の処理
                    @Override
                    public void onNext(String item) {
                        // 実行しているスレッド名の取得
                        String threadName = Thread.currentThread().getName();
                        // 受け取ったデータを出力する
                        System.out.println(threadName + ": " + item);

                        // 次に受け取るデータ数をリクエストする
                        // 受け取れるデータ数をリクエストする責任も持っており、このデータ数のリクエストを行わないとFlowableはデータを通知することはできません
                        // もし、次のデータ数をリクエストしていないと、最初にリクエストした分のデータを通知した後、それ以降データが通知されなくなる
                        this.subscription.request(1L);
                    }

                    // 完了を通知された際の処理
                    @Override
                    public void onComplete() {
                        // 実行しているスレッド名の取得
                        String threadName = Thread.currentThread().getName();
                        System.out.println(threadName + ": 完了しました");
                    }

                    // エラーを通知された際の処理
                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                    }
                });

        // ResourceSubscriber
        // onSubscribeメソッドでLong.MAX_VALUEをリクエストすることを内部で行うResourceSubscriberクラスが用意されています。
        // このResourceSubscriberは抽象クラスであり、基本的にはRxJava 1.xのObserverと同様に次のメソッドのみ実装すればよくなり、データ数のリクエストを忘れることを防げます。
        // onStartメソッドをオーバーライドすることで初期時のリクエストを行うことができます。
        // Disposableを実装しているため、購読を解除するdisposeメソッドも実装しています。
        // このdisposeメソッドは内部でSubscriptionのcancelメソッドを呼んでいるので、Subscriptionに直接アクセスできなくてもdisposeメソッド経由で購読の解除を行うことを可能にしています。
        flowable
                .subscribe(new ResourceSubscriber<String>() {

                    @Override
                    protected void onStart() {
                        request(2);
                    }

                    @Override
                    public void onNext(String s) {
                        System.out.println(s);
//                        dispose();
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {
                        System.out.println("onComplete");
                    }
                });

        // しばらく待つ
        Thread.sleep(500L);

        flowable
                // Subscriberの処理を別スレッドで行うようにする
                .observeOn(Schedulers.computation())
                // 購読する
                .subscribe(new ResourceSubscriber<String>() {

                    /** 購読の開始時間 */
                    private long startTime;

                    // 購読が開始された際の処理
                    @Override
                    protected void onStart() {
                        // 購読の開始時間を取得
                        startTime = System.currentTimeMillis();
                        // データ数のリクエスト
                        request(Long.MAX_VALUE);
                    }

                    // データを受け取った際の処理
                    @Override
                    public void onNext(String data) {
                        // 購読開始から500ミリ秒を過ぎた場合は購読を解除する
                        if ((System.currentTimeMillis() - startTime) > 500L) {
                            dispose();  //  購読を解除する
                            System.out.println("購読解除しました");
                            return;
                        }

                        // 重い処理をしているとみなして1000ミリ秒待機
                        // ※ 本来はThread.sleepは呼ぶべきではない
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            System.exit(1);
                        }

                        // 実行しているThread名の取得
                        String threadName = Thread.currentThread().getName();
                        // 受け取ったデータを出力する
                        System.out.println(threadName + ": " + data);
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });

        // subscribeメソッドにはこのReactive Streamsの仕様に沿ったものの他に、Disposableを戻り値に返す関数型インターフェースを引数に取るsubscribeメソッドも用意しています。この引数となる関数型インターフェースには各通知時の処理が定義されています。
        // そして、このsubscribeメソッドを使う場合は、戻り値のDisposableを使ってSubscriberの外から購読を解除することが可能になります。
        // subscribeメソッドでは、デフォルトでリクエストするデータ数にはLong.MAX_VALUEが設定されています（引数にonSubscribe時の関数型インターフェースを取るものを除く）。
        // そのためFlowableの通知に対しデータ数の制限はなくなり、再度データ数をリクエストする必要はなくなります。
        Disposable disposable = flowable.subscribe(System.out::println);
        disposable.dispose();

        /*
        さらにRxJava 2.xではこれらのsubscribeメソッドに加え、新たに購読を行うためのメソッドとして、Subscriberを引数に取り、戻り値も返すsubscribeWithメソッドを用意しています。このメソッドの実装は次のようになっており、引数にSubscriberを渡すと内部でそのSubscriberをsubscribeメソッドに渡して実行し、戻り値としてその引数を返すようになっています。

        subscriberWithメソッドの実装
        public final <E extends Subscriber<? super T>> E subscribeWith(E subscriber) {
            subscribe(subscriber);
            return subscriber;
        }
　
        これは一見Subscriberを返してどうするのかと思うかもしれませんが、引数にResourceSubscriberやDisposableSubscriberなどのDisposableを実装しているSubscribeを渡すことで戻り値としてDisposableを受け取ることができます。

        subscriberWithメソッドを使ってDisposableを戻り値を取得する例
        Disposable disposable =  flowable.subscribeWith(new ResourceSubscriber(){...});
         */

        // しばらく待つ
        Thread.sleep(1500L);

        // observeOn(Scheduler scheduler, boolean delayError, int bufferSize)
        // delayError trueの場合はエラーが発生しても、そのことをすぐには通知せず、バッファしているデータを通知し終えてからエラーを通知する。falseの場合は発生したらすぐにエラーを通知する。デフォルトはfalse。
        // bufferSize 通知待ちのデータをバッファするサイズ。デフォルトでは128。

        // 通知待ちのデータを扱う際に特に重要になるのが第3引数で、消費者に通知されるデータは、このバッファされた通知待ちのデータから取得されることになります。
        // 実際には、ここで指定した数値がFlowableに対してデータ数のリクエストを自動で行うようになっており、そのリクエストを受けて送られたデータがバッファされることになります。
        // つまり「1」を指定すると、内部でrequest(1)が実行されていることになります。

        // 100ミリ秒ごとに0から始まる数値を通知するFlowableに対し、別スレッド上でデータを受け取るSubscriberが処理を300ミリ秒待ってから受け取ったデータを出力するようにしています。
        // そのため、Flowableの処理がSubscriberの処理より早いので、通知待ちのデータが発生することになります。
        // そこで、このサンプルでは通知待ちのデータを破棄するように設定しています。
        // 通知されるまでに生成されたデータは破棄される。
        Flowable<Long> flowable2 =
                // 100ミリ秒ごとに0から始まるデータを通知するFlowableを生成……（1）
                Flowable.interval(100L, TimeUnit.MILLISECONDS)
                        // BackpressureMode.DROPを設定した時と同じ挙動にする……（2）
                        .onBackpressureDrop();


        // バッファサイズを「2」に変更すると
        // リクエストするまでに時間があいているにも関わらず、「0」「1」と続けてデータが通知されていることから、リクエストした後に通知されるデータはキャッシュされたデータであり、キャッシュサイズを超えて通知待ちになっているデータが破棄されていることが分かります。
        flowable2
                // 非同期でデータを受け取るようにし、バッファサイズを1にする……（3）
                .observeOn(Schedulers.computation(), false, 1)
                // 購読する
                .subscribe(new DisposableSubscriber<Long>() {

                    // データを受け取った際の処理
                    @Override
                    public void onNext(Long item) {
                        // 300ミリ秒待つ……（4）
                        try {
                            Thread.sleep(300L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            // 異常終了で終わる
                            System.exit(1);
                        }

                        // 実行しているThread名の取得
                        String threadName = Thread.currentThread().getName();
                        // 受け取ったデータを出力する
                        System.out.println(threadName + ": " + item);
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });

        // しばらく待つ
        Thread.sleep(3000L);

        Observable
                .create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> e) throws Exception {
                        String[] datas = {"Hello, World!", "こんにちは、世界！"};
                        for (String data : datas) {
                            // 購読解除されている場合は処理をやめる
                            if (e.isDisposed()) {
                                // RxJava 2.xでは購読が解除された場合、onNextメソッドなどの通知メソッドを呼び出しても、通知を行わないようになっていますが、createメソッドを使って実装している場合、購読が解除された後もFlowableの処理を続けるかどうかは実装者の責任になります。
                                // つまり、RxJavaがしてくれるのは通知メソッドを呼び出しても通知を行わないことだけで、処理自体を止めてくれるわけではありません。
                                // そのため、createメソッドを使って大量のデータをループしながら通知するFlowableを生成した場合や、完了することなく永遠にデータを通知するようなFlowableを生成した場合、購読解除された際の処理を自分で実装していないと処理が実行され続け無駄にリソースを費やすことになります。
                                return;
                            }

                            // データを通知する
                            // RxJava 1.xと異なりRxJava 2.xからはnullが通知できなくなったことがあります。
                            // もしonNextメソッドの引数にnullを渡した場合、NullPointerExceptionが発生します。
                            e.onNext(data);
                        }

                        // 完了したことを通知する
                        e.onComplete();
                    }
                })
                .subscribe(new Observer<String>() {

                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        // 購読の途中で購読を解除する必要がある場合は受け取ったDisposableをObserver内で保持する
                        disposable = d;
                    }

                    @Override
                    public void onNext(String s) {
                        System.out.println(s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println(e);
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("onComplete");
                    }
                });

        Observable
                .create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> e) throws Exception {
                        String[] datas = {"Hello, World!", "こんにちは、世界！"};
                        for (String data : datas) {
                            // 購読解除されている場合は処理をやめる
                            if (e.isDisposed()) {
                                // RxJava 2.xでは購読が解除された場合、onNextメソッドなどの通知メソッドを呼び出しても、通知を行わないようになっていますが、createメソッドを使って実装している場合、購読が解除された後もFlowableの処理を続けるかどうかは実装者の責任になります。
                                // つまり、RxJavaがしてくれるのは通知メソッドを呼び出しても通知を行わないことだけで、処理自体を止めてくれるわけではありません。
                                // そのため、createメソッドを使って大量のデータをループしながら通知するFlowableを生成した場合や、完了することなく永遠にデータを通知するようなFlowableを生成した場合、購読解除された際の処理を自分で実装していないと処理が実行され続け無駄にリソースを費やすことになります。
                                return;
                            }

                            // データを通知する
                            // RxJava 1.xと異なりRxJava 2.xからはnullが通知できなくなったことがあります。
                            // もしonNextメソッドの引数にnullを渡した場合、NullPointerExceptionが発生します。
                            e.onNext(data);
                        }

                        // 完了したことを通知する
                        e.onComplete();
                    }
                })
                .subscribe(new ResourceObserver<String>() {

                    @Override
                    protected void onStart() {
                        super.onStart();
                        // 購読開始時の処理としてonStartメソッドが用意されており、購読開始時に何か行う場合はこのメソッドをオーバーライド
                        System.out.println("onStart");
                    }

                    @Override
                    public void onNext(String s) {
                        dispose();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        System.out.println("onComplete");
                    }
                });

        Flowable<Integer> result = Flowable
                // 引数のデータを通知するFlowableを生成
                .just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                // 偶数のデータのみを通知する
                .filter(data -> data % 2 == 0)
                // 通知するデータを10倍にする
                .map(data -> data * 10);
        result.subscribe(System.out::println);

        Flowable.just("A", "a", "a", "A", "a")
                // 連続して重複したデータを除いて通知する
                .distinctUntilChanged()
                .subscribe(new DebugSubscriber<>());

        Flowable.just("A", "B", "", "", "C")  // ①
                .observeOn(Schedulers.computation())
                // flatMapメソッドを使って空文字を除きかつ小文字に変換
                .flatMap(data -> {
                    if ("".equals(data)) {
                        // 空文字なら空のFlowableを返す
                        return Flowable.empty();  // ②
                    } else {
                        // 受け取ったデータの大文字と小文字を3000ミリ秒後に通知する
                        return Flowable
                                .just(data.toUpperCase(), data.toLowerCase())
                                .delay(300L, TimeUnit.MILLISECONDS);  // ③
                    }
                })
                .subscribe(new DebugSubscriber<>());

        Thread.sleep(2000L);

        Single.
                create((SingleOnSubscribe<String>) e -> e.onSuccess("single"))
                .subscribe(new DisposableSingleObserver<String>() {
                    @Override
                    public void onSuccess(String s) {
                        System.out.println(s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println(e);
                    }
                });

        Maybe
                .create(new MaybeOnSubscribe<String>() {
                    @Override
                    public void subscribe(MaybeEmitter<String> e) throws Exception {
                        if ("".length() > 0) {
                            e.onComplete();
                            return;
                        }

                        try {
                            e.onSuccess("Maybe");
                        } catch (Exception e1) {
                            e.onError(e1);
                        }

                    }
                })
                .subscribe(new DisposableMaybeObserver<String>() {
                    @Override
                    public void onSuccess(String s) {
                        System.out.println(s);

                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println(e);
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("Maybe onComplete");
                    }
                });

        Completable
                .create(new CompletableOnSubscribe() {
                    @Override
                    public void subscribe(CompletableEmitter e) throws Exception {
                        e.onComplete();
                    }
                })
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        System.out.println("Completable onComplete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println(e);
                    }
                });
    }
}