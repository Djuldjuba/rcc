package io.student.rococo.utils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class Waiter {

    private Waiter() {
    }

    public static <T> Optional<T> waitOptional(Supplier<Optional<T>> supplier,
                                               long timeoutMs,
                                               long pollIntervalMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        Optional<T> result = supplier.get();
        while (result.isEmpty()) {
            if (System.currentTimeMillis() >= deadline) {
                return Optional.empty();
            }
            TimeUnit.MILLISECONDS.sleep(pollIntervalMs);
            result = supplier.get();
        }
        return result;
    }
}