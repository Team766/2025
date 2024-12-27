package com.team766.framework3;

class FakeMechanism extends MechanismWithStatus<FakeMechanism.FakeStatus> {
    public record FakeStatus(int currentState) implements Status {}

    public Request<FakeMechanism> requestFakeState(int targetState) {
        currentState = targetState;
        wasRequestNew = true;
        currentState = targetState;
        return startRequest(
                () -> {
                    currentState = targetState;
                    wasRequestNew = false;

                    return currentState == targetState;
                });
    }

    Integer currentState = null;
    Boolean wasRequestNew = null;

    public FakeMechanism() {
        // Set initial request
        requestFakeState(-1);
    }

    @Override
    protected FakeStatus reportStatus() {
        return new FakeStatus(currentState);
    }
}

class FakeMechanism1 extends FakeMechanism {}

class FakeMechanism2 extends FakeMechanism {}

class FakeMechanism3 extends FakeMechanism {}
