package io.randomthoughts;

public class TryCatch {
    public static <T> T attempt(CheckedSupplier<T> supplier) {
        return attempt(supplier, null);
    }

    public static <T> T attempt(CheckedSupplier<T> supplier, T defaultValue) {
        try {
            return supplier.get();
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }
}
