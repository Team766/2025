package com.team766.framework3;

class FakeMechanism extends MechanismWithStatus<FakeMechanism.FakeStatus> {
    public record FakeStatus(int currentState) implements Status {}

    int currentState = -1;

    public void mutateMechanism(int newState) {
        checkContextReservation();
        currentState = newState;
    }

    @Override
    protected void onMechanismIdle() {
        mutateMechanism(10);
    }

    @Override
    protected FakeStatus reportStatus() {
        return new FakeStatus(currentState);
    }
}

class FakeMechanism1 extends FakeMechanism {}

class FakeMechanism2 extends FakeMechanism {}

class FakeMechanism3 extends FakeMechanism {}

class FakeMechanism4 extends FakeMechanism {}
