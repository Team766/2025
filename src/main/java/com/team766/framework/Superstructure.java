package com.team766.framework;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A Superstructure coordinates the behavior of several Mechanisms which could interfere with one
 * another if not commanded properly (e.g. they could collide with each other, or some other part
 * of the robot, or the floor, etc).
 *
 * A Mechanism in a Superstructure cannot be reserved individually by Procedures (Procedures
 * should reserve the entire Superstructure). Only the Superstructure should call methods on its
 * constituent Mechanisms, wrapped with calls to the {@link #startForMechanism} family of methods.
 */
public abstract class Superstructure extends Mechanism {
    private ArrayList<Mechanism> submechanisms = new ArrayList<>();

    protected final <M extends Mechanism> M addMechanism(M submechanism) {
        Objects.requireNonNull(submechanism);
        submechanisms.add(submechanism);
        return submechanism;
    }

    protected final LaunchedCommand startForMechanisms(Procedure procedure) {
        for (var req : procedure.reservations()) {
            if (!submechanisms.contains(req)) {
                throw new IllegalArgumentException(
                        getName()
                                + " tried to start "
                                + procedure.getName()
                                + " but it reserves "
                                + req.getName()
                                + " which is not part of this Superstructure");
            }
        }
        var command = procedure.createCommandToRunProcedure();
        command.schedule();
        return new LaunchedCommand(command);
    }

    protected final LaunchedCommand startForMechanisms(
            Set<Reservable> mechanisms, Consumer<Context> procedure) {
        return startForMechanisms(new FunctionalProcedure(mechanisms, procedure));
    }

    protected final LaunchedCommand startForMechanisms(
            Set<Reservable> mechanisms, Runnable procedure) {
        return startForMechanisms(new FunctionalInstantProcedure(mechanisms, procedure));
    }

    protected final LaunchedCommand startForMechanism(
            Reservable mechanism, Consumer<Context> procedure) {
        return startForMechanisms(Set.of(mechanism), procedure);
    }

    protected final LaunchedCommand startForMechanism(Mechanism mechanism, Runnable procedure) {
        return startForMechanisms(Set.of(mechanism), procedure);
    }
}
