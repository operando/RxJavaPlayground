package rx.playground;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.SerializedSubject;

public class Variable<T> {
    private T value;

    private final SerializedSubject<T, T> serializedSubject;

    public Variable(T value) {
        this.value = value;
        serializedSubject = new SerializedSubject<>(BehaviorSubject.create(value));
    }

    public synchronized T get() {
        return value;
    }

    public synchronized void set(T value) {
        this.value = value;
        serializedSubject.onNext(this.value);
    }

    public Observable<T> asObservable() {
        return serializedSubject.asObservable();
    }
}