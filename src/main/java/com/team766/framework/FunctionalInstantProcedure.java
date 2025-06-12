package com.team766.framework;

import java.util.Set;
import java.util.function.Consumer;

public final class FunctionalInstantProcedure extends InstantProcedure {
    private final Consumer<InstantContext> runnable;

    public FunctionalInstantProcedure(Set<Reservable> reservations, Runnable runnable) {
        this(runnable.toString(), reservations, runnable);
    }

    public FunctionalInstantProcedure(
            Set<Reservable> reservations, Consumer<InstantContext> runnable) {
        this(runnable.toString(), reservations, runnable);
    }

    public FunctionalInstantProcedure(
            String name, Set<Reservable> reservations, Runnable runnable) {
        this(name, reservations, (context) -> runnable.run());
    }

    public FunctionalInstantProcedure(
            String name, Set<Reservable> reservations, Consumer<InstantContext> runnable) {
        super(name, reservations);
        this.runnable = runnable;
    }

    @Override
    public void run(InstantContext context) {
        runnable.accept(context);
    }
}
