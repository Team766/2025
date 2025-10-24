package com.team766.simulator.interfaces;

public interface ElectricalDevice {
    record State(double voltage) {}

    record Action(double current) {}

    String name();

    Action step(State state, double dt);
}
