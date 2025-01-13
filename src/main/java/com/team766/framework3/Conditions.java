package com.team766.framework3;

import java.util.function.BooleanSupplier;

/**
 * Pre-canned Conditions used frqeuently in robot programming.
 */
public class Conditions {
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
