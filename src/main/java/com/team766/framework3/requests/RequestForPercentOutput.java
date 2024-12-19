package com.team766.framework3.requests;

import com.team766.framework3.InstantRequest;
import com.team766.framework3.Mechanism;
import com.team766.hal.MotorController;

public class RequestForPercentOutput<M extends Mechanism<M, ?>> extends InstantRequest<M> {
    private final MotorController controller;
    private final double percentOutput;

    public RequestForPercentOutput(MotorController controller, double percentOutput) {
        this.controller = controller;
        this.percentOutput = percentOutput;
    }

    @Override
    public void runOnce() {
        controller.set(percentOutput);
    }
}
