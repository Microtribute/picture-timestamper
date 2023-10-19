package io.randomthoughts;

import java.util.function.Supplier;

@FunctionalInterface
public interface CheckedSupplier<T> extends Supplier<T> {
    @Override
    default T get() throws RuntimeException {
        try {
            return runExceptionally();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    T runExceptionally() throws Throwable;
}