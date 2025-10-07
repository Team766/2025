package com.team766.hal.simulator;

public interface SimulatorInterface {
    /**
     * @return deltaTime for the pending program step
     */
    double prepareStep();

    void setResetHandler(Runnable handler);
}
