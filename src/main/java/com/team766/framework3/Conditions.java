package com.team766.framework3;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Pre-canned Conditions used frqeuently in robot programming.
 */
public class Conditions {
    /**
     * Suspend the Procedure until the given Supplier returns a non-empty Optional. Then, unwrap
     * the T value from the Optional and return it.
     */
    public static <T> T waitForValue(Context context, Supplier<Optional<T>> supplier) {
        final AtomicReference<T> result = new AtomicReference<>();
        context.waitFor(
                () -> {
                    final var t = supplier.get();
                    t.ifPresent(result::set);
                    return t.isPresent();
                });
        return result.get();
    }

    /**
     * Suspend the Procedure until the given Supplier returns a non-empty Optional, or we've waited
     * for at least {@code timeoutSeconds}. Returns the last value returned by the Supplier.
     */
    public static <T> Optional<T> waitForValueOrTimeout(
            Context context, Supplier<Optional<T>> supplier, double timeoutSeconds) {
        final AtomicReference<Optional<T>> result = new AtomicReference<>(Optional.empty());
        context.waitForConditionOrTimeout(
                () -> {
                    result.set(supplier.get());
                    return result.get().isPresent();
                },
                timeoutSeconds);
        return result.get();
    }

    /**
     * Suspend the Procedure until {@link Request#isDone} returns true.
     */
    public static void waitForRequest(Context context, Request<?> request) {
        context.waitFor(request::isDone);
    }

    /**
     * Suspend the Procedure until {@link Request#isDone} returns true, or we've waited for at least
     * {@code timeoutSeconds}. Returns true if the Request is done; false otherwise.
     */
    public static boolean waitForRequestOrTimeout(
            Context context, Request<?> request, double timeoutSeconds) {
        return context.waitForConditionOrTimeout(request::isDone, timeoutSeconds);
    }

    /**
     * This predicate toggles its value (false -> true, or true -> false) whenever the provided
     * predicate changes from false to true (rising edge). Otherwise, it retains its previous value.
     *
     * This is useful when you want a joystick button to switch between two different modes whenever
     * it is pushed (pass `() -> joystick.getButton()` as the constructor argument).
     */
    public static final class Toggle implements BooleanSupplier {
        private final BooleanSupplier predicate;
        private boolean predicatePrevious = false;
        private boolean toggleValue = false;

        public Toggle(BooleanSupplier predicate) {
            this.predicate = predicate;
        }

        @Override
        public boolean getAsBoolean() {
            final var current = predicate.getAsBoolean();
            if (current && !predicatePrevious) {
                toggleValue = !toggleValue;
            }
            predicatePrevious = current;
            return toggleValue;
        }
    }

    // utility class
    private Conditions() {}
}
