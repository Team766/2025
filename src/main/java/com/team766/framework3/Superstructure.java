package com.team766.framework3;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A Superstructure coordinates the behavior of several Mechanisms which could interfere with one
 * another if not commanded properly (e.g. they could collide with each other, or some other part
 * of the robot, or the floor, etc).
 *
 * A Mechanism in a Superstructure cannot be reserved individually by Procedures (Procedures
 * should reserve the entire Superstructure) and cannot have an Idle request. Only the
 * Superstructure should set requests on its constituent Mechanisms (in its
 * {@link #run(R, boolean)} method).
 */
public abstract class Superstructure extends Mechanism {
    private final ArrayList<Mechanism> submechanisms = new ArrayList<>();

    @Override
    /* package */ final void runSubmechanisms() {
        for (var m : submechanisms) {
            m.periodicInternal();
        }
    }

    protected final Directive requestOfSubmechanism(Request<?> submechanismRequest) {
        if (!submechanisms.contains(submechanismRequest.getMechanism())) {
            throw new IllegalArgumentException(
                    "Request is for "
                            + submechanismRequest.getMechanism()
                            + " which is not a submechanism of "
                            + getName());
        }
        return new Directive() {
            @Override
            public boolean update() {
                return submechanismRequest.isDone();
            }

            @Override
            public String getProvenance() {
                return submechanismRequest.getProvenance();
            }
        };
    }

    protected final <M extends Mechanism> M addMechanism(M submechanism) {
        Objects.requireNonNull(submechanism);
        submechanism.setSuperstructure(this);
        submechanisms.add(submechanism);
        return submechanism;
    }
}
