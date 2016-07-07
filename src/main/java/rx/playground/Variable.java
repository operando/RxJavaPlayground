package rx.playground;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.SerializedSubject;

public class Variable<T> {
    private T value;

    private final SerializedSubject<T, T> serializedSubject;

    public Variable(T value) {
        serializedSubject = new SerializedSubject<>(BehaviorSubject.create(value));
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        serializedSubject.onNext(value);
    }

    public Observable<T> asObservable() {
        return serializedSubject.asObservable();
    }
}