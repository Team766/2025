package com.team766.simulator.interfaces;

public interface MechanicalDevice {
    record State(double position, double velocity) {}

    record Action(double force) {}

    Action step(State state, double dt);
}
