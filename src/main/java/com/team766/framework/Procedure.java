package com.team766.framework;

import com.google.common.collect.Sets;
import com.team766.logging.Category;
import edu.wpi.first.wpilibj2.command.Command;
import java.util.Collection;
import java.util.Set;

public abstract class Procedure implements StatusesMixin, LoggingBase {
    // A reusable Procedure that does nothing.
    private static final class NoOpProcedure extends InstantProcedure {
        @Override
        public void run(InstantContext context) {}
    }

    public static final InstantProcedure NO_OP = new NoOpProcedure();

    private static int c_idCounter = 0;

    private static synchronized int createNewId() {
        return c_idCounter++;
    }

    private final String name;
    private final Set<Reservable> reservations;

    protected Procedure() {
        this.name = createName();
        this.reservations = Sets.newHashSet();
    }

    protected Procedure(Set<Reservable> reservations) {
        this.name = createName();
        this.reservations = reservations;
    }

    protected Procedure(String name, Set<Reservable> reservations) {
        this.name = name;
        this.reservations = reservations;
    }

    public abstract void run(Context context);

    public Command createCommandToRunProcedure() {
        return new ContextImpl(this);
    }

    private String createName() {
        return this.getClass().getName() + "/" + createNewId();
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public Category getLoggerCategory() {
        return Category.PROCEDURES;
    }

    protected final <M extends Reservable> M reserve(M m) {
        reservations.add(m);
        return m;
    }

    protected final void reserve(Reservable... ms) {
        for (var m : ms) {
            reservations.add(m);
        }
    }

    protected final void reserve(Collection<? extends Reservable> ms) {
        reservations.addAll(ms);
    }

    public final Set<Reservable> reservations() {
        return reservations;
    }

    @Override
    public final String toString() {
        return getName();
    }

    /* package */ void checkReservations(Command command) {
        final var commandReservations = command.getRequirements();
        for (var res : this.reservations()) {
            for (var req : res.getReservableSubsystems()) {
                if (!commandReservations.contains(req)) {
                    throw new IllegalArgumentException(
                            command.getName()
                                    + " tried to run "
                                    + this.getName()
                                    + " but is missing the reservation on "
                                    + req.getName());
                }
            }
        }
    }
}
