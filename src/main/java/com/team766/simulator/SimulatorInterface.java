package com.team766.simulator;

public interface SimulatorInterface {
    record StepResult(boolean simWasReset, double deltaTime) {}

    StepResult step();
}
