package cz.matysekxx.beatbounce.util;

import java.util.function.Supplier;

public class Lazy<T> {
    private final Supplier<T> supplier;
    private T value;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    public T get() {
        if (value == null) {
            value = supplier.get();
        }
        return value;
    }

    public boolean wasInitialized() {
        return value != null;
    }

    public void initialize() {
        if (!wasInitialized()) value = supplier.get();
    }
}