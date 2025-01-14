package com.team766.framework3;

class FakeMechanism extends Mechanism {
    record FakeStatus(int currentState) implements Status {}

    public void mutateMechanism() {
        checkContextReservation();
    }
}

class FakeMechanism1 extends FakeMechanism {}

class FakeMechanism2 extends FakeMechanism {}

class FakeMechanism3 extends FakeMechanism {}

class FakeMechanism4 extends FakeMechanism {}
