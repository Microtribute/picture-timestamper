package io.randomthoughts;

public class TryCatch {
    public static <T> T attempt(CheckedSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static <T> T attempt(CheckedSupplier<T> supplier, T defaultValue) {
        try {
            return supplier.get();
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }
}