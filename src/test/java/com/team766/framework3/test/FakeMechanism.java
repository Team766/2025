package com.team766.framework3.test;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.NoReservationRequired;
import com.team766.framework3.Status;

public class FakeMechanism extends MechanismWithStatus<FakeMechanism.FakeStatus> {
    public record FakeStatus(int currentState) implements Status {}

    int currentState = -1;

    public void mutateMechanism(int newState) {
        currentState = newState;
    }

    // Note: @NoReservationRequired is an advanced feature. You probably shouldn't use it.
    // Most uses of non-mutating methods should be solved using Statuses.
    @NoReservationRequired
    public void nonMutatingMethod() {
        System.out.println("my state: " + currentState);
    }

    @Override
    protected void onMechanismIdle() {
        mutateMechanism(10);
    }

    @Override
    protected FakeStatus updateStatus() {
        return new FakeStatus(currentState);
    }
}
